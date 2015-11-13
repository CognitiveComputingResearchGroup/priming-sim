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
public class DiagonalFSM  extends ArmsFSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(DiagonalFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_DIAGONAL = 2;
    private static final int STATE_STUCK = 3;

    private boolean beam_grabbing;


    public DiagonalFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();

        state = STATE_NIL;

        beam_grabbing = false;
    }

    @Override
    public void receiveData(Object o) {

        beam_grabbing = (Boolean) o;
    }



    @Override
    public void execute() {

        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "STATE_NIL starts...");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                state = STATE_CHECK;

                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "STATE_CHECK starts...");
                if (beam_grabbing == true)
                    state = STATE_DIAGONAL;
                else{
                    state = STATE_NIL;
                }
                break;
            case STATE_DIAGONAL:
                //logger.log(Level.INFO, "STATE_DIAGONAL starts...");

                // Verify the current arms positions
                if (arms_are_valid(arm2_pos, arm3_pos, arm4_pos) == false){
                    logger.log(Level.INFO, "arms_are_valid() got FALSE result!");
                    state = STATE_STUCK;
                    break;
                }

                // Downward and slightly forward
                if (down_forward() == false){
                    logger.log(Level.INFO, "down_forward() got FALSE result!");
                    state = STATE_STUCK;
                    break;
                }

                commands.put("Arm2_pos", arm2_updated_pos);
                commands.put("Arm3_pos", arm3_updated_pos);
                commands.put("Arm4_pos", arm4_updated_pos);

                state = STATE_CHECK;
                break;

            case STATE_STUCK:
                //logger.log(Level.INFO, "STATE_STUCK !!");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                state = STATE_STUCK;
                break;

            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }


}
