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
import myagent.modules.PrimingEnvironment;

public class PrimingSensoryMotorSystem extends BasicSensoryMotorMemory {

    private static final Logger logger = Logger.getLogger(PrimingSensoryMotorSystem.class.getCanonicalName());
    private static final int DEFAULT_BACKGROUND_TASK_TICKS = 1;
    private int processActionTaskTicks;
    private List<SensoryMotorMemoryListener> listeners = new ArrayList<SensoryMotorMemoryListener>();
    private Map<Number, Object> actionAlgorithmMap = new HashMap<Number, Object>();
    private Environment environment;
    private Map<String, Object> sensedData = new HashMap<String, Object>();
  
    private boolean actionInProgress = false;

    /**
     * Default constructor
     */
    public PrimingSensoryMotorSystem() {
    }

    @Override
    public void init() {
        processActionTaskTicks = (Integer) getParam("smm.processActionTaskTicks", DEFAULT_BACKGROUND_TASK_TICKS);

        /* Init MPTs as below
        gripMpt = new GripMPT();
        gripMpt.init();
        gripMpt.receiveTS(taskSpawner);
        */
        
        //Load motor plan tempaltes (MPTs), maybe from disk (database) to memory, 
        //for choosing current MPT
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
