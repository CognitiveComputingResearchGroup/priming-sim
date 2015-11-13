/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules.Grip;

import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daqi
 */
public class OverFSM extends ArmsFSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(OverFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_OVER = 2;
    private static final int STATE_STUCK = 3;

    private boolean IR_crossed;

    public OverFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();

        state = STATE_NIL;

        IR_crossed = false;

    }

    @Override
    public void receiveData(Object o) {

        IR_crossed = (Boolean) o;
    }

    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "OverFSM STATE_NIL starts...");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                state = STATE_CHECK;
                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "OverFSM STATE_CHECK starts...");
                if (IR_crossed == true)
                    state = STATE_OVER;
                else{
                    state = STATE_NIL;
                }
                break;
            case STATE_OVER:
                //logger.log(Level.INFO, "OverFSM STATE_OVER starts...");

                // Verify the current arms positions
                if (arms_are_valid(arm2_pos, arm3_pos, arm4_pos) == false){
                    logger.log(Level.INFO, "arms_are_valid() got FALSE result!");
                    state = STATE_STUCK;
                    break;
                }

                //System.out.println("From:" + arm2_pos + " : " + arm3_pos + " : " + arm4_pos);

                // upward
                if (upward() == false){
                    logger.log(Level.INFO, "upward() got FALSE result!");
                    state = STATE_STUCK;
                    break;
                }

                //System.out.println("To:" + arm2_updated_pos + " : " + arm3_updated_pos + " : " + arm4_updated_pos);

                commands.put("Arm2_pos", arm2_updated_pos);
                commands.put("Arm3_pos", arm3_updated_pos);
                commands.put("Arm4_pos", arm4_updated_pos);

                state = STATE_CHECK;

                break;

            case STATE_STUCK:
                //logger.log(Level.INFO, "OverFSM STATE_STUCK !!");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                // The arms are in stuck. It needs to be reset by other higher FSM,
                // such as a homeFSM calling BounceFSM.init()
                state = STATE_STUCK;
                break;

            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }


}
