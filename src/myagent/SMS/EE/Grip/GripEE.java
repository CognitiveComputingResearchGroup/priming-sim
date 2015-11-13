/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.SMS.EE.Grip;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import myagent.SMS.EE.EEImpl;
import myagent.SMS.EE.KF;

/**
 * GripEE is a concrete environment emulator for Grip action. It contains one or
 * more specific skills (currently implemented by the Kalman filter (KF))
 * @author Daqi
 */
public class GripEE extends EEImpl{
    private static final Logger logger = Logger.getLogger(GripEE.class.getCanonicalName());

    private final int KF_TICKS_PER_RUN = 1;

    /*
     * Input Data
     * The motor commands to the arm is reprented by the desired arm rotation positions.
     * Note that, although theortically the motor commands should be the kind of force,
     * the Webots supports a built-in computational LIB that translates between arm's
     * rotation position and forces. So that we consider the arm rotations 
     * a "special" force and use it as input motor commands.
     * Spcifically, only arm's 2, 3, and 4 joints rotation are used to represent
     * the hand's position
    */
    private Map<String, Object> motorCommands_arms = new HashMap<String, Object>();
    //private double motorCommands_arm2, motorCommands_arm3, motorCommands_arm4;

    /*Input-Data
     * Similar to above motor commands, the measurements (sensory data) are represented
     * by the arm's rotation position.
     *
     */
    private Map<String, Object> measurements_arms = new HashMap<String, Object>();
    //private double measurements_arm2, measurements_arm3, measurements_arm4;

    // This hand position is represented in a 2D dimension (X-Y)
    private Map<String, Object> estimated_hand_pos = new HashMap<String, Object>();

    private KF handDescendKF;

    @Override
    public void init() {
        //Input
        motorCommands_arms.put("Arm2_pos", null);
        motorCommands_arms.put("Arm3_pos", null);
        motorCommands_arms.put("Arm4_pos", null);

        measurements_arms.put("arm2_pos", null);
        measurements_arms.put("arm3_pos", null);
        measurements_arms.put("arm4_pos", null);

        //Output
        estimated_hand_pos.put("arm2_pos", null);
        estimated_hand_pos.put("arm3_pos", null);
        estimated_hand_pos.put("arm4_pos", null);

        handDescendKF = new DescendKF(KF_TICKS_PER_RUN);
        handDescendKF.init();
       
    }

    @Override
    public void recieveMeasurements(Object o){
        Map<String, Object> data = (HashMap) o;

        measurements_arms.put("arm2_pos", data.get("arm2_pos"));
        measurements_arms.put("arm3_pos", data.get("arm3_pos"));
        measurements_arms.put("arm4_pos", data.get("arm4_pos"));

        handDescendKF.recieveMeasturement(measurements_arms);
    }

    @Override
    public void recieveMotorCommands(Object o){

        Map<String, Object> motorCommands = (HashMap) o;

        motorCommands_arms.put("Arm2_pos", motorCommands.get("Arm2_pos"));
        motorCommands_arms.put("Arm3_pos", motorCommands.get("Arm3_pos"));
        motorCommands_arms.put("Arm4_pos", motorCommands.get("Arm4_pos"));

        handDescendKF.recieveMotorCommand(motorCommands_arms);
        
    }

    @Override
    public void estimation(){
        logger.log(Level.INFO, "GripEE starts...");

        /*Currently we just test each estimation KF separately.
        /*Later on, all of KFs should be combine together by a subsumption architecture,
        /*so that only one KF's output will be the EE's output at a time
         *
         */

        taskSpawner.addTask(handDescendKF);
        
    }

    @Override
    public Object estSensoryData(){

        /*Because we don't use a task to send out the output, so we cannot put
         * this retrieving output data at estimation() method, but in this real
         * output method.
         */
//        Map<String, Object> data = (HashMap)handDescendKF.estimatedSensoryData();
//        estimated_hand_pos.put("arm2_pos", data.get("arm2_pos"));
//        estimated_hand_pos.put("arm3_pos", data.get("arm3_pos"));
//        estimated_hand_pos.put("arm4_pos", data.get("arm4_pos"));
//
//        return estimated_hand_pos;
        return handDescendKF.estimatedSensoryData();

    }

}
