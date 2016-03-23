/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.MPT.pointing;

import java.util.Map;
import myagent.SMS.MPT.FSM;
import myagent.SMS.MPT.SubsumptionMPTImpl;
import myagent.modules.PrimingSensoryMotorSystem;

/**
 *
 * @author Daqi
 */
public abstract class pointingMPT extends SubsumptionMPTImpl{
   
    protected FSM theFSM;

    @Override
    public void init() {
        
        //PointingTopRightFSM = new PointingTopRightFSM(FSM_TICKS_PER_RUN);
        //PointingTopRightFSM.init();
        specifyTheFSM();
        theFSM.init();
        
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
        theFSM.update(PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF);
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
