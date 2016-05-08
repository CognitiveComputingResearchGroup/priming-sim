/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.MPT.pointing;

import java.util.Map;

import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.FSM;
import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.SubsumptionMPTImpl;
import java.util.logging.Level;
import myagent.modules.PrimingSensoryMotorSystem;

/**
 *
 * @author Daqi
 */
public abstract class pointingMPT extends SubsumptionMPTImpl{
   
    protected FSM theFSM;
    
    //private double FOO;

    @Override
    public void init() {
        
        //PointingTopRightFSM = new PointingTopRightFSM(FSM_TICKS_PER_RUN);
        //PointingTopRightFSM.init();
        specifyTheFSM();
        theFSM.init();
        
        //FOO = PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF;
        
    }
    
    public abstract void specifyTheFSM();

    @Override
    public void receiveData(Object o) {
        //no impl
    }

     @Override
    public void specify() {
        theFSM.specify(PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF);
    }
    
    @Override
    public void update() {
        //logger.log(Level.INFO, "updating MC variables ...");
        //FOO = FOO + 0.01;
                
        theFSM.update(PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF);
        //theFSM.update(FOO);
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
