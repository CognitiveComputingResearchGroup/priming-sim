/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules.Grip;

import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.modules.FsmImpl;
import myagent.modules.WebotsEnvironment;

/**
 *
 * @author Daqi
 */
public class DepositGrippersFsm extends FsmImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(DepositGrippersFsm.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_DECIDE = 1;
    private static final int STATE_RELEASE = 2;

    //private static final double CMD_UNDEFINED = -1.0;

    private Map<String, Object> commands = new HashMap<String, Object>();

    private static final double GRIPPER_MAX_POS = 0.0025;

    private static final double WRIST_FORCE_THRESHOLD = 500.0;


    // Does the beam of grippers has been broken?
    private double wrist_force;

    @Override
    public void init() {

        state = STATE_NIL;

        commands = null;
        
        wrist_force = 0.0;
    }

    @Override
    public void receiveData(Object o) {
        //Read wrist force
         wrist_force = (Double)o;
    }

    @Override
    public Map outputCommands() {
        return commands;
    }

    @Override
    public void run() {
        //logger.log(Level.INFO, "The wrist force is: {0} and state is: {1}", new Object[]{wrist_force, state});
        //System.out.println("The wrist force is: " + wrist_force + " and state is: " + state);
        System.out.println("deposit running!");
        System.out.println("The state is: " + state + " and wrist_force is: " + wrist_force);
        switch (state){
            case STATE_NIL:
                state = STATE_DECIDE;
                break;
            case STATE_DECIDE:
                if (wrist_force > WRIST_FORCE_THRESHOLD)
                    state = STATE_RELEASE;
                else{
                    commands = null;
                    state = STATE_NIL;
                }    
                break;
            case STATE_RELEASE:
                commands.put("Left_Gripper_pos", GRIPPER_MAX_POS);
                commands.put("Right_Gripper_pos", GRIPPER_MAX_POS);
                state = STATE_NIL;
                break;
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }
}
