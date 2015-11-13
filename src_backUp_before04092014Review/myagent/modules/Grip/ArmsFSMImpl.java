/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.modules.Grip;

import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.modules.FSMImpl;

/*
 *  This class provides the updated position of the arms to achieve
 *  the desired hand moving. arm(2,3,4)_pos ==> arm(2,3,4)_updated_pos
 *  Note that these operations are supposed to control arms when the Herbert is exploring the object
 *  in the opposite side of home; The arms movements acted in the home side is not controlled by these operations.
 * @author Daqi
 *
 */
public abstract class ArmsFSMImpl extends FSMImpl implements ArmsFSM {

    private static final Logger logger = Logger.getLogger(ArmsFSMImpl.class.getCanonicalName());
    protected static final double ARM2_INIT_POS = -0.18;
    protected static final double ARM3_INIT_POS = -1.19;
    protected static final double ARM4_INIT_POS = -1.77;

    protected static final double ARM1_HOME_POS = 1.57;
    protected static final double ARM1_START_POS = 0.0;

    protected static final double APPROX_EQUAL_MIN_VAL = 0.0001;

    protected static final double APPROX_EQUAL_MAX_VAL = 0.03;
    // This is the MAX space used to check sum pf arms position is almost equal to Math.PI

    protected static final double APPROX_EQUAL_MAX_VAL2 = 0.03;
    // This is used to check arms positions limitation

    protected static final double APPROX_EQUAL_MAX_VAL3 = 1.0;
    // This variable represents the MAX deviation of beam distance; it is used to
    // check whether the beam distance changed.

    protected static final double ARM2_MIN_POS = -1.57;
    protected static final double ARM2_MAX_POS = 1.57;

    protected static final double ARM3_MIN_POS = -2.64;
    protected static final double ARM3_MAX_POS = 2.55;

    protected static final double ARM4_MIN_POS = -1.78;
    protected static final double ARM4_MAX_POS = 1.78;

    protected static final double ATOMIC_SPACE = 0.01;
    // Its value is negative because the hand is in the opposite side of home

    protected static final double NORMAL_SPACE = ATOMIC_SPACE * 4;

    protected static final double SLIGHT_SPACE = ATOMIC_SPACE;

    protected static final double SLIGHT_DOWN_PROPORTION = 0.95;

    protected static final double SLIGHT_EXTEND_PROPORTION = 1.005;

    protected static final double SLIGHT_BACK_PROPORTION = 0.96;

    protected static final double ARM3_ARM2_PROPORTION = 0.871; //an older value '0.895' calculated manually
    // The real exact value of arm3/arm2 ==> arm3 == 135mm, arm2 == 155 mm
    // (http://www.youbot-store.com/youbot-developers/software/ros/youbot-inverse-kinematics?c=43)
    
    protected static final double ENLARGE_SPACE_HEIGHT = 1.05;
    protected static final double ENLARGE_SPACE_DISTANCE = 1.05;
    protected static final double SHORTER_SPACE_HEIGHT = 0.95;
    protected static final double SHORTER_SPACE_DISTANCE = 0.95;
    
    protected double arm1_pos, arm2_pos, arm3_pos, arm4_pos, arm1_updated_pos,
            arm2_updated_pos, arm3_updated_pos, arm4_updated_pos;
    private boolean Sign_flag;
    //This Sign_flag is used to indicate the sign of arm position
    // Sign_flag == true ==> Positive
    // Sign_flag == false ==> Negative
    
    //Note that, because we assume arms are always in the opposite side of home, so
    //arms psositions are always negative. Therefore, Sign_flag <-- flase
    private double arm2_positive_pos, arm3_positive_pos, arm4_positive_pos;
    // arm1 is positive variable, therefore arm1_positive_pos is not necessary
    
    // These temparory POS variables are supposed to hold positive values
    private double ARM3_ARM2_PROPORTION_power2;
    private double ARMS_INIT_POS_Height, ARMS_INIT_POS_Distance;

    public ArmsFSMImpl(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {
        //System.out.println("movingHand starts...");

        arm1_pos = 0.0;

        arm2_pos = 0.0;

        arm3_pos = 0.0;

        arm4_pos = 0.0;

        arm1_updated_pos = 0.0;

        arm2_updated_pos = 0.0;

        arm3_updated_pos = 0.0;

        arm4_updated_pos = 0.0;

        commands.put("Arm1_pos", null);
        commands.put("Arm2_pos", null);
        commands.put("Arm3_pos", null);
        commands.put("Arm4_pos", null);
        commands.put("Arm5_pos", null);

        arm2_positive_pos = 0.0;

        arm3_positive_pos = 0.0;

        arm4_positive_pos = 0.0;

        Sign_flag = false;
        //Note that, because we assume arms are always in the opposite side of home, so
        //arms psositions are always negative. Therefore, Sign_flag <-- flase

        ARM3_ARM2_PROPORTION_power2 = Math.pow(ARM3_ARM2_PROPORTION, 2);

        ARMS_INIT_POS_Height = Math.cos(Math.abs(ARM2_INIT_POS))
                - ARM3_ARM2_PROPORTION * Math.cos(Math.abs(ARM4_INIT_POS));

        ARMS_INIT_POS_Distance = Math.sin(Math.abs(ARM2_INIT_POS))
                + ARM3_ARM2_PROPORTION * Math.sin(Math.abs(ARM4_INIT_POS));

    }

    // Input the current arms positions
    @Override
    public void recieveArmsPositions(double arm2, double arm3, double arm4) {

        arm2_pos = arm2;
        arm3_pos = arm3;
        arm4_pos = arm4;

        arm2_positive_pos = Math.abs(arm2_pos);
        arm3_positive_pos = Math.abs(arm3_pos);
        arm4_positive_pos = Math.abs(arm4_pos);

    }

    //Input the position of arm1
    @Override
    public void recieveHomeArm(double arm1){

        arm1_pos = arm1;

    }

    //Valid checking of input arms position
    protected boolean arms_are_valid(double arm2, double arm3, double arm4) {

        boolean arms_are_valid = true;

        double sum_pos = 0.0;

        // 1. General requirements for the arms
        // --> Within the limitations && is a number

        // 2. Special requirements for this youbot robot
        //Arms are supposed to work on the opposite side of home, so arm2 position value should not be positive.
        // Only the Elbow UP is acceptable (Elbow Down is not), so arm3 and arm 4 should not be positive

        sum_pos = arm2 + arm3 + arm4;

        if (Math.abs(Math.abs(sum_pos) - Math.PI) > APPROX_EQUAL_MAX_VAL) {
            arms_are_valid = false;
            logger.log(Level.SEVERE, "Invalid sum position:" + sum_pos, 0L);
        }

        if (((arm2 > ARM2_MAX_POS + APPROX_EQUAL_MAX_VAL2)
                || (arm2 < ARM2_MIN_POS - APPROX_EQUAL_MAX_VAL2)) || Double.isNaN(arm2)
                || (arm2 > 0.0)) {
            arms_are_valid = false;
            logger.log(Level.SEVERE, "Invalid Arm2 position:" + arm2, 0L);
        }


        if (((arm3 > ARM3_MAX_POS + APPROX_EQUAL_MAX_VAL2)
                || (arm3 < ARM3_MIN_POS - APPROX_EQUAL_MAX_VAL2)) || Double.isNaN(arm3)
                || (arm3 > 0.0)) {
            arms_are_valid = false;
            logger.log(Level.SEVERE, "Invalid Arm3 position:" + arm3, 0L);
        }

        if (((arm4 > ARM4_MAX_POS + APPROX_EQUAL_MAX_VAL2)
                || (arm4 < ARM4_MIN_POS - APPROX_EQUAL_MAX_VAL2)) || Double.isNaN(arm4)
                || (arm4 > 0.0)) {
            arms_are_valid = false;
            logger.log(Level.SEVERE, "Invalid Arm4 position:" + arm4, 0L);
        }

        return arms_are_valid;
    }

    //descend
    protected boolean downward() {

        return downward(NORMAL_SPACE);
        //return generalHandMoving(0.95, 1.0);
    }

    protected boolean downward(double space) {

        double sin_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        // In this process, we assume all arms position are positive,
        // and assign proper sign to them at end.

        //First step --> Degree of arm4 decreases
        degree_arm4 = arm4_positive_pos - space;

        //The value of "Length of arm 2 * sine(Degree of arm2) + Length of arm3 * sine(Degree of arm4)" is constant
        sin_degree_arm2 = Math.sin(arm2_positive_pos) + ARM3_ARM2_PROPORTION * (Math.sin(arm4_positive_pos) - Math.sin(degree_arm4));
        degree_arm2 = Math.asin(sin_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false) {
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true) {

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        } else {
            return false;
        }

    }

    //lift
    protected boolean upward() {

        return upward(NORMAL_SPACE);
        //return generalHandMoving(1.05, 1.0);
    }

    protected boolean upward(double space) {
        // This upward was implemented very similar to the downward; The only
        // difference is the arm4 position instead increases.

        double sin_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        //First step --> Degree of arm4 increase
        degree_arm4 = arm4_positive_pos + space;

        //The value of "Length of arm 2 * sine(Degree of arm2) + Length of arm3 * sine(Degree of arm4)" is constant
        sin_degree_arm2 = Math.sin(arm2_positive_pos) + ARM3_ARM2_PROPORTION * (Math.sin(arm4_positive_pos) - Math.sin(degree_arm4));
        degree_arm2 = Math.asin(sin_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false) {
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true) {

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        } else {
            return false;
        }

    }

    //extend
    protected boolean forward() {

        return forward(NORMAL_SPACE);
        //return generalHandMoving(1.0, 1.05);
    }

    protected boolean forward(double space) {
        double cos_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        // This upward was implemented very similar to the downward;
        // The difference are as below:
        // 1) the arm4 position instead increases.
        // 2) Arms position calculating formula is a little bit different:
        // 2-1) sine --> cosine
        // 2-2) Regarding the two major parts in the formula, Operation Sum --> Operation Sbustration

        //First step --> Degree of arm4 increases
        degree_arm4 = arm4_positive_pos + space;

        //The value of "Length of arm 2 * cosine(Degree of arm2) - Length of arm3 * cosine(Degree of arm4)" is constant
        cos_degree_arm2 = Math.cos(arm2_positive_pos) - ARM3_ARM2_PROPORTION * (Math.cos(arm4_positive_pos) - Math.cos(degree_arm4));
        degree_arm2 = Math.acos(cos_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false) {
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true) {

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        } else {
            return false;
        }

    }

    //back
    protected boolean backward() {

        return backward(NORMAL_SPACE);
    }

    protected boolean backward(double space) {
        // :: First step --> Degree of arm4 decreases
        // ::Constraints:
        // 1) Degree of arm2 + arm3 + arm4 == PI
        // 2) The value of "Length of arm 2 * cosine(Degree of arm2) - Length of arm3 * cosine(Degree of arm4)" is constant

        return true;
    }

    //extend and slightly downward
    protected boolean forward_down() {

        return forward_down(SLIGHT_SPACE, SLIGHT_DOWN_PROPORTION);
        //return generalHandMoving(0.95, 1.05);
    }

    protected boolean forward_down(double space, double prportion) {

        double cos_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        // This forward_down() is similar to forward(), except:
        // D' != D --> D' == prportion * D, and prportion < 1 becase the behavior 'down'

        //First step --> Degree of arm4 increases
        degree_arm4 = arm4_positive_pos + space;

        //The value of "Length of arm 2 * cosine(Degree of arm2) - Length of arm3 * cosine(Degree of arm4)" decreases with the rate of 'prportion'
        cos_degree_arm2 = prportion * Math.cos(arm2_positive_pos) - ARM3_ARM2_PROPORTION * (prportion * Math.cos(arm4_positive_pos) - Math.cos(degree_arm4));
        degree_arm2 = Math.acos(cos_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false) {
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true) {

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        } else {
            return false;
        }
    }

    //Mainly go down and slighly forward
    protected boolean down_forward(){
        return down_forward(NORMAL_SPACE, SLIGHT_EXTEND_PROPORTION);
        
    }

    protected boolean down_forward(double space, double prportion){

        double sin_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        // This down_forward() is similar to forward_down(), except:
        // Main and minor behavior intentions are inverted

        //First step --> Degree of arm4 decrease
        degree_arm4 = arm4_positive_pos - space;

        //The value of "Length of arm 2 * sine(Degree of arm2) + Length of arm3 * sine(Degree of arm4)" increases with the rate of 'prportion'
        sin_degree_arm2 = prportion * Math.sin(arm2_positive_pos) + ARM3_ARM2_PROPORTION * (prportion * Math.sin(arm4_positive_pos) - Math.sin(degree_arm4));

        degree_arm2 = Math.asin(sin_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false) {
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true) {

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        } else {
            return false;
        }
    }

    //Rise and slighly backward
    protected boolean rise_backward(){
        return rise_backward(NORMAL_SPACE, SLIGHT_BACK_PROPORTION);

    }

    protected boolean rise_backward(double space, double prportion){

        double sin_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        // This rise_backward() is similar to down_forward(), except:
        // Towards the opposite directions

        //First step --> Degree of arm4 increase
        degree_arm4 = arm4_positive_pos + space;

        //The value of "Length of arm 2 * sine(Degree of arm2) + Length of arm3 * sine(Degree of arm4)" increases with the rate of 'prportion'
        sin_degree_arm2 = prportion * Math.sin(arm2_positive_pos) + ARM3_ARM2_PROPORTION * (prportion * Math.sin(arm4_positive_pos) - Math.sin(degree_arm4));

        degree_arm2 = Math.asin(sin_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false) {
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true) {

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        } else {
            return false;
        }
    }

    protected boolean back_init() {
        return back_init(NORMAL_SPACE);
    }

    protected boolean back_init(double space) {

        double degree_arm2 = 0.0, degree_arm3 = 0.0, degree_arm4 = 0.0;

        // Because the target positinos are already known, we could control the 
        // the angle directly.
        // Explictly controling two angles and passively for the rest one,
        // in the order of arm2, arm3, and arm4

        //Here we directly use values of arms and its Init position
        //because we know that they are both negative.
        if (Math.abs(arm2_pos - ARM2_INIT_POS) > space) {
            degree_arm2 = arm2_pos
                    - space * Math.abs(arm2_pos - ARM2_INIT_POS) / (arm2_pos - ARM2_INIT_POS);
        } else {
            degree_arm2 = ARM2_INIT_POS;
        }


        if (Math.abs(arm3_pos - ARM3_INIT_POS) > space) {
            degree_arm3 = arm3_pos
                    - space * Math.abs(arm3_pos - ARM3_INIT_POS) / (arm3_pos - ARM3_INIT_POS);
        } else {
            degree_arm3 = ARM3_INIT_POS;
        }

        //here we are not using (0 - Math.PI) but -3.14 because the sum of home
        //position is 3.14
        degree_arm4 = (0 - 3.14) - degree_arm2 - degree_arm3;

        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true) {

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        } else {
            return false;
        }

    }
    
    // go back to start position
    // TODO: Not was invoked yet
    protected boolean back_start_general() {
        double current_height = 0.0, current_distance = 0.0;
        double hieght_scale_value = 0.0, distance_scale_value = 0.0;

        current_height = Math.cos(arm2_positive_pos)
                - ARM3_ARM2_PROPORTION * Math.cos(arm4_positive_pos);

        if (current_height <= 0) {
            logger.log(Level.SEVERE, "Current height is invalid:" + current_height, 0L);
            return false;
        }

        current_distance = Math.sin(arm2_positive_pos)
                + ARM3_ARM2_PROPORTION * Math.sin(arm4_positive_pos);

        if (current_distance <= 0) {
            logger.log(Level.SEVERE, "Current distance is invalid:" + current_distance, 0L);
            return false;
        }

        System.out.println("ARMS_INIT_POS_Height:" + ARMS_INIT_POS_Height);
        System.out.println("current_height:" + current_height);
        // Current position --> Init position
        if (current_height < ARMS_INIT_POS_Height) {
            hieght_scale_value = ENLARGE_SPACE_HEIGHT;
        } else if (current_height > ARMS_INIT_POS_Height) {
            hieght_scale_value = SHORTER_SPACE_HEIGHT;
        } else {
            hieght_scale_value = 1.0;
        }

        if (current_distance < ARMS_INIT_POS_Distance) {
            distance_scale_value = ENLARGE_SPACE_DISTANCE;
        } else if (current_distance > ARMS_INIT_POS_Distance) {
            distance_scale_value = SHORTER_SPACE_DISTANCE;
        } else {
            distance_scale_value = 1.0;
        }

        return generalHandMoving(hieght_scale_value, distance_scale_value);
    }

    protected boolean generalHandMoving(double height_scale, double distance_scale) {

        if (height_scale <= 0) {
            logger.log(Level.SEVERE, "Invalid height scale value:" + height_scale, 0L);
            return false;
        }

        if (distance_scale <= 0) {
            logger.log(Level.SEVERE, "Invalid distance scale value:" + distance_scale, 0L);
            return false;
        }

        double A = 0.0, B = 0.0; // A and B represent height and distance scales respectively

        double Q1 = 0.0, Q2 = 0.0, Q3 = 0.0, Q4 = 0.0, Q5 = 0.0, a = 0.0, b = 0.0, c = 0.0;
        // Intermidiated variables

        double sin_degree_arm2, sin_degree_arm4, sin_degree_arm4_tmp1, sin_degree_arm4_tmp2,
                degree_arm2, degree_arm3, degree_arm4;

        A = height_scale;
        B = distance_scale;

        Q1 = Math.pow(A, 2) * Math.pow(Math.cos(arm2_positive_pos), 2) + ARM3_ARM2_PROPORTION_power2
                * Math.pow(A, 2) * Math.pow(Math.cos(arm4_positive_pos), 2) + Math.pow(B, 2)
                * Math.pow(Math.sin(arm2_positive_pos), 2) + Math.pow(B, 2) * ARM3_ARM2_PROPORTION_power2
                * Math.pow(Math.sin(arm4_positive_pos), 2) - 2 * ARM3_ARM2_PROPORTION * Math.pow(A, 2)
                * Math.cos(arm2_positive_pos) * Math.cos(arm4_positive_pos) + 2 * Math.pow(B, 2) * ARM3_ARM2_PROPORTION
                * Math.sin(arm2_positive_pos) * Math.sin(arm4_positive_pos);

        Q2 = 2 * ARM3_ARM2_PROPORTION * A * Math.cos(arm2_positive_pos) - 2 * ARM3_ARM2_PROPORTION_power2
                * A * Math.cos(arm4_positive_pos);

        Q3 = 2 * ARM3_ARM2_PROPORTION * B * Math.sin(arm2_positive_pos) + 2 * B * ARM3_ARM2_PROPORTION_power2
                * Math.sin(arm4_positive_pos);

        Q4 = (1 - ARM3_ARM2_PROPORTION_power2 - Q1) / Q2;

        Q5 = Q3 / Q2;

        a = Math.pow(Q5, 2) + 1;

        b = 2 * Q4 * Q5;

        c = Math.pow(Q4, 2) - 1;

        sin_degree_arm4_tmp1 = (0 - b + Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);

        sin_degree_arm4_tmp2 = (0 - b - Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);

        //check the value of tmp1 & 2, so that to decide the value of degree_arm4
        logger.log(Level.SEVERE, "arm4_tmp1 & tmp2: " + sin_degree_arm4_tmp1 + " : " + sin_degree_arm4_tmp2, 0L);

        //TODO: We don't have a good way yet to choose between tmp1 and 2 when they are both OK.
        if (0 <= sin_degree_arm4_tmp1 && sin_degree_arm4_tmp1 <= 1) {
            logger.log(Level.INFO, "arm4_tmp1 was chosen.");
            sin_degree_arm4 = sin_degree_arm4_tmp1;
        } else if (0 <= sin_degree_arm4_tmp2 && sin_degree_arm4_tmp2 <= 1) {
            logger.log(Level.INFO, "arm4_tmp2 was chosen.");
            sin_degree_arm4 = sin_degree_arm4_tmp2;
        } else {
            logger.log(Level.SEVERE, "Both arm4_POS are wrong:" + sin_degree_arm4_tmp1
                    + " : " + sin_degree_arm4_tmp2, 0L);
            return false;
        }

        //sin_degree_arm4 = sin_degree_arm4_tmp2;

        sin_degree_arm2 = B * Math.sin(arm2_positive_pos)
                + B * ARM3_ARM2_PROPORTION * Math.sin(arm4_positive_pos)
                - ARM3_ARM2_PROPORTION * sin_degree_arm4;

        degree_arm2 = Math.asin(sin_degree_arm2);
        degree_arm4 = Math.asin(sin_degree_arm4);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false) {
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true) {

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        } else {
            return false;
        }

    }
}
