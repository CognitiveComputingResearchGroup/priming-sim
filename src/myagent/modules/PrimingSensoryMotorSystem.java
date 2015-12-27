/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package myagent.modules;

import myagent.SMS.MPT.MPT;
import myagent.SMS.EE.EE;
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
import myagent.SMS.MPT.Grip.GripMPT;
import myagent.modules.WebotsEnvironment;

import myagent.SMS.EE.Grip.GripEE;

public class PrimingSensoryMotorSystem extends BasicSensoryMotorMemory {

    private static final Logger logger = Logger.getLogger(PrimingSensoryMotorSystem.class.getCanonicalName());
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

    private boolean actionInProgress = false;

    private MPT gripMpt;

    private EE gripEE;

    /**
     * Default constructor
     */
    public PrimingSensoryMotorSystem() {
    }

    @Override
    public void init() {

   }


    @Override
    public void addListener(ModuleListener listener) {

    }

    @Override
    public void addSensoryMotorMemoryListener(SensoryMotorMemoryListener l) {

    }

    @Override
    public void setAssociatedModule(FrameworkModule module, String moduleUsage) {

    }

    /**
     * Adds an Algorithm to this {@link SensoryMotorMemory}
     * @param actionId Id of {@link Action} which is implemented by the algorithm
     * @param action an algorithm
     */
    @Override
    public void addActionAlgorithm(Number actionId, Object action) {

    }

    @Override
    public synchronized void receiveAction(Action action) {

    }

    private class ProcessActionTask extends FrameworkTaskImpl {

        private Action action;
        Object alg = null;

        public ProcessActionTask(Action a) {
            super(processActionTaskTicks);
            action = a;
        }

        @Override
        protected void runThisFrameworkTask() {

        }
    }

    private Map<String, Object> generateActuatorCommand(Object alg) {
		return sensedData;

    }

    @Override
    public void sendActuatorCommand(Object command) {

    }

    @Override
    public void receiveSensoryMemoryContent(Object content) {

    }
}
