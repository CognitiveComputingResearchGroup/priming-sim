/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.MPT.pointing.FSM;

/**
 *
 * @author Daqi
 */

import myagent.modules.PrimingEnvironment;

public class PointingTopRightFSM extends pointingFSM {

    
    
    public PointingTopRightFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    public void setMotorName() {
       commands.put("MotorName", PrimingEnvironment.UPPER_MOTOR_NAME);
    }
    
}
