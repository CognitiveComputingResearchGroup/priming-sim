/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules.Grip;

import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.modules.FSMImpl;

/**
 *
 * @author Daqi
 */
public class OpenFSM extends FSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(OpenFSM.class.getCanonicalName());

    private int state;
        
    private static final int STATE_NIL = 0;
    private static final int STATE_OPEN = 1;

    private static final double GRIPPER_MAX_POS = 0.025;

    public OpenFSM(int ticksPerRun) {
        super(ticksPerRun);
    }
    
    @Override
    public void init() {

        state = STATE_NIL;

        commands.put("Left_Gripper_pos", null);
        commands.put("Right_Gripper_pos", null);
    }
        
    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:
                state = STATE_OPEN;
                break;
            case STATE_OPEN:
                commands.put("Left_Gripper_pos", GRIPPER_MAX_POS);
                commands.put("Right_Gripper_pos", GRIPPER_MAX_POS);
                state = STATE_OPEN;
                break;
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }

}
