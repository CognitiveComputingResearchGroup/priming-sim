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

    private double gripper_POS;
        
    private static final int STATE_NIL = 0;
    private static final int STATE_OPEN = 1;
    
    private static final double GRIPPER_MAX_POS = 0.025;

    private static final int FIXED_MGA_TIME = 1024;

    private int started_time;

    public OpenFSM(int ticksPerRun) {
        super(ticksPerRun);
    }
    
    @Override
    public void init() {

        state = STATE_NIL;

        gripper_POS = GRIPPER_MAX_POS;

        started_time = 0;

        commands.put("Left_Gripper_pos", null);
        commands.put("Right_Gripper_pos", null);
    }

    @Override
    public void specify(Object o){
        // This specify() method rely on dorsal, ventral, or default value orderly
        Double cmdValue = (Double) o;

        if (cmdValue > 0.0){
            gripper_POS = cmdValue;
        }else{
            gripper_POS = GRIPPER_MAX_POS;
        }

    }

    @Override
    public void update(Object o){
        // This update() method rely on dorsal, or default value orderly

        Double cmdValue = (Double) o;

        if (cmdValue > 0.0){
            gripper_POS = cmdValue;
        }else{
            gripper_POS = GRIPPER_MAX_POS;
        }
    }
        
    @Override
    public void execute() {

        switch (state){
            case STATE_NIL:
                state = STATE_OPEN;
                break;
            case STATE_OPEN:
                //System.out.println("The gripper_POS is: " + gripper_POS);
                if (started_time < FIXED_MGA_TIME){//The finger open to MGA first when it starts
                    commands.put("Left_Gripper_pos", GRIPPER_MAX_POS);
                    commands.put("Right_Gripper_pos", GRIPPER_MAX_POS);
                } else{
                    commands.put("Left_Gripper_pos", gripper_POS);
                    commands.put("Right_Gripper_pos", gripper_POS);
                }
                state = STATE_OPEN;
                break;
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }

        started_time = started_time + 1;
    }

}
