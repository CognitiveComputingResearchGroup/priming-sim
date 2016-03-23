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
//import edu.memphis.ccrg.lida.sensorymotormemory.BasicSensoryMotorMemory;
import edu.memphis.ccrg.lida.sensorymotormemory.SensoryMotorMemoryListener;
import myagent.modules.PrimingEnvironment;

import myagent.SMS.SensoryMotorSystem;
import myagent.SMS.MPT.MPT;

import myagent.MPT.pointing.PointingBottomLeftMPT;
import myagent.MPT.pointing.PointingTopRightMPT;



public class PrimingSensoryMotorSystem extends SensoryMotorSystem {

    private static final Logger logger = Logger.getLogger(PrimingSensoryMotorSystem.class.getCanonicalName());
    private static final int DEFAULT_BACKGROUND_TASK_TICKS = 1;
    private int processActionTaskTicks;
    private Map<Number, Object> actionAlgorithmMap = new HashMap<Number, Object>();
  
    private boolean actionInProgress = false;
    
    private Map<String, Object> algoMPTMap = new HashMap<String, Object>();
    
    //The name of these algorithms are defined in the configure file;
    //The consistency is necessary
    public static final String topRightMPT = "algorithm.topRight";
    public static final String bottomLeftMPT = "algorithm.bottomLeft";
    
    //default moving force (N)
    public static final double MOVING_FORCE_DEF = 1.0;

    //default direction (90 degrees)
    public static final double MOVING_DIRECTION_DEF = Math.PI/4;
    
    private MPT selectedMPT;
    
    /**
     * Default constructor
     */
    public PrimingSensoryMotorSystem() {
    }

    @Override
    public void init() {
        processActionTaskTicks = (Integer) getParam("smm.processActionTaskTicks", DEFAULT_BACKGROUND_TASK_TICKS);
        
        //Load motor plan tempaltes (MPTs), maybe from disk (database) to memory, 
        //for choosing current MPT
        loadMPT();
        
        actionInProgress = false;
               
    }
    
    @Override
    public void loadMPT() {
        
        MPT m;
        
        m = new PointingTopRightMPT();
        m.init();
        m.receiveTS(taskSpawner);
        
        algoMPTMap.put(topRightMPT, m);
        
        m = new PointingBottomLeftMPT();
        m.init();
        m.receiveTS(taskSpawner);
        
        algoMPTMap.put(bottomLeftMPT, m);
        
    }

    @Override
    public MPT selectMPT(Object alg) {
        
        return (MPT)algoMPTMap.get((String)alg);

    }
    
    /**
     * Adds an Algorithm to this {@link SensoryMotorMemory}
     * @param actionId Id of {@link Action} which is implemented by the algorithm
     * @param action an algorithm
     */
    @Override
    public void addActionAlgorithm(Number actionId, Object action) {
        //System.out.println("::addActionAlgorithm:: actionId is" + actionId + " and action is " + action);
        actionAlgorithmMap.put(actionId, action);
    }
    
    @Override
    public synchronized void receiveAction(Action action) {

        if (actionInProgress){
        // To start the certain grip MPT once only, because the FSMs inside
        // the MPT are kind of tasks automatically running continuously
            //logger.log(Level.INFO, "No need to execute now.");
            return;
        }
        
        if (action != null) {
            logger.log(Level.INFO, "receiveAction starts...");
            
            Object selectedAlg = actionAlgorithmMap.get((Number)action.getId());
            
            //MPT selection
            selectedMPT = selectMPT(selectedAlg);
           
            //MPT specificaiton (a fake one; currently the default value of direction is passed)
            selectedMPT.specify();
            
            //online control
            selectedMPT.onlineControl();
            
            OutputMPTCommands t1 = new OutputMPTCommands();
            taskSpawner.addTask(t1);
                        
            actionInProgress = true;
        } else {
            logger.log(Level.WARNING, "Received null action", TaskManager.getCurrentTick());
        }

    }

    private class OutputMPTCommands extends FrameworkTaskImpl {

        @Override
        protected void runThisFrameworkTask() {
        
            Object cmd = selectedMPT.outputCommands();
            //System.out.println("OutputMPTCommands::the command sent to environment is: " + cmd);
            
            sendActuatorCommand(cmd);

        }
    }
    

    @Override
    public void receiveSensoryMemoryContent(Object content) {
                    
        //recieve data
        //no impl
        
        //update
        //selectedMPT.update();
        
        //TODO:
        //priming: cue a new MPT to execute

    }
}
