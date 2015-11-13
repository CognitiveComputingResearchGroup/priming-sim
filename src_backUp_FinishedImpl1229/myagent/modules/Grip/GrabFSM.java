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
import myagent.modules.FSMImpl;

/**
 *
 * @author Daqi
 */
public class GrabFSM extends FSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(GrabFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_GRAB = 2;

    private static final double GRIPPER_MIN_POS = 0.0;


    // Does the beam of grippers has been broken?
    private boolean beam;

    public GrabFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        state = STATE_NIL;

        commands.put("Left_Gripper_pos", null);
        commands.put("Right_Gripper_pos", null);

        beam = false;
    }

    @Override
    public void receiveData(Object o) {
        //Read beam status
         beam = (Boolean)o;
    }

    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "GrabFSM STATE_NIL starts...");
                commands.put("Left_Gripper_pos", null);
                commands.put("Right_Gripper_pos", null);
                state = STATE_CHECK;
                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "GrabFSM STATE_CHECK starts...");
                if (beam == true)
                    state = STATE_GRAB;
                else{
                    state = STATE_NIL;
                } 
                break;
            case STATE_GRAB:
                //logger.log(Level.INFO, "GrabFSM STATE_GRAB starts...");
                commands.put("Left_Gripper_pos", GRIPPER_MIN_POS);
                commands.put("Right_Gripper_pos", GRIPPER_MIN_POS);
                state = STATE_CHECK;
                break;
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }
}
