/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.MPT.pointing;

import java.util.Map;

import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.FSM;
import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.MPTension;
import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.SubsumptionMPTImpl;
import java.util.HashMap;
import java.util.logging.Level;
import myagent.modules.PrimingSensoryMotorSystem;

/**
 *
 * @author Daqi
 */
public abstract class pointingMPT extends SubsumptionMPTImpl implements MPTension{
   
    protected FSM theFSM;
    
    protected double tension;
    
    protected boolean behavioralSelected;
    
    protected Map <String, Double> CommandVal = new HashMap <String, Double>();
    
    //default moving force (N)
    public static final double MOVING_FORCE_DEF = 10.0;

    //default direction in radians (45 degrees)
    public static final double MOVING_DIRECTION_DEF = Math.PI/4;
    
    public static final double TENSION_TO_FORCE_RATE = 0.01;
    
    //private double FOO;

    @Override
    public void init() {
        
        //PointingTopRightFSM = new PointingTopRightFSM(FSM_TICKS_PER_RUN);
        //PointingTopRightFSM.init();
        specifyTheFSM();
        theFSM.init();
        
        //FOO = PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF;
        
        tension = 0.0;
        
        behavioralSelected =false;
        
        CommandVal.put("force", 0.0);
        CommandVal.put("direction", 0.0);
        
    }
    
    public abstract void specifyTheFSM();
    
    public void setTension (double val){
        tension = val;
    }
    
    public double getTension (){
        return tension;
    }
    
    public void addTesion (double val){
        
        if (val > 0.0){
            tension = tension + val;
        }
    }
    
    public void removeTension(double val){
        
        if (val < tension){
            tension = tension - val;
        } else{
            tension = 0.0;
        }
        
    }
    
    public void setBehavioralSelected(boolean val){
        behavioralSelected = val;
    }
    
    public boolean getBehavioralSelected(){
        return behavioralSelected;
    }
    
    

    @Override
    public void receiveData(Object o) {
        //no impl
    }

     @Override
    public void specify() {
        //theFSM.specify(PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF);
        
        
        CommandVal.put("direction", MOVING_DIRECTION_DEF);
        
        double force_val = tension*TENSION_TO_FORCE_RATE;
        
        if ((behavioralSelected == true)&& (force_val < MOVING_FORCE_DEF)){
            force_val = MOVING_FORCE_DEF;
        }
        
        CommandVal.put("force", force_val);
        
        theFSM.specify(CommandVal);

    }
    
    @Override
    public void update() {

        CommandVal.put("direction", MOVING_DIRECTION_DEF);
        
        double force_val = tension*TENSION_TO_FORCE_RATE;
        
        if ((behavioralSelected == true)&& (force_val < MOVING_FORCE_DEF)){
            force_val = MOVING_FORCE_DEF;
        }
        
        CommandVal.put("force", force_val);
        
        theFSM.update(CommandVal);
    }

    @Override
    public void onlineControl() {
       
        taskSpawner.addTask(theFSM);
        
        //commands = (Map<String, Object>) theFSM.outputCommands();
    }
    
    @Override
    public Object outputCommands() {
        //Since there is only one FSM running in this MPT, here we directly return the FSM's output
        return theFSM.outputCommands();
    }

    @Override
    public Object dorsal_MotorValueOf(String cmdName) {
        //no impl
        return -1.0;
    }

    @Override
    public Object behavior_MotorValueOf(String cmdName) {
        // no impl
        return -1.0;
    }
}
