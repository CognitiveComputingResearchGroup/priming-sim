/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.MPT.pointing;

import java.util.logging.Level;
import java.util.logging.Logger;

import myagent.MPT.pointing.FSM.PointingBottomLeftFSM;
import myagent.modules.PrimingSensoryMotorSystem;

/**
 *
 * @author Daqi
 */
public class PointingBottomLeftMPT extends pointingMPT{
    private static final Logger logger = Logger.getLogger(PointingBottomLeftMPT.class.getCanonicalName());

    private final int FSM_TICKS_PER_RUN = 1;
    
    @Override
    public void specifyTheFSM() {
        theFSM = new PointingBottomLeftFSM(FSM_TICKS_PER_RUN);;
    }
   
    @Override
    public void onlineControl() {
        logger.log(Level.INFO, "pointing to the target (bottom left)...");
        
        super.onlineControl();
    }
    
    @Override
    public void specify() {
        theFSM.specify(PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF + Math.PI);
    }
}
