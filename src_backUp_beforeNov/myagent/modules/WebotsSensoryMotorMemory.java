/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package myagent.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.actionselection.Action;
import edu.memphis.ccrg.lida.environment.Environment;
import edu.memphis.ccrg.lida.framework.FrameworkModule;
import edu.memphis.ccrg.lida.framework.ModuleListener;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import edu.memphis.ccrg.lida.sensorymotormemory.BasicSensoryMotorMemory;
import edu.memphis.ccrg.lida.sensorymotormemory.SensoryMotorMemoryListener;
import myagent.modules.Grip.GripMpt;
import myagent.modules.WebotsEnvironment;

public class WebotsSensoryMotorMemory extends BasicSensoryMotorMemory {

    private static final Logger logger = Logger.getLogger(WebotsSensoryMotorMemory.class.getCanonicalName());
    private static final int DEFAULT_BACKGROUND_TASK_TICKS = 1;
    private int processActionTaskTicks;
    private List<SensoryMotorMemoryListener> listeners = new ArrayList<SensoryMotorMemoryListener>();
    private Map<Number, Object> actionAlgorithmMap = new HashMap<Number, Object>();
    private Environment environment;
    private Map<String, Object> sensedData = new HashMap<String, Object>();
    private static final double DEFAULT_WHEEL_SPEED = 50.0;
    private double wheelSpeed;
    private static final double TURN_ANGLE_SCALE_UPPER = 1.0;
    private double scaleUpper;
    private static final double TURN_ANGLE_SCALE_LOWER = 0.2;
    private double scaleLower;
    private static final double FORWARD_NOISE = 0.1;
    private double forwardNoise;
    private static final double MAX_SPEED = 1024;
    private double maxSpeed;
    private static final boolean DORSAL_STREAM = true;
    private boolean dorsalStream;
    private static final double SMALLEST_NUM = 0.0001;

    private Mpt gripMpt;

    /**
     * Default constructor
     */
    public WebotsSensoryMotorMemory() {
    }

    @Override
    public void init() {
        processActionTaskTicks = (Integer) getParam("smm.processActionTaskTicks", DEFAULT_BACKGROUND_TASK_TICKS);
        scaleUpper = (Double) getParam("smm.turnAngleScaleUpper", TURN_ANGLE_SCALE_UPPER);
        scaleLower = (Double) getParam("smm.turnAngleScaleLower", TURN_ANGLE_SCALE_LOWER);
        forwardNoise = (Double) getParam("smm.forwardNoise", FORWARD_NOISE);
        wheelSpeed = (Double) getParam("smm.defaultWheelSpeed", DEFAULT_WHEEL_SPEED);
        maxSpeed = (Double) getParam("smm.maxSpeed", MAX_SPEED);
        dorsalStream = (Boolean) getParam("smm.dorsalStream", DORSAL_STREAM);

        gripMpt = new GripMpt();
        
        gripMpt.init();
    }


    @Override
    public void addListener(ModuleListener listener) {
            if (listener instanceof SensoryMotorMemoryListener) {
                    addSensoryMotorMemoryListener((SensoryMotorMemoryListener) listener);
            } else {
                    logger.log(Level.WARNING, "Cannot add listener {1}",
                                    new Object[]{TaskManager.getCurrentTick(),listener});
            }
    }

    @Override
    public void addSensoryMotorMemoryListener(SensoryMotorMemoryListener l) {
            listeners.add(l);
    }

    @Override
    public void setAssociatedModule(FrameworkModule module, String moduleUsage) {
            if (module instanceof Environment) {
                    environment = (Environment) module;
            } else {
                    logger.log(Level.WARNING, "Cannot add module {1}",
                                    new Object[]{TaskManager.getCurrentTick(),module});
            }
    }

    /**
     * Adds an Algorithm to this {@link SensoryMotorMemory}
     * @param actionId Id of {@link Action} which is implemented by the algorithm
     * @param action an algorithm
     */
    public void addActionAlgorithm(Number actionId, Object action) {
        actionAlgorithmMap.put(actionId, action);
    }

    @Override
    public synchronized void receiveAction(Action action) {
        if (action != null) {
            ProcessActionTask t = new ProcessActionTask(action);
            taskSpawner.addTask(t);
        } else {
            logger.log(Level.WARNING, "Received null action", TaskManager.getCurrentTick());
        }
    }

    private class ProcessActionTask extends FrameworkTaskImpl {

        private Action action;

        public ProcessActionTask(Action a) {
            super(processActionTaskTicks);
            action = a;
        }

        @Override
        protected void runThisFrameworkTask() {
            // Get algorithm (kind of template -> Symbolic)
            Object alg = actionAlgorithmMap.get((Number) action.getId());

            // TODO: If there is sensed data only.
            if (alg != null) {

                sendActuatorCommand(generateActuatorCommand(alg));
            } else {
                logger.log(Level.WARNING, "Could not find algorithm for action {1}",
                        new Object[]{TaskManager.getCurrentTick(), action});
            }
            cancel();
        }
    }

    private Map<String, Object> generateActuatorCommand(Object alg) {

        
        Map<String, Object> commands = new HashMap<String, Object>();

        gripMpt.receiveData(sensedData);

        gripMpt.run();

        commands = (HashMap)gripMpt.outputCommands();


        //logger.log(Level.INFO, "SMM is working!");

        //Object o = sensedData.get("leftEye");
        
        //TODO: Why does this (Double) block the program flow?
        //double dummy = (Double)sensedData.get("leftEye");

        //logger.log(Level.INFO, "SMM is working again!!!!!!");

        
        return commands;
    }

    @Override
    public void sendActuatorCommand(Object command) {
        environment.processAction(command);
        for (SensoryMotorMemoryListener l : listeners) {
            l.receiveActuatorCommand(command);
        }
    }

    @Override
    public void receiveSensoryMemoryContent(Object content) {
        if (content instanceof HashMap) {
            sensedData.putAll((HashMap) content);
            //logger.log(Level.INFO, "SMM:The beam broken is: " + (Boolean)sensedData.get("beam_broken"));
            //logger.log(Level.INFO, "SMM:The wrist force is: " + (Double)sensedData.get("wrist_force"));
        } else {
            logger.log(Level.INFO, "Not a HashMap", TaskManager.getCurrentTick());
            //Add here for other type of sensed data
        }
        // TODO: Add the functionality of letting doesal stream content drives SMM
        // which means it needs to add a task here, similar to that is in receiveAction()
    }
}
