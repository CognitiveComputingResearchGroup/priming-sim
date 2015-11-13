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
    
    //TODO: Need variables to store output commands

    //Handler of robot node
    private Robot agent;
    
    private DistanceSensor distance1;

    //private double distanceVal, servoPos;

    private Servo gripper1, gripper2, arm1, arm2, arm3, arm4, arm5;

    private static final int TIME_STEP = 32; // milliseconds

    private static final double GRIPPER_MIN_POS = 0.0;
    private static final double GRIPPER_MAX_POS = 0.025;

    private static final double GRIP_DISTANCE = 500.0;

    //This is a dummy force(0~1000), higher than threshold 500
    private static final double WRIST_HIGH_FORCE = 800.0;

    //This is a dummy force(0~1000), lower than threshold 500
    private static final double WRIST_LOW_FORCE = 100.0;

    private double left_gripper_pos, right_gripper_pos;

    private int wrist_force_fireTime;

    @Override
    public void init() {
        //For Webots' init
        agent = new Robot();

        distance1 = agent.getDistanceSensor("beam");

        distance1.enable(TIME_STEP);

        gripper1 = agent.getServo("finger1");
        gripper2 = agent.getServo("finger2");

        gripper1.enablePosition(TIME_STEP);
        gripper2.enablePosition(TIME_STEP);

         //gripper_release
        gripper1.setPosition(GRIPPER_MAX_POS);
        gripper2.setPosition(GRIPPER_MAX_POS);

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

        // Initialize interal sensed data and commands
        sensedData.put("beam_broken", false);
        sensedData.put("wrist_force", 0.0);

        //For general init
        ticksPerRun = (Integer) getParam("environment.ticksPerRun", DEFAULT_TICKS_PER_RUN);
        backgroundTask = new BackgroundTask(ticksPerRun);
        taskSpawner.addTask(backgroundTask);

        left_gripper_pos = 0.0;
        right_gripper_pos = 0.0;

        wrist_force_fireTime =0;        
    }

    @Override
    public Object getState(Map<String, ?> params) {
       
        return sensedData;
    }

    /* TODO: accessor for actuator variables
    public Map<String, Object> getWheelsSpeed() {
        return wheelSpeeds;
    }
     * */

    private void passive_wait(double sec){
        double start_time = agent.getTime();
        do{
            if (agent.step(TIME_STEP) == -1) {
                logger.log(Level.SEVERE, "Connection with Webots was disconnected.", TaskManager.getCurrentTick());
                return;
            }
        }while (start_time + sec > agent.getTime());
        
        
    }

    private void automatic_behavior(){

        double distanceVal, forceVal;
        
        passive_wait(1.0);

        //arm_set_height: ARM_FRONT_CARDBOARD_BOX
        arm2.setPosition(0.0);
        arm3.setPosition(-0.77);
        arm4.setPosition(-1.21);
        arm5.setPosition(0.0);

        passive_wait(0.5);

        //gripper_grip();
        distanceVal = distance1.getValue();
        //logger.log(Level.INFO, "Gripping distance = " + distanceVal);

        if (distanceVal< GRIP_DISTANCE){
            sensedData.put("beam_broken", true);
        }else{
            sensedData.put("beam_broken", false);
        }

        passive_wait(0.5);
        gripper1.setPosition(left_gripper_pos);
        gripper2.setPosition(right_gripper_pos);

        passive_wait(0.5);

        if (distanceVal< GRIP_DISTANCE){
            sensedData.put("beam_broken", true);
        }else{
            sensedData.put("beam_broken", false);
        }

        //arm_set_height: ARM_BACK_PLATE_LOW
        arm2.setPosition(0.92);
        arm3.setPosition(0.42);
        arm4.setPosition(1.78);
        arm5.setPosition(0.0);

        passive_wait(0.5);

        //gripper_release
        //gripper1.setPosition(GRIPPER_MAX_POS);
        //gripper2.setPosition(GRIPPER_MAX_POS);

    }

    private class BackgroundTask extends FrameworkTaskImpl {

        public BackgroundTask(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            driveEnvironment();
        }
    }

    public void driveEnvironment() {
        // Syncronize with Webots

        double distanceVal;
        
        if (agent.step(TIME_STEP) == -1) {
            logger.log(Level.SEVERE, "Connection with Webots was disconnected.", TaskManager.getCurrentTick());
            return;
        }

        //Get sensed data
        distanceVal = distance1.getValue();
        logger.log(Level.INFO, "The initial gripping distance = " + distanceVal);

        //TODO: Triggered by real sensed data
        sensedData.put("wrist_force", 0.0);

        // Send commands to robot
        automatic_behavior();
       
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
        Map<String, Object> commands = (HashMap) o;
        if (commands != null){
            left_gripper_pos = (Double) commands.get("Left_Gripper_pos");
            right_gripper_pos = (Double) commands.get("Right_Gripper_pos");
        } else{
            logger.log(Level.INFO, "Commands are null!");
        }

        //logger.log(Level.INFO, "Process Action is working!");
        
    }
}
