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
public class EngardeFSM extends ArmsFSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(EngardeFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_GOSTART = 2;
    private static final int STATE_STUCK = 9;


    private boolean beam;

    public EngardeFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();
        state = STATE_NIL;

        beam = false;
    }

    @Override
    public void receiveData(Object o) {

        beam = (Boolean) o;
    }

    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "HomeFSM STATE_NIL starts...");
                commands.put("Arm1_pos", null);
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                state = STATE_CHECK;
                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "HomeFSM STATE_CHECK starts...");

                //If the hand is in the home side and at the initial position
                // and its grippers is empty
                if (Math.abs(arm1_pos - ARM1_HOME_POS) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm2_pos - ARM2_INIT_POS) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm3_pos - ARM3_INIT_POS) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm4_pos - ARM4_INIT_POS) < APPROX_EQUAL_MIN_VAL
                        && beam == false){

                    state = STATE_GOSTART;

                } else {
                    state = STATE_NIL;
                }

                break;
            case STATE_GOSTART:
                //logger.log(Level.INFO, "HomeFSM STATE_HOME starts...");
                // Waiting for being in the Start position
                if (Math.abs(arm1_pos - ARM1_START_POS) < APPROX_EQUAL_MIN_VAL){
                    System.out.println("The Arm is in the Start side now.");
                    state = STATE_NIL;
                } else {
                    // Verify the current arms positions
                    if (arms_are_valid(arm2_pos, arm3_pos, arm4_pos) == false){
                        logger.log(Level.INFO, "arms_are_valid() got FALSE result!");
                        state = STATE_STUCK;
                        break;
                    }

                    commands.put("Arm1_pos", ARM1_START_POS);
                    state = STATE_GOSTART;
                }

                break;
            case STATE_STUCK:
                logger.log(Level.INFO, "STATE_STUCK !!");
                commands.put("Arm1_pos", null);
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
