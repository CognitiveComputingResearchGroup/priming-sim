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

public abstract class ArmsFSMImpl extends FSMImpl implements ArmsFSM{

    private static final Logger logger = Logger.getLogger(ArmsFSMImpl.class.getCanonicalName());

    protected static final double arm2_initPos = -0.18;
    protected static final double arm3_initPos = -1.19;
    protected static final double arm4_initPos = -1.77;

    protected static final double APPROX_EQUAL_MIN_VAL = 0.0001;

    protected static final double APPROX_EQUAL_MAX_VAL = 0.02;
    // This is the MAX space used to check sum pf arms position is almost equal to Math.PI

    private static final double APPROX_EQUAL_MAX_VAL2 = 0.03;
    // This is used to check arms positions limitation

    private static final double ARM2_MIN_POS = -1.57;
    private static final double ARM2_MAX_POS = 1.57;

    private static final double ARM3_MIN_POS = -2.64;
    private static final double ARM3_MAX_POS = 2.55;

    private static final double ARM4_MIN_POS = -1.78;
    private static final double ARM4_MAX_POS = 1.78;

    private static final double ATOMIC_SPACE = 0.01;
    // Its value is negative because the hand is in the opposite side of home

    private static final double NORMAL_SPCAE = ATOMIC_SPACE * 4;

    private static final double SLIGHT_SPACE = ATOMIC_SPACE;

    private static final double SLIGHT_DOWNUP_PROPORTION = 0.95;
    

    private static final double ARM3_ARM2_PROPORTION = 0.871; //an older value '0.895' calculated manually
    // The real exact value of arm3/arm2 ==> arm3 == 135mm, arm2 == 155 mm
    // (http://www.youbot-store.com/youbot-developers/software/ros/youbot-inverse-kinematics?c=43)

    protected double arm2_pos, arm3_pos, arm4_pos, arm2_updated_pos, arm3_updated_pos, arm4_updated_pos;

    public ArmsFSMImpl(int ticksPerRun) {
        super(ticksPerRun);
    }
    
    @Override
    public void init(){
    System.out.println("movingHand starts...");

        arm2_pos = 0.0;

        arm3_pos = 0.0;

        arm4_pos = 0.0;

        arm2_updated_pos = 0.0;

        arm3_updated_pos = 0.0;

        arm4_updated_pos = 0.0;

        commands.put("Arm2_pos", null);
        commands.put("Arm3_pos", null);
        commands.put("Arm4_pos", null);
        
    }

    // Input the current arms positions
    @Override
    public void recieveArmsPositions(double arm2, double arm3, double arm4) {

        arm2_pos = arm2;
        arm3_pos = arm3;
        arm4_pos = arm4;

    }

    //descend
    protected boolean downward(){

        return downward(NORMAL_SPCAE);
    }
    
    protected boolean downward(double space){

        double sin_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        boolean Sign_flag = false;
        //This Sign_flag is used to indicate the sign of arm position
        // Sign_flag == true ==> Positive
        // Sign_flag == false ==> Negative
        //Note that, because we assume arms are always in the opposite side of home, so
        //arms psositions are always negative. Therefore, Sign_flag <-- flase


        // In this process, we assume all arms position are positive,
        // and assign proper sign to them at end.
        
        //First step --> Degree of arm4 decreases
        degree_arm4 = Math.abs(arm4_pos) - space;

        //The value of "Length of arm 2 * sine(Degree of arm2) + Length of arm3 * sine(Degree of arm4)" is constant
        sin_degree_arm2 = Math.sin(Math.abs(arm2_pos)) + ARM3_ARM2_PROPORTION * (Math.sin(Math.abs(arm4_pos)) - Math.sin(degree_arm4));
        degree_arm2 = Math.asin(sin_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false){
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true){
            
            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        }else{
            return false;
        }

    }


    //lift
    protected boolean upward(){

        return upward(NORMAL_SPCAE);
    }

    protected boolean upward(double space){
        // This upward was implemented very similar to the downward; The only
        // difference is the arm4 position instead increases.

        double sin_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        boolean Sign_flag = false;

        //First step --> Degree of arm4 increase
        degree_arm4 = Math.abs(arm4_pos) + space;

        //The value of "Length of arm 2 * sine(Degree of arm2) + Length of arm3 * sine(Degree of arm4)" is constant
        sin_degree_arm2 = Math.sin(Math.abs(arm2_pos)) + ARM3_ARM2_PROPORTION * (Math.sin(Math.abs(arm4_pos)) - Math.sin(degree_arm4));
        degree_arm2 = Math.asin(sin_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false){
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true){

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        }else{
            return false;
        }

    }
    

    //extend
    protected boolean forward(){

        return forward(NORMAL_SPCAE);
    }

    protected boolean forward(double space){
        double cos_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        boolean Sign_flag = false;
        // This upward was implemented very similar to the downward;
        // The difference are as below:
        // 1) the arm4 position instead increases.
        // 2) Arms position calculating formula is a little bit different:
        // 2-1) sine --> cosine
        // 2-2) Regarding the two major parts in the formula, Operation Sum --> Operation Sbustration

        //First step --> Degree of arm4 increases
        degree_arm4 = Math.abs(arm4_pos) + space;

        //The value of "Length of arm 2 * cosine(Degree of arm2) - Length of arm3 * cosine(Degree of arm4)" is constant
        cos_degree_arm2 = Math.cos(Math.abs(arm2_pos)) - ARM3_ARM2_PROPORTION * (Math.cos(Math.abs(arm4_pos)) - Math.cos(degree_arm4));
        degree_arm2 = Math.acos(cos_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false){
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true){

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        }else{
            return false;
        }

    }

    //back
    protected boolean backward(){

        return backward(NORMAL_SPCAE);
    }

    protected boolean backward(double space){
        // :: First step --> Degree of arm4 decreases
        // ::Constraints:
        // 1) Degree of arm2 + arm3 + arm4 == PI
        // 2) The value of "Length of arm 2 * cosine(Degree of arm2) - Length of arm3 * cosine(Degree of arm4)" is constant

        return true;
    }


    //extend and slightly downward
    //back
    protected boolean forward_down(){

        return forward_down(SLIGHT_SPACE, SLIGHT_DOWNUP_PROPORTION);
    }
    
    protected boolean forward_down(double space, double prportion){

        double cos_degree_arm2, degree_arm2, degree_arm3, degree_arm4;

        boolean Sign_flag = false;
        // This forward_down() is similar to forward(), except:
        // D' != D --> D' == prportion * D, and prportion < 1 becase the behavior 'down'

        //First step --> Degree of arm4 increases
        degree_arm4 = Math.abs(arm4_pos) + space;

        //The value of "Length of arm 2 * cosine(Degree of arm2) - Length of arm3 * cosine(Degree of arm4)" decreases with the rate of 'prportion'
        cos_degree_arm2 = prportion * Math.cos(Math.abs(arm2_pos)) - ARM3_ARM2_PROPORTION * (prportion * Math.cos(Math.abs(arm4_pos)) - Math.cos(degree_arm4));
        degree_arm2 = Math.acos(cos_degree_arm2);

        //Degree of arm2 + arm3 + arm4 == PI
        degree_arm3 = Math.PI - degree_arm2 - degree_arm4;

        if (Sign_flag == false){
            degree_arm2 = 0 - degree_arm2;
            degree_arm3 = 0 - degree_arm3;
            degree_arm4 = 0 - degree_arm4;
        }
        if (arms_are_valid(degree_arm2, degree_arm3, degree_arm4) == true){

            arm2_updated_pos = degree_arm2;
            arm3_updated_pos = degree_arm3;
            arm4_updated_pos = degree_arm4;

            return true;
        }else{
            return false;
        }
    }

    //go back and slightly upward
    protected boolean backward_rise() {

        boolean result = true;

        //1) Go back first with normal step space
        result = backward();

        if (result == false) {
            return false;
        }

        // Update the current Arms Positions
        arm2_pos = arm2_updated_pos;
        arm3_pos = arm3_updated_pos;
        arm4_pos = arm4_updated_pos;

        //2) Slightly rise
        result = upward(SLIGHT_SPACE);

        return result;
    }

    //go back and slightly downward
    protected boolean backward_down() {

        boolean result = true;

        //1) Go back first with normal step space
        result = backward();

        if (result == false) {
            return false;
        }

        // Update the current Arms Positions
        arm2_pos = arm2_updated_pos;
        arm3_pos = arm3_updated_pos;
        arm4_pos = arm4_updated_pos;

        //2) Slightly go down
        result = downward(SLIGHT_SPACE);

        return result;

    }

    //Valid checking of input arms position
    protected boolean arms_are_valid(double arm2, double arm3, double arm4){

    boolean arms_are_valid = true;

    double sum_pos = 0.0;

    // 1. General requirements for the arms
    // --> Within the limitations && is a number

    // 2. Special requirements for this youbot robot
    //Arms are supposed to work on the opposite side of home, so arm2 position value should not be positive.
    // Only the Elbow UP is acceptable (Elbow Down is not), so arm3 and arm 4 should not be positive

    sum_pos = arm2 + arm3 + arm4;

    if (Math.abs(Math.abs(sum_pos) - Math.PI) > APPROX_EQUAL_MAX_VAL){
        arms_are_valid = false;
        System.out.println("Invalid sum position:" + sum_pos);
    }

    if ( ((arm2 > ARM2_MAX_POS + APPROX_EQUAL_MAX_VAL2) ||
            (arm2 < ARM2_MIN_POS - APPROX_EQUAL_MAX_VAL2))|| Double.isNaN(arm2) ||
            (arm2 > 0.0)){
        arms_are_valid = false;
        //logger.log(Level.SEVERE, "Invalid Arm2 position.");
        System.out.println("Invalid Arm2 position:" + arm2);
    }

    
    if ( ((arm3 > ARM3_MAX_POS + APPROX_EQUAL_MAX_VAL2) ||
            (arm3 < ARM3_MIN_POS - APPROX_EQUAL_MAX_VAL2))|| Double.isNaN(arm3) ||
            (arm3 > 0.0)){
        arms_are_valid = false;
        //logger.log(Level.SEVERE, "Invalid Arm3 position.");
        System.out.println("Invalid Arm3 position:" + arm3);
    }

    if ( ((arm4 > ARM4_MAX_POS + APPROX_EQUAL_MAX_VAL2) || 
            (arm4 < ARM4_MIN_POS - APPROX_EQUAL_MAX_VAL2))|| Double.isNaN(arm4) ||
            (arm4 > 0.0)){
        arms_are_valid = false;
        //logger.log(Level.SEVERE, "Invalid Arm4 position.");
        System.out.println("Invalid Arm4 position:" + arm4);
    }

    return arms_are_valid;
    }

}
