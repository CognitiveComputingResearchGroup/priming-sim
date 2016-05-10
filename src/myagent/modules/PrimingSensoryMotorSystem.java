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

import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.SensoryMotorSystem;
import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.MPT;

import myagent.MPT.pointing.PointingBottomLeftMPT;
import myagent.MPT.pointing.PointingTopRightMPT;
import myagent.MPT.pointing.pointingMPT;



public class PrimingSensoryMotorSystem extends SensoryMotorSystem {

    private static final Logger logger = Logger.getLogger(PrimingSensoryMotorSystem.class.getCanonicalName());
    private static final int DEFAULT_BACKGROUND_TASK_TICKS = 1;
    private int processActionTaskTicks;
    private Map<Number, Object> actionAlgorithmMap = new HashMap<Number, Object>();
  
    private boolean actionInProgress = false;
    
    private Map<String, Object> MPTInstanceMap = new HashMap <String, Object>();
    
    //The name of MPTs
    public static final String MPT_TopRight = "topRight";
    public static final String MPT_BottomLeft = "bottomLeft";
    
    private Map<String, String> algoMPTMap = new HashMap<String, String>();
    
    //The name of these algorithms are defined in the configure file;
    //The consistency is necessary
    public static final String algo_TopRightMPT = "algorithm.topRight";
    public static final String algo_BottomLeftMPT = "algorithm.bottomLeft";
    
    private Map<Integer, String> sensoryDataMPTMap = new HashMap <Integer, String>();
    
    //The sensory data (target stimulus area/position)
    public static final Integer pos_TopRight = 1;
    public static final Integer pos_BottomLeft = 3;
    
    private Map <String, Object> currentMPs = new HashMap <String, Object>();
       
    //default moving force (N)
    public static final double MOVING_FORCE_DEF = 1.0;

    //default direction in radians (45 degrees)
    public static final double MOVING_DIRECTION_DEF = Math.PI/4;
    
    public static final double TENSION_ADDED_PERRUN = 10.0;
    
    public static final double TENSION_REMOVED_PERRUN = 5.0;
    
    public static final String target_color = "red";
    
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
        
        //Init the MPT instance map
        m = new PointingTopRightMPT();
        m.init();
        m.receiveTS(taskSpawner);
        
        MPTInstanceMap.put(MPT_TopRight, m);
        
        m = new PointingBottomLeftMPT();
        m.init();
        m.receiveTS(taskSpawner);
        
        MPTInstanceMap.put(MPT_BottomLeft, m);
        
        //Init the algo MPT map
        algoMPTMap.put(algo_TopRightMPT, MPT_TopRight);
        
        algoMPTMap.put(algo_BottomLeftMPT, MPT_BottomLeft);
        
        //Init the sensory data MPT map
        sensoryDataMPTMap.put(pos_TopRight, MPT_TopRight);
        sensoryDataMPTMap.put(pos_BottomLeft, MPT_BottomLeft);
        
        
    }

    @Override
    public MPT selectMPT(Object alg) {
        
        String MPTname = algoMPTMap.get((String)alg);
        
        if (MPTname != null)
            return (MPT)MPTInstanceMap.get(MPTname);
        else{
            return null;
        }

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
            
            String MPTName = algoMPTMap.get(selectedAlg);
            
            if (MPTName == null){
                logger.log(Level.WARNING, "The selected algorithm does not have relevant motor plan template", TaskManager.getCurrentTick());
                return;
            }
            
            //If MPTName != null
            //Then
            
             pointingMPT selectedMP;
                     
            if (currentMPs.containsKey(MPTName)){
                selectedMP = (pointingMPT) currentMPs.get(MPTName);
                
            } else{
                //MPT selection
                MPT selectedMPT = (MPT)MPTInstanceMap.get(MPTName);

                selectedMP = (pointingMPT)selectedMPT;

                //MPT specificaiton (a fake one; currently the default value of direction is passed)
                selectedMP.specify();
                
                currentMPs.put(MPTName, selectedMP);
            }
                            
            //control the selected MP to have current tension to be kept 
            selectedMP.setBehavioralSelected(true);
            
            //online control
            //currentMP.onlineControl();
            
            for (Object theMP: currentMPs.values()){
                ((MPT)theMP).onlineControl();
            }
            
            OutputMPTCommands t1 = new OutputMPTCommands();
            taskSpawner.addTask(t1);
                        
            System.out.println("Action execution start!");
            actionInProgress = true;
        } else {
            logger.log(Level.WARNING, "Received null action", TaskManager.getCurrentTick());
        }

    }

    private class OutputMPTCommands extends FrameworkTaskImpl {

        @Override
        protected void runThisFrameworkTask() {
        
            /*
            Object cmd = currentMP.outputCommands();
            //System.out.println("OutputMPTCommands::the command sent to environment is: " + cmd);
            
            sendActuatorCommand(cmd);
            */
            Map <String, Object> commands = new HashMap<String, Object> ();
            for (Object theMP: currentMPs.values()){
                Object cmd = ((MPT)theMP).outputCommands();
                //System.out.println("OutputMPTCommands::the command sent to environment is: " + cmd);
                String motorName = (String)((Map)cmd).get("MotorName");
                commands.put(motorName, cmd);
            }
            
            sendActuatorCommand(commands);
        }
    }
    

    @Override
    public void receiveSensoryMemoryContent(Object content) {
                    
        //recieve data
        //no impl
        
        if (content == null)
            return;//ignore the empty states
        
        //update
        
        /*
        if (currentMP != null)
        {
            currentMP.update();
        }
        */
        
        for (Object theMP: currentMPs.values()){
            ((MPT)theMP).update();
        }
        
        //priming: cue a new MPT to execute
        
        //1. retrieve the relevant sensory data
        Map sensoryData = (Map) content;
        Integer pos = 0;
        
        Object obj;
        
        String MPTName;
        
        if (sensoryData.containsKey(target_color)){

            if ((Boolean)sensoryData.get(target_color) == true){
                obj = sensoryData.get(target_color+"_position");

                if (obj != null){
                    pos = (Integer) obj;        
                }
            }
        }
        
        //System.out.println("the pos is: " + pos);
        
        //2. prime the relevant MP
        obj = sensoryDataMPTMap.get(pos);
        
        if (obj != null){
            MPTName = (String)obj;
            
            if (actionInProgress == false){//Before the start of action execution
                //Create new MP in the current MPs pool
                primingMPT(MPTName);
                
            } else {//During action execution
                //Support current existing MP
                //--> No impl since the priming effect only occurs before the start
                //of action execution
                
            }
        }
    }
    
    /*
    Create new motor plan (MP) driven by the arrival of relevant sensory data
    when the action execution is not started yet
    */
    private void primingMPT (String MPTName){
        
        if (currentMPs.containsKey(MPTName)){
            //no impl
            //TODO add a few amount of tension?
        } else {
            //add a new MP into the current pool
            //if there is a relevant MPT available
            if (MPTInstanceMap.containsKey(MPTName)){
                MPT m1 = (MPT)MPTInstanceMap.get(MPTName);
                
                //MPT --> MP
                m1.specify();
                
                //add the MP into the current MP pool
                currentMPs.put(MPTName, m1);
                
                //maintain the selected MP so as to update its tension values
                maintaintheMPtension t1 = new maintaintheMPtension(m1);
                taskSpawner.addTask(t1);
                
            }
        }
        
    }
    
    private class maintaintheMPtension extends FrameworkTaskImpl {
        
        private pointingMPT theMP;
        
        public maintaintheMPtension(MPT m1) {
            super();
            
            theMP = (pointingMPT)m1;
        }

        @Override
        protected void runThisFrameworkTask() {
            
            /*Two states
            1. "Seal": before to start the execution
            --> ferment. quickly increase the tension value of a ready MP
            2. "Open": after the moment of starting the execution
            --> leakage. quickly decrease the tension value of a ready MP
                    */

            if (actionInProgress == false){ // "Seal" using TENSION_ADDED_PERRUN
                
                theMP.addTesion(TENSION_ADDED_PERRUN);

            } else {//"Open" using TENSION_REMOVED_PERRUN
                
                if (!theMP.getBehavioralSelected()){//If the MP is not the one mapped from the selected behavior
                    theMP.removeTension(TENSION_REMOVED_PERRUN);
                    
                } else{//If the MP is the one from the selected behavior
                    //its tension will not decrease but just be 'perserved' 
                    
                }

            }
            
            //System.out.println("MP name:" + theMP.toString() + " and the tension is " + theMP.getTension());

        }
        
    }
}
