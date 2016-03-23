/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.MPT.pointing;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.SMS.MPT.FSM;
import myagent.SMS.MPT.SubsumptionMPTImpl;
import myagent.MPT.pointing.FSM.PointingTopRightFSM;

/**
 *
 * @author Daqi
 */
public class PointingTopRightMPT extends SubsumptionMPTImpl{
    
    private static final Logger logger = Logger.getLogger(PointingTopRightMPT.class.getCanonicalName());

    private final int FSM_TICKS_PER_RUN = 1;
    
    private FSM PointingTopRightFSM;

    @Override
    public void init() {
        
        PointingTopRightFSM = new PointingTopRightFSM(FSM_TICKS_PER_RUN);
        PointingTopRightFSM.init();
        
    }

    @Override
    public void specify() {
        //no impl
    }

    @Override
    public void update() {
        //no impl
    }

    @Override
    public void onlineControl() {
        logger.log(Level.INFO, "pointing to the target (top right)...");
        
        taskSpawner.addTask(PointingTopRightFSM);
        
        commands = (Map<String, Object>) PointingTopRightFSM.outputCommands();
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
