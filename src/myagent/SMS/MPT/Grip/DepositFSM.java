/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.SMS.MPT.Grip;

import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.SMS.MPT.FSMImpl;

/**
 *
 * @author Daqi
 */
public class DepositFSM extends FSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(DepositFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_RELEASE = 2;

    private static final double GRIPPER_MAX_POS = 0.025;

    private static final int MONOSTABLE_MAX_TIME = 1024;

    private int monostable_time;

    // The force applied on the wrist (Arm4)
    // Note that we decide to consider arm4, instead of arm 5, the wrist
    // as well as that is in the Herbert robot
    private boolean wrist_force_active, toucher1_active, toucher2_active;

    public DepositFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        state = STATE_NIL;

        commands.put("Left_Gripper_pos", null);
        commands.put("Right_Gripper_pos", null);
        
        wrist_force_active = false;

        toucher1_active = false;
        toucher2_active = false;

        monostable_time = 0;
    }

    @Override
    public void receiveData(Object wristForce, Object toucher1, Object toucher2) {
        //Read wrist force
         wrist_force_active = (Boolean)wristForce;

         toucher1_active = (Boolean)toucher1;
         toucher2_active = (Boolean)toucher2;
    }

    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "DepositFSM STATE_NIL starts...");
                commands.put("Left_Gripper_pos", null);
                commands.put("Right_Gripper_pos", null);
                state = STATE_CHECK;
                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "DepositFSM STATE_CHECK starts...");
                if (wrist_force_active == true || toucher1_active == true || toucher2_active == true)
                    state = STATE_RELEASE;
                else{
                    state = STATE_NIL;
                }    
                break;
            case STATE_RELEASE:
                //logger.log(Level.INFO, "DepositFSM STATE_RELEASE starts...");
                commands.put("Left_Gripper_pos", GRIPPER_MAX_POS);
                commands.put("Right_Gripper_pos", GRIPPER_MAX_POS);
                monostable_time = monostable_time + 1;
                if (monostable_time > MONOSTABLE_MAX_TIME){
                    monostable_time = 0;
                    state = STATE_CHECK;
                } else{
                    state = STATE_RELEASE;
                }
                break;
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }
}
