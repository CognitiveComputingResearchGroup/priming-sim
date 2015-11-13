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
public class OpenGrippersFsm extends FsmImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(OpenGrippersFsm.class.getCanonicalName());
    
    private int state;
    
    private static final int STATE_NIL = 0;
    private static final int STATE_OPEN = 1;

    //private static final double CMD_UNDEFINED = -1.0;

    private Map<String, Object> commands = new HashMap<String, Object>();

    private static final double GRIPPER_MAX_POS = 0.025;

    @Override
    public void init() {

        state = STATE_NIL;

        commands = null;

   }

    /*
    @Override
    public void receiveData(Object o) {
        // No running input
    }
     * 
     */

    @Override
    public Map outputCommands() {
        return commands;
    }

    @Override
    public void run() {
        System.out.println("open running! and the state is:" + state);
        switch (state){
            case STATE_NIL:
                state = STATE_OPEN;
                break;
            case STATE_OPEN:
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
