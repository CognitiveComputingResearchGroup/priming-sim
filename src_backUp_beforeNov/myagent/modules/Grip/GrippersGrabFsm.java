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
public class GrippersGrabFsm extends FsmImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(GrippersGrabFsm.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_DECIDE = 1;
    private static final int STATE_GRAB = 2;

    //private static final double CMD_UNDEFINED = -1.0;

    private Map<String, Object> commands = new HashMap<String, Object>();

    private static final double GRIPPER_MIN_POS = 0.0;


    // Does the beam of grippers has been broken?
    private boolean beam;

    @Override
    public void init() {

        state = STATE_NIL;

        commands = null;

        beam = false;
    }

    @Override
    public void receiveData(Object o) {
        //Read beam status
         beam = (Boolean)o;
    }

    @Override
    public Map outputCommands() {
        return commands;
    }

    @Override
    public void run() {
        System.out.println("grab running!");
        switch (state){
            case STATE_NIL:
                state = STATE_DECIDE;
                break;
            case STATE_DECIDE:
                if (beam == true)
                    state = STATE_GRAB;
                else{
                    commands = null;
                    state = STATE_NIL;
                } 
                break;
            case STATE_GRAB:
                commands.put("Left_Gripper_pos", GRIPPER_MIN_POS);
                commands.put("Right_Gripper_pos", GRIPPER_MIN_POS);
                state = STATE_NIL;
                break;
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }
}
