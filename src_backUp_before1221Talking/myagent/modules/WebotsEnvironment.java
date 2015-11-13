/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/

package myagent.modules;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

import com.cyberbotics.webots.controller.Robot;

import com.cyberbotics.webots.controller.DistanceSensor;
import com.cyberbotics.webots.controller.Servo;
import com.cyberbotics.webots.controller.TouchSensor;

import java.util.HashMap;


public class WebotsEnvironment extends EnvironmentImpl {

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(WebotsEnvironment.class.getCanonicalName());
    private BackgroundTask backgroundTask;
    private final int DEFAULT_TICKS_PER_RUN = 100;
    private int ticksPerRun = DEFAULT_TICKS_PER_RUN;

    //Environment GUI size (used in WebotsEnvironmentPanel.java)
    public static final int ENVIRONMENT_WIDTH = 100;
    public static final int ENVIRONMENT_HEIGHT = 100;

    //General parameters
    public static final double DEFAULT_UNKNOWN_DATA = -1.0;

    //Store the sensed data
    private Map<String, Object> sensedData = new HashMap<String, Object>();

    //Handler of robot node
    private Robot agent;
    
    private DistanceSensor distance_beam, distance_leftIR, distance_rightIR;

    private TouchSensor toucher1, toucher2;

    //private double distanceVal, servoPos;

    private Servo gripper1, gripper2, arm1, arm2, arm3, arm4, arm5;

    private static final int TIME_STEP = 32; // milliseconds

    private static final double GRIPPER_MIN_POS = 0.0;
    private static final double GRIPPER_MAX_POS = 0.025;

    private static final double GRIP_DISTANCE = 500.0;

    private static final double Distance_TolerationRate = 0.02;

    private static final double BEAM_MAX_DIS = 1000;

    private static final double GRIPPER_MAX_FORCE = 4.9; // Phsical MaxForce is set to 5.0 in Webots

    private static final double WRIST_FORCE_THRESHOLD = 0.25;
    //This wrist force threshold relates to the value of the gripper max force

    private static final double TOUCH_THRESHOLD = 0.0;
    // No need to consider noise because the touch sensor type is dumper, which only support 0.0 or 1.0

    private static final double IR_CROSSED_AREA_UPPERBOUND_DISTANCE = 564.0;
    private static final double IR_CROSSED_AREA_LOWERBOUND_DISTANCE = 438.0;

    private double left_gripper_pos, right_gripper_pos, arm1_pos, arm2_pos, arm3_pos, arm4_pos,
            arm5_pos, wheel1_base_pos, wheel2_base_pos, wheel3_base_pos, wheel4_base_pos;

    private double left_gripper_pos_last = 0.0,
    right_gripper_pos_last = 0.0,
    arm1_pos_last = 0.0,
    arm2_pos_last = 0.0,
    arm3_pos_last = 0.0,
    arm4_pos_last = 0.0,
    arm5_pos_last = 0.0,
    wheel1_base_pos_last = 0.0,
    wheel2_base_pos_last = 0.0,
    wheel3_base_pos_last = 0.0;



    @Override
    public void init() {
        //For Webots' init
        agent = new Robot();

        distance_beam = agent.getDistanceSensor("beam");

        distance_beam.enable(TIME_STEP);

        distance_leftIR = agent.getDistanceSensor("CrossedIR1");

        distance_leftIR.enable(TIME_STEP);

        distance_rightIR = agent.getDistanceSensor("CrossedIR2");

        distance_rightIR.enable(TIME_STEP);

           

        toucher1 = agent.getTouchSensor("toucher1");
        toucher1.enable(TIME_STEP);

        toucher2 = agent.getTouchSensor("toucher2");
        toucher2.enable(TIME_STEP);

        gripper1 = agent.getServo("finger1");
        gripper2 = agent.getServo("finger2");

        gripper1.enablePosition(TIME_STEP);
        gripper2.enablePosition(TIME_STEP);

        gripper1.enableMotorForceFeedback(TIME_STEP);
        gripper2.enableMotorForceFeedback(TIME_STEP);

        arm1 = agent.getServo("arm1");
        arm2 = agent.getServo("arm2");
        arm3 = agent.getServo("arm3");
        arm4 = agent.getServo("arm4");
        arm5 = agent.getServo("arm5");

        arm1.enablePosition(TIME_STEP);
        arm2.enablePosition(TIME_STEP);
        arm3.enablePosition(TIME_STEP);
        arm4.enablePosition(TIME_STEP);
        arm5.enablePosition(TIME_STEP);

        arm1.enableMotorForceFeedback(TIME_STEP);
        arm2.enableMotorForceFeedback(TIME_STEP);
        arm3.enableMotorForceFeedback(TIME_STEP);
        arm4.enableMotorForceFeedback(TIME_STEP);
        arm5.enableMotorForceFeedback(TIME_STEP);

        // Initialize interal sensed data and commands
        //1. Sensed data
        sensedData.put("beam_broken", false);
        sensedData.put("wristForce_active", false);
        sensedData.put("grabbing", false);
        sensedData.put("toucher1_active", false);
        sensedData.put("toucher2_active", false);
        sensedData.put("arm2_pos", 0.0);
        sensedData.put("arm3_pos", 0.0);
        sensedData.put("arm4_pos", 0.0);
        sensedData.put("IR_Crossed", false);

        //2. Output commands
        left_gripper_pos = 0.0;
        right_gripper_pos = 0.0;

        arm1_pos = 0.0;
        arm2_pos = 0.0;
        arm3_pos = 0.0;
        arm4_pos = 0.0;
        arm5_pos = 0.0;

        wheel1_base_pos = 0.0;
        wheel2_base_pos = 0.0;
        wheel3_base_pos = 0.0;
        wheel4_base_pos = 0.0;

        // This sub task must be started after other variables have been initialized above,
        // becaues some of those variables might be necessarily update in this task.
        ticksPerRun = (Integer) getParam("environment.ticksPerRun", DEFAULT_TICKS_PER_RUN);
        backgroundTask = new BackgroundTask(ticksPerRun);
        taskSpawner.addTask(backgroundTask);

    }

    @Override
    public Object getState(Map<String, ?> params) {
       
        return sensedData;
    }

    private class BackgroundTask extends FrameworkTaskImpl {

        public BackgroundTask(int ticksPerRun) {
            super(ticksPerRun);
            // Initialize the arms position to grabbing
            // TODO: To replace this behavior by real arms motors

            //arm_set_height: ARM_FRONT_CARDBOARD_BOX
            /*
            arm2_pos = -0.67;
            arm3_pos = -1.23;
            arm4_pos = -1.24;
            //arm4_pos = -0.89;
            arm5_pos = 0.0;
             * 
             */
            
        }

        @Override
        protected void runThisFrameworkTask() {
            driveEnvironment();
        }
    }

    public void driveEnvironment() {

        double beam_Distance, leftIR_Distance, rightIR_Distance;

        double toucher1_force = 0.0, toucher2_force = 0.0;

        double gripper1_force = 0.0, gripper2_force = 0.0, arm4_force = 0.0, sum_force = 0.0;;
		
	double arm2_pos_tmp = 0.0, arm3_pos_tmp = 0.0, arm4_pos_tmp = 0.0, sum_arm_pos = 0.0;

        boolean hand_on_homeSide = false;
                
        // Syncronize with Webots
        if (agent.step(TIME_STEP) == -1) {
            logger.log(Level.SEVERE, "Connection with Webots was disconnected.", TaskManager.getCurrentTick());
            return;
        }

        // 1. Collect sensory information
        
        // 1) ::Beam::
        //Based on the configuration of the gripper, the translating formular
        //between grippers position and the distance sensory Info is:
        // (MAX) Diff POS is 0.05 = 0.025 *2 <==> (MAX) DIS Sensor is 1000
        beam_Distance = distance_beam.getValue();
        
        if (beam_Distance < BEAM_MAX_DIS*(1-Distance_TolerationRate))
        {
            //logger.log(Level.INFO, "beam is broken!");
            sensedData.put("beam_broken", true);
        }else{
            //logger.log(Level.INFO, "beam is NOT broken!");
            sensedData.put("beam_broken", false);
        }

        gripper1_force = gripper1.getMotorForceFeedback();

        gripper2_force =gripper2.getMotorForceFeedback();

        arm4_force = arm4.getMotorForceFeedback();

        //System.out.println("Forces are in order of ::Gripper1::Gripper2:arm4::" +
        //        gripper1_force + " :: " + gripper2_force + " :: " + arm4_force);

        // 2) wrist force
        // 2-1) Arms position and Side of hand
        // Note that, we don't consider arm1 and arm5 here because they are not moving the hand
        // in Y-Axis. Their position don't affect the result of the hand's side.
        // Also, we assume that the positions of arm1 and arm5 should be set by 0 and not change in
        // the whole process of the project at all. It means we only consider them the rigid solids.
        arm2_pos_tmp = arm2.getPosition();
        arm3_pos_tmp = arm3.getPosition();
        arm4_pos_tmp = arm4.getPosition();

        sensedData.put("arm2_pos", arm2_pos_tmp);
        sensedData.put("arm3_pos", arm3_pos_tmp);
        sensedData.put("arm4_pos", arm4_pos_tmp);

        sum_arm_pos = arm2_pos_tmp + arm3_pos_tmp + arm4_pos_tmp;

        if (sum_arm_pos > 0.0)
        {
            hand_on_homeSide = true;
        }else if (sum_arm_pos < 0.0){
            hand_on_homeSide = false;
        }else{
            // The value of hand side is assume to true when sum_arm_pos == 0.0
            hand_on_homeSide = true;
        }
        
        // 2-2) Boolean of wrist force
        //Note that the variable 'handOnHomeSide' indicates the side of hand;
        // the force against to the wrist is positive when the hand side is on home side (true),
        // while it's negative when the hand side is not on home side (false).
        //
        //Note that, arm4 is considered the wrist because the arm5 is not used,
        // so that which (arm5) is not suitable to used to observe wrist force.
        if ((hand_on_homeSide == true && arm4_force > WRIST_FORCE_THRESHOLD) ||
                        (hand_on_homeSide == false && arm4_force < (0 - WRIST_FORCE_THRESHOLD))){
            sensedData.put("wristForce_active", true);
        } else{
            sensedData.put("wristForce_active", false);
        }

        // 3) Touch force
        toucher1_force = toucher1.getValue();
        toucher2_force = toucher2.getValue();

        //System.out.println("toucher1_force is: " + toucher1_force);
        if (toucher1_force > TOUCH_THRESHOLD){
            sensedData.put("toucher1_active", true);
        }else{
            sensedData.put("toucher1_active", false);
        }

        if (toucher2_force > TOUCH_THRESHOLD){
            sensedData.put("toucher2_active", true);
        }else{
            sensedData.put("toucher2_active", false);
        }

        
        // 4) Grabbing
        // Note that this is an indirected sensed data, which relays on the
        // previously sensed data: beam and grippers forces. So it must process
        // after the processes of beam and grippers forces.

        if ((Boolean)sensedData.get("beam_broken") &&
                (Math.abs(gripper1_force) >= GRIPPER_MAX_FORCE) && (Math.abs(gripper2_force) >= GRIPPER_MAX_FORCE)){
            //logger.log(Level.INFO, "Grabbing is True.");
            sensedData.put("grabbing", true);
        }else{
            //logger.log(Level.INFO, "Grabbing is False.");
            sensedData.put("grabbing", false);
        }

        // TODO: For testing only
        //sensedData.put("grabbing", false);

        //5) Crossed IR
        leftIR_Distance = distance_leftIR.getValue();

        rightIR_Distance = distance_rightIR.getValue();

        //System.out.println("LeftIR and rightIR:" + leftIR_Distance + ", " + rightIR_Distance);

        if ((leftIR_Distance > IR_CROSSED_AREA_LOWERBOUND_DISTANCE &&
                leftIR_Distance < IR_CROSSED_AREA_UPPERBOUND_DISTANCE) &&
                (rightIR_Distance > IR_CROSSED_AREA_LOWERBOUND_DISTANCE &&
                rightIR_Distance < IR_CROSSED_AREA_UPPERBOUND_DISTANCE)){
            sensedData.put("IR_Crossed", true);
            //System.out.println("IR_Crossed is True.");
        } else {
            sensedData.put("IR_Crossed", false);
            //System.out.println("IR_Crossed is False.");
        }
        

                

        // 2. Send commands to robot

        // Record the commands of last time so that only execute the commands
        // if they are different than that of last time.
        // 1) ::Hand::

        if (left_gripper_pos != left_gripper_pos_last){
            //System.out.println("left_gripper_pos:" + left_gripper_pos);
            gripper1.setPosition(left_gripper_pos);
            left_gripper_pos_last = left_gripper_pos;
        }

        if (right_gripper_pos != right_gripper_pos_last){
            gripper2.setPosition(right_gripper_pos);
            right_gripper_pos_last = right_gripper_pos;
        }
        
        // 2) ::Arm::
        if (arm1_pos != arm1_pos_last){
            arm1.setPosition(arm1_pos);
            arm1_pos_last = arm1_pos;
        }

        if (arm2_pos != arm2_pos_last){
            arm2.setPosition(arm2_pos);
            arm2_pos_last = arm2_pos;
        }

        //System.out.println("ENV:: arm2, arm3, and arm4 are:" + arm2_pos + arm3_pos + arm4_pos);

        if (arm3_pos != arm3_pos_last){
            arm3.setPosition(arm3_pos);
            arm3_pos_last = arm3_pos;
        }
        
        if (arm4_pos != arm4_pos_last){
            arm4.setPosition(arm4_pos);
            arm4_pos_last = arm4_pos;
        }
        
        if (arm5_pos != arm5_pos_last){
            arm5.setPosition(arm5_pos);
            arm5_pos_last = arm5_pos;
        }

        // 3) ::Base::
        //TODO
    }

    @Override
    public void resetState() {
        //initSensedData();
    }

    @Override
    public String toString() {
        return "WebotsEnvironment";
    }

    @Override
    public Object getModuleContent(Object... params) {
        return sensedData;
    }

    @Override
    public void processAction(Object o) {
        //logger.log(Level.INFO, "ProcessAction is working!");
        Map<String, Object> commands = (HashMap) o;
        if (commands != null){

            Object gripper1_p = commands.get("Left_Gripper_pos");
            Object gripper2_p = commands.get("Right_Gripper_pos");

            Object arm1_p = commands.get("Arm1_pos");
            Object arm2_p = commands.get("Arm2_pos");
            Object arm3_p = commands.get("Arm3_pos");
            Object arm4_p = commands.get("Arm4_pos");
            Object arm5_p = commands.get("Arm5_pos");

            Object wheel1_p = commands.get("base_wheel1");
            Object wheel2_p = commands.get("base_wheel2");
            Object wheel3_p = commands.get("base_wheel3");
            Object wheel4_p = commands.get("base_wheel4");

            if (gripper1_p != null){
                //logger.log(Level.INFO, "Command for left gripper position is set.");
                left_gripper_pos = (Double) gripper1_p;
            } else{
                //logger.log(Level.INFO, "Command for left gripper position is null.");
            }

            if (gripper2_p != null){
                right_gripper_pos = (Double) gripper2_p;
            } else{
            }

            if (arm1_p != null){
                arm1_pos = (Double) arm1_p;
            } else{
            }

            if (arm2_p != null){
                arm2_pos = (Double) arm2_p;
            } else{
            }

            if (arm3_p != null){
                //System.out.println("The arm3_pos is SET as:" + arm3_pos);
                arm3_pos = (Double) arm3_p;
            } else{
                //System.out.println("The arm3_pos is NOT set value!");
            }

            if (arm4_p != null){
                arm4_pos = (Double) arm4_p;
            } else{
            }

            if (arm5_p != null){
                arm5_pos = (Double) arm5_p;
            } else{
            }

            if (wheel1_p != null){
                wheel1_base_pos = (Double) wheel1_p;
            } else{
            }

            if (wheel2_p != null){
                wheel2_base_pos = (Double) wheel2_p;
            } else{
            }

            if (wheel3_p != null){
                wheel3_base_pos = (Double) wheel3_p;
            } else{
            }

            if (wheel4_p != null){
                wheel4_base_pos = (Double) wheel4_p;
            } else{
            }
            
        } else{
            logger.log(Level.INFO, "Commands are null!");
        }
        
    }
}
