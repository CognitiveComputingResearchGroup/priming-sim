/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.SMS.MPT.Grip;

import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Daqi
 */
public class DescendFSM extends ArmsFSMImpl{
    
    //Parameters for task running
    private static final Logger logger = Logger.getLogger(DescendFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_DESCEND = 1;
    private static final int STATE_STUCK = 2;

    private static final int MONOSTABLE_MAX_TIME = 128;

    private static final int TEST_STOP_MOMENT = 2048;

    private int monostable_time;

    private int test_stop_moment;

    public DescendFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();

        state = STATE_NIL;

        monostable_time = 0;

        test_stop_moment = 0;
    }

    @Override
    public void execute() {
        
        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "DescendFSM STATE_NIL starts...");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                // Delay to start real function for a while because of the reading delay of sensory info.
                monostable_time = monostable_time + 1;
                if (monostable_time > MONOSTABLE_MAX_TIME){
                    monostable_time = 0;
                    state = STATE_DESCEND;
                } else{
                    state = STATE_NIL;
                }

                break;
            case STATE_DESCEND:
                //logger.log(Level.INFO, "DescendFSM STATE_DESCEND starts...");

                // Verify the current arms positions
                if (arms_are_valid(arm2_pos, arm3_pos, arm4_pos) == false){
                    logger.log(Level.INFO, "arms_are_valid() got FALSE result!");
                    state = STATE_STUCK;
                    break;
                }

                test_stop_moment = test_stop_moment + 1;

//                if (test_stop_moment >= TEST_STOP_MOMENT){
//                    //DO NOTHING
//                    //Not going down if the time reaches a certain movement
//
//                }else{
                    // Downward
                    if (downward() == false){
                        logger.log(Level.INFO, "downward() got FALSE result!");
                        state = STATE_STUCK;
                        break;
                    }
//                }


//
//                System.out.println("movment: --" + test_stop_moment + "--");
//
//                System.out.println("Measurements: " + arm2_pos + ", " + arm3_pos
//                        + ", " + arm4_pos);
//
//                System.out.println("Motor commands: " + arm2_updated_pos + ", "
//                        + arm3_updated_pos + ", " + arm4_updated_pos);

                commands.put("Arm2_pos", arm2_updated_pos);
                commands.put("Arm3_pos", arm3_updated_pos);
                commands.put("Arm4_pos", arm4_updated_pos);

                state = STATE_DESCEND;
                break;
                
            case STATE_STUCK:
                //logger.log(Level.INFO, "DescendFSM STATE_STUCK !!");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                // The arms are in stuck. It needs to be reset by other higher FSM,
                // such as a homeFSM calling descendFsm.init()
                state = STATE_STUCK;
                break;
                
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }
}
