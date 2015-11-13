/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.SMS.EE.Grip;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.SMS.EE.KFImpl;

import com.jmatio.types.MLDouble;
import com.jmatio.io.MatFileWriter;

import java.util.ArrayList;
import java.io.File;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import java.util.Random;


/**
 *
 * @author Daqi
 */
public class DescendKF extends KFImpl{

    private static final Logger logger = Logger.getLogger(DescendKF.class.getCanonicalName());

    //Input--motor commands
    private Map<String, Double> motorCommands_arms = new HashMap<String, Double>();

    //private Map<String, Double> motorCommands_arms_Last = new HashMap<String, Double>();

    //Input--measurements
    private Map<String, Double> measurements_arms = new HashMap<String, Double>();

    /*Output--estimated sensory data
    */
    private Map<String, Object> estimated_states = new HashMap<String, Object>();

    private final static double TOUCH_THRESHOLD = 0.005;

    private final static int MC_DELAY_RATE = 1; //'0' means no delay

    Queue<Map<String, Double>> mc_queue =new LinkedList<Map<String, Double>>();

    //The number of iterations
    private int monostable_time;

    private final static int TIME_UNIT = 1;

    // For output data
    private final static int OUTPUT_ARRAY_SIZE = 80;

    double[][] ZData = new double[OUTPUT_ARRAY_SIZE][3];

    double[][] XMINSData = new double[OUTPUT_ARRAY_SIZE][3];

    double[][] XData = new double[OUTPUT_ARRAY_SIZE][3];

    double[][] XDataXY = new double[OUTPUT_ARRAY_SIZE][2];

    double[][] XDataXYPos = new double[OUTPUT_ARRAY_SIZE][2];
    
    double[][] XDataXYReal = new double[OUTPUT_ARRAY_SIZE][2];
    
    double[][] XDataXYBias = new double[OUTPUT_ARRAY_SIZE][2];

    //For test of output
    double[][] PData = new double[OUTPUT_ARRAY_SIZE][1];
    
    double[][] KData = new double[OUTPUT_ARRAY_SIZE][1];
    
    double[][] XData_statemins = new double[OUTPUT_ARRAY_SIZE][2];

    private int array_index;
    
    private double Process_STD_Q, Measurement_STD_R;
    
    private Random Process_Noise_Generator, Measurement_Noise_Generator;


    public DescendKF(int ticksPerRun) {
        super(ticksPerRun);
    }
    @Override
    public void init() {

        //Input
        // the motor commands and measurements initially have same values
        // during the running time, motor commands are desired arm positions,
        // and measurements are sensed positions.
        motorCommands_arms.put("Arm2_pos", ARM2_INIT_POS);
        motorCommands_arms.put("Arm3_pos", ARM3_INIT_POS);
        motorCommands_arms.put("Arm4_pos", ARM4_INIT_POS);

        measurements_arms.put("arm2_pos", ARM2_INIT_POS);
        measurements_arms.put("arm3_pos", ARM3_INIT_POS);
        measurements_arms.put("arm4_pos", ARM4_INIT_POS);

        //Output
        estimated_states.put("arm2_pos", ARM2_INIT_POS);
        estimated_states.put("arm3_pos", ARM3_INIT_POS);
        estimated_states.put("arm4_pos", ARM4_INIT_POS);
//        estimated_states.put("hand_X_pos", FIXED_BASE_DISTANCE + Distance (ARM2_INIT_POS, ARM4_INIT_POS));
//        estimated_states.put("hand_Y_pos", FIXED_BASE_HEIGHT + Height (ARM2_INIT_POS, ARM4_INIT_POS) -ARM4_LENGTH);
        estimated_states.put("hand_X_pos", Distance (ARM2_INIT_POS, ARM4_INIT_POS));
        estimated_states.put("hand_Y_pos", Height (ARM2_INIT_POS, ARM4_INIT_POS));

        estimated_states.put("toucher1_active", false);
        estimated_states.put("toucher2_active", false);

        //Parameters
        //Process_Variance_Q = 3.8e-5; // default is 1e-5
        Process_Variance_Q = 3e-5;
        //Process_STD_Q = Math.sqrt(Process_Variance_Q);
        Process_STD_Q = 0;
        //Measurement_Variance_R = 1.32e-4; // default is 0.1
        Measurement_Variance_R = 0.01;
        //Measurement_STD_R = Math.sqrt(Measurement_Variance_R);
        Measurement_STD_R = 0;
        
        Process_Noise_Generator = new Random();
        Measurement_Noise_Generator = new Random();
        
        Estimation_Noise_P = 1e-5;

        Parameter_A = 1.0; //Same to arm2 ~ arm4
        Parameter_B = 1.4; //Same to arm2 ~ arm4
        Parameter_H = 1.0; //Same to arm2 ~ arm4

        KFGain_K = 0.0;

        monostable_time = 0;

        array_index = 0;
    }
    @Override
    public void recieveMotorCommand(Object o) {
        Map<String, Object> motorCommands = (HashMap) o;

        if (motorCommands != null){
            Object arm2_p = motorCommands.get("Arm2_pos");
            Object arm3_p = motorCommands.get("Arm3_pos");
            Object arm4_p = motorCommands.get("Arm4_pos");

            if (arm2_p != null){
                motorCommands_arms.put("Arm2_pos", (Double)arm2_p);
            } else{
            }

            if (arm3_p != null){
                //System.out.println("The arm3_pos is SET as:" + arm3_pos);
                motorCommands_arms.put("Arm3_pos", (Double)arm3_p);
            } else{
                //System.out.println("The arm3_pos is NOT set value!");
            }

            if (arm4_p != null){
                motorCommands_arms.put("Arm4_pos", (Double)arm4_p);
            } else{
            }

        } else{
            logger.log(Level.INFO, "recieved motor commands are null!");
        }

    }

    @Override
    public void recieveMeasturement(Object o) {
        Map<String, Double> data = (HashMap) o;

        measurements_arms.put("arm2_pos", data.get("arm2_pos"));
        measurements_arms.put("arm3_pos", data.get("arm3_pos"));
        measurements_arms.put("arm4_pos", data.get("arm4_pos"));
    }

    @Override
    public Object estimatedSensoryData() {
        //TODO

        return estimated_states;
    }

    @Override
    /*
     * New version of execute() --> Apply KF on the rotational position
     * and may represent by linear position
     */
    public void execute() {

        double motorCommand_Arm2_pos = ARM2_INIT_POS,
                motorCommand_Arm3_pos = ARM3_INIT_POS,
                motorCommand_Arm4_pos = ARM4_INIT_POS,
                motorCommand_Arm2_pos_last = ARM2_INIT_POS,
                motorCommand_Arm3_pos_last = ARM3_INIT_POS,
                motorCommand_Arm4_pos_last = ARM4_INIT_POS;

        double hand_X_bais = 0.0, hand_Y_bais = 0.0;

        double hand_X_state = 0.0, hand_Y_state = 0.0;

        double hand_X_stateMins = 0.0, hand_Y_stateMins = 0.0;

        double measurements_arm2_pos = 0.0,
                measurements_arm3_pos = 0.0,
                measurements_arm4_pos = 0.0;

        double arm2_bais = 0.0, arm3_bais = 0.0, arm4_bais = 0.0;

        double arm2_state = 0.0, arm3_state = 0.0, arm4_state = 0.0;

        double arm2_stateMins = 0.0, arm3_stateMins = 0.0, arm4_stateMins = 0.0;

        double Pmins = 0.0;

        double handState_X_dim = 0.0, handState_Y_dim = 0.0;
        
        double hand_X_real = 0.0, hand_Y_real = 0.0;
       
        // Retrieve the measurements
        measurements_arm2_pos = measurements_arms.get("arm2_pos");
        measurements_arm3_pos = measurements_arms.get("arm3_pos");
        measurements_arm4_pos = measurements_arms.get("arm4_pos");

        //Retrieve the motor commands
        Map<String, Double> MC_tmp, MC_last_tmp;

        Map<String, Double> tmp_mcQueue_content = new HashMap<String, Double>();

        int size_tmp;

        //Put the current MC into the queue
        //System.out.println("The cm_queue before add = " + mc_queue);

        //Manually make a clone of motorCommands_arms
        tmp_mcQueue_content.put("Arm2_pos", motorCommands_arms.get("Arm2_pos"));
        tmp_mcQueue_content.put("Arm3_pos", motorCommands_arms.get("Arm3_pos"));
        tmp_mcQueue_content.put("Arm4_pos", motorCommands_arms.get("Arm4_pos"));

//        //Put the X-Y position prediction factors into the queue as well,
//        //so that we can predict the position directly based on the forward model
//        tmp_mcQueue_content.put("Hand_X_pos", Distance(motorCommands_arms.get("Arm2_pos"),
//                motorCommands_arms.get("Arm4_pos")));
//        tmp_mcQueue_content.put("Hand_Y_pos", Height(motorCommands_arms.get("Arm2_pos"),
//                motorCommands_arms.get("Arm4_pos")));
        
        mc_queue.add(tmp_mcQueue_content);

        //System.out.println("The cm_queue after add = " + mc_queue);

        size_tmp = mc_queue.size();

        if (size_tmp < MC_DELAY_RATE + 1){
            //System.out.println("The MC queue's size is ZERO!!!!!!");
            //Do nothing

        } else if (size_tmp == MC_DELAY_RATE + 1){
            //System.out.println("The MC queue's size is ONE");
            //only has current MC but not past one
            MC_tmp = mc_queue.peek(); // take the MC for the current but not remove it

            motorCommand_Arm2_pos = MC_tmp.get("Arm2_pos");
            motorCommand_Arm3_pos = MC_tmp.get("Arm3_pos");
            motorCommand_Arm4_pos = MC_tmp.get("Arm4_pos");

        } else if (size_tmp == MC_DELAY_RATE + 2){
            //System.out.println("The MC queue's size is TWO");
            //Being have both current and past MCs
            MC_last_tmp = mc_queue.remove(); // take and remove the MC for the last MC
            MC_tmp = mc_queue.peek(); // take the MC for the current but not remove it

            motorCommand_Arm2_pos = MC_tmp.get("Arm2_pos");
            motorCommand_Arm3_pos = MC_tmp.get("Arm3_pos");
            motorCommand_Arm4_pos = MC_tmp.get("Arm4_pos");

            motorCommand_Arm2_pos_last = MC_last_tmp.get("Arm2_pos");
            motorCommand_Arm3_pos_last = MC_last_tmp.get("Arm3_pos");
            motorCommand_Arm4_pos_last = MC_last_tmp.get("Arm4_pos");

        } else {//mc_queue.size() > MC_DELAY_RATE + 2
            logger.log(Level.INFO, "Motor commands queue overflow!");
        }

        //0. Preporcessing
        //0.1 Get the update of motor commands (u)
        arm2_bais = motorCommand_Arm2_pos - motorCommand_Arm2_pos_last;
        arm3_bais = motorCommand_Arm3_pos - motorCommand_Arm3_pos_last;
        arm4_bais = motorCommand_Arm4_pos - motorCommand_Arm4_pos_last;

        hand_X_bais = Distance(motorCommand_Arm2_pos, motorCommand_Arm4_pos) -
                Distance(motorCommand_Arm2_pos_last, motorCommand_Arm4_pos_last);

        hand_Y_bais = Height(motorCommand_Arm2_pos, motorCommand_Arm4_pos) -
                Height(motorCommand_Arm2_pos_last, motorCommand_Arm4_pos_last);
        
        if (hand_Y_bais == 0.0){//Only execute when a new motor command comes
            return;
        }

        //System.out.println("The arm2_bais is: " + arm2_bais);

        //0.2 Get the previous estimated states
        arm2_state = (Double) estimated_states.get("arm2_pos");
        arm3_state = (Double) estimated_states.get("arm3_pos");
        arm4_state = (Double) estimated_states.get("arm4_pos");

        hand_X_state = (Double)estimated_states.get("hand_X_pos");
        hand_Y_state = (Double)estimated_states.get("hand_Y_pos");

        //1. Prediction (time update): Xmins = A*X(k-1) + B*u
        // 1.1 To predict: Xmins = A*X(k-1) + B*u
        
        //The system dynamics of the gripper were approximated by a damped point
        //Parameter_B = Parameter_B/(monostable_time + 1);
        
        arm2_stateMins = Parameter_A * arm2_state + Parameter_B * arm2_bais;
        arm3_stateMins = Parameter_A * arm3_state + Parameter_B * arm3_bais;
        arm4_stateMins = Parameter_A * arm4_state + Parameter_B * arm4_bais;

        hand_X_stateMins = Parameter_A * hand_X_state + Parameter_B * hand_X_bais;
        
        //We only add noise to Y-dim because we only examine it in the paper
        hand_Y_stateMins = Parameter_A * hand_Y_state + Parameter_B * hand_Y_bais 
                + Process_STD_Q*Process_Noise_Generator.nextGaussian();
        
        System.out.println("hand_Y_state, hand_Y_bais, and hand_Y_stateMins are: " 
                + hand_Y_state + ", "
                + hand_Y_bais + ", "
                + hand_Y_stateMins);

        // 1.2 To upadte the P: Pmins = A*P(k-1) + Q
        Pmins = Parameter_A * Estimation_Noise_P + Process_Variance_Q;

        //2. Correction (measurement update)
        // 2.1 To update the filter's gain K: K = Pmins / (Pmins + R)
        KFGain_K = Pmins / (Pmins + Measurement_Variance_R);

        // 2.2 To update the X: X = Xmins + K (Z - Xmins)
        arm2_state = arm2_stateMins + KFGain_K * (measurements_arm2_pos - arm2_stateMins);
        arm3_state = arm3_stateMins + KFGain_K * (measurements_arm3_pos - arm3_stateMins);
        arm4_state = arm4_stateMins + KFGain_K * (measurements_arm4_pos - arm4_stateMins);

        hand_X_state = hand_X_stateMins + 
                KFGain_K * (Distance(measurements_arm2_pos, measurements_arm4_pos) - hand_X_stateMins);

        //We only add noise to Y-dim because we only examine it in the paper
        hand_Y_state = hand_Y_stateMins + 
                KFGain_K * (Height(measurements_arm2_pos, measurements_arm4_pos)
                + Measurement_STD_R*Measurement_Noise_Generator.nextGaussian() 
                - hand_Y_stateMins);

        // 2.3 To update the P = (1 - K) * Pmins
        Estimation_Noise_P = (1 - KFGain_K) * Pmins;

        //Reflect the current state back to estimated variables
        estimated_states.put("arm2_pos", arm2_state);
        estimated_states.put("arm3_pos", arm3_state);
        estimated_states.put("arm4_pos", arm4_state);

        estimated_states.put("hand_X_pos", hand_X_state);
        estimated_states.put("hand_Y_pos", hand_Y_state);

        //System.out.println("hand_X_state: " + hand_X_state);
        //System.out.println("hand_Y_state: " + hand_Y_state);

        //Create a (X-Y) representation about the hand position

        //Current X-dim (Distance) = FIXED_BASE_DISTANCE + D1
        handState_X_dim = FIXED_BASE_DISTANCE + Distance(arm2_state, arm4_state);


        /*Current Y-dim (Height) = FIXED_BASE_HEIGHT + H2
        /* H2 = H1 - 0.218 (The length of arm4)
        /* H1 = The value of "Length of arm 2 * cosine(Degree of arm2) - Length of arm3 * cosine(Degree of arm4)"
         */
        handState_Y_dim = FIXED_BASE_HEIGHT + Height(arm2_state, arm4_state) - ARM4_LENGTH;
        
        /*
        * change the position state to absolute values
        */
        hand_X_state = FIXED_BASE_DISTANCE + hand_X_state;
        hand_Y_state = FIXED_BASE_HEIGHT + hand_Y_state - ARM4_LENGTH;
        
        /*
        /* The real gripper's position (X-Y)
        TODO: The real location should not be measured data!!! but the real (ideal) data without noise!!
        */
        hand_X_real = FIXED_BASE_DISTANCE + Distance(measurements_arm2_pos, measurements_arm4_pos);
        
        hand_Y_real = FIXED_BASE_HEIGHT + Height(measurements_arm2_pos, measurements_arm4_pos) - ARM4_LENGTH;


        // To check and set touch sensor on if the hand is almost touch the table
        if (handState_Y_dim - TABLE_SURFACE_HEIGHT > TOUCH_THRESHOLD){
            estimated_states.put("toucher1_active", false);
            estimated_states.put("toucher2_active", false);
        }else{
            estimated_states.put("toucher1_active", true);
            estimated_states.put("toucher2_active", true);
        }

        /*
         * Output the estimated and its relevant data for study
         *
         */

        if (monostable_time % TIME_UNIT == 0) {

            if (array_index < OUTPUT_ARRAY_SIZE){
                System.out.println("array_index is " + array_index);

                //output the measurements
                ZData[array_index][0] = measurements_arm2_pos;
                ZData[array_index][1] = measurements_arm3_pos;
                ZData[array_index][2] = measurements_arm4_pos;

                //output the prior (desired) sensory data
                XMINSData[array_index][0] = arm2_stateMins;
                XMINSData[array_index][1] = arm3_stateMins;
                XMINSData[array_index][2] = arm4_stateMins;

                //output the estimated sensory data
                XData[array_index][0] = arm2_state;
                XData[array_index][1] = arm3_state;
                XData[array_index][2] = arm4_state;

                XDataXY[array_index][0] = handState_X_dim;
                XDataXY[array_index][1] = handState_Y_dim;

                XDataXYPos[array_index][0] = hand_X_state;
                XDataXYPos[array_index][1] = hand_Y_state;
                
                XDataXYReal[array_index][0] = hand_X_real;
                XDataXYReal[array_index][1] = hand_Y_real;
                
                XDataXYBias[array_index][0] = hand_X_real - hand_X_state;
                XDataXYBias[array_index][1] = hand_Y_real - hand_Y_state;

                //Pmins
                PData[array_index][0] = Pmins;
                
                KData [array_index][0] = KFGain_K;

                XData_statemins[array_index][0] = hand_X_stateMins;
                XData_statemins[array_index][1] = hand_Y_stateMins;

            } else if (array_index == OUTPUT_ARRAY_SIZE){
                System.out.println("Writing the data ...");

                //1-3 Save the measurements data with noise
                MLDouble mlDouble1 = new MLDouble("pos_measurementsNoise", ZData);

                ArrayList list1 = new ArrayList();

                list1.add(mlDouble1);

                try{
                    new MatFileWriter(".\\_data\\pos_measurementsNoise.mat", list1);
                }
                catch(IOException e)
                {
                    System.out.println("pos_measurementsNoise IOError");
                }

                //1-3b Save the Pmins data
                MLDouble mlDouble1b = new MLDouble("Pmins", PData);

                ArrayList list1b = new ArrayList();

                list1b.add(mlDouble1b);

                try{
                    new MatFileWriter(".\\_data\\Pmins.mat", list1b);
                }
                catch(IOException e)
                {
                    System.out.println("Pmins IOError");
                }


                //2-3 Save the prior data
                MLDouble mlDouble2 = new MLDouble("pos_prior", XMINSData);

                ArrayList list2 = new ArrayList();

                list2.add(mlDouble2);

                try{
                    new MatFileWriter(".\\_data\\pos_prior.mat", list2);
                }
                catch(IOException e)
                {
                    System.out.println("pos_prior IOError");
                }

                //3-3 Save the estimated data
                MLDouble mlDouble3 = new MLDouble("pos_estimation", XData);

                ArrayList list3 = new ArrayList();

                list3.add(mlDouble3);

                try{
                    new MatFileWriter(".\\_data\\pos_estimation.mat", list3);
                }
                catch(IOException e)
                {
                    System.out.println("pos_estimation IOError");
                }

                //3-3b Save the estimated data in (X-Y) format
                MLDouble mlDouble3b = new MLDouble("posXY_estimation", XDataXY);

                ArrayList list3b = new ArrayList();

                list3b.add(mlDouble3b);

                try{
                    new MatFileWriter(".\\_data\\posXY_estimation.mat", list3b);
                }
                catch(IOException e)
                {
                    System.out.println("posXY_estimation IOError");
                }

                //3-3c Save the estimated data in (X-Y) format
                //with different prediction (by X-Y position but not rotation)
                MLDouble mlDouble3c = new MLDouble("posXYPos_estimation", XDataXYPos);

                ArrayList list3c = new ArrayList();

                list3c.add(mlDouble3c);

                try{
                    new MatFileWriter(".\\_data\\posXYPos_estimation.mat", list3c);
                }
                catch(IOException e)
                {
                    System.out.println("posXY2_estimation IOError");
                }
                
                //3-3d Save the real gripper's position in (X-Y) format
                MLDouble mlDouble3d = new MLDouble("posXYReal", XDataXYReal);

                ArrayList list3d = new ArrayList();

                list3d.add(mlDouble3d);

                try{
                    new MatFileWriter(".\\_data\\posXYReal.mat", list3d);
                }
                catch(IOException e)
                {
                    System.out.println("posXYReal IOError");
                }
                
                //3-3e Save the bias between real and estimated gripper's position in (X-Y) format
                MLDouble mlDouble3e = new MLDouble("posXYBias", XDataXYBias);

                ArrayList list3e = new ArrayList();

                list3e.add(mlDouble3e);

                try{
                    new MatFileWriter(".\\_data\\posXYBias.mat", list3e);
                }
                catch(IOException e)
                {
                    System.out.println("posXYBias IOError");
                }
                
                //4a Test data: State_mins
                MLDouble mlDouble4a = new MLDouble("posXY_stateMins", XData_statemins);

                ArrayList list4a = new ArrayList();

                list4a.add(mlDouble4a);

                try{
                    new MatFileWriter(".\\_data\\XData_statemins.mat", list4a);
                }
                catch(IOException e)
                {
                    System.out.println("XData_statemins IOError");
                }
                
                //4b Test data: State_mins
                MLDouble mlDouble4b = new MLDouble("KData", KData );

                ArrayList list4b = new ArrayList();

                list4b.add(mlDouble4b);

                try{
                    new MatFileWriter(".\\_data\\KData.mat", list4b);
                }
                catch(IOException e)
                {
                    System.out.println("KData IOError");
                }

                System.out.println("Writing is finished!");
                

            } else{
                // do nothing
            }

            array_index = array_index + 1;

        }

        monostable_time = monostable_time + 1;

    }

    //D1 = the value of "Length of arm 2 * sine(Degree of arm2) + Length of arm3 * sine(Degree of arm4)"
    private double Distance(double arm2_pos, double arm4_pos){
        return ARM2_LENGTH * Math.sin(Math.abs(arm2_pos)) +
                    ARM3_LENGTH * Math.sin(Math.abs(arm4_pos));
    }

    //H1 = The value of "Length of arm 2 * cosine(Degree of arm2) - Length of arm3 * cosine(Degree of arm4)"
    private double Height(double arm2_pos, double arm4_pos){
        return ARM2_LENGTH * Math.cos(Math.abs(arm2_pos)) -
                    ARM3_LENGTH * Math.cos(Math.abs(arm4_pos));
    }

}
