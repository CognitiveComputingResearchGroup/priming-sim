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
public class HomeFSM extends ArmsFSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(HomeFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_GOHOME = 2;
    private static final int STATE_HOME = 3;
    private static final int STATE_STUCK = 9;

    private static final int TIMER_UPPER_BOUND = 1024;

    private int timer;

    private double last_arm1_pos, last_arm2_pos, last_arm3_pos, last_arm4_pos;

    public HomeFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();
        state = STATE_NIL;

        timer = 0;

        last_arm1_pos = 0.0;
        last_arm2_pos = 0.0;
        last_arm3_pos = 0.0;
        last_arm4_pos = 0.0;

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

                //If the hand is not in the home side and the hand is freezing
                // then move it back to home side
                if (Math.abs(arm1_pos - ARM1_HOME_POS) > APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm1_pos - last_arm1_pos) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm2_pos - last_arm2_pos) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm3_pos - last_arm3_pos) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm4_pos - last_arm4_pos) < APPROX_EQUAL_MIN_VAL){
                    timer = timer + 1;
                    if (timer > TIMER_UPPER_BOUND){
                        timer = 0;
                        state = STATE_GOHOME;
                    } else {
                        state = STATE_CHECK;
                    }
                } else {
                    timer = 0;
                    state = STATE_NIL;
                }

                break;
            case STATE_GOHOME:
                //logger.log(Level.INFO, "STATE_GOHOME starts...");
                // Waiting for being in the Home position
                if (Math.abs(arm1_pos - ARM1_HOME_POS) < APPROX_EQUAL_MIN_VAL){
                    System.out.println("The Arm is in the Home side now.");
                    state = STATE_NIL;
                } else {
                    // Verify the current arms positions
                    /* Ignore the STUCK condition because it was used to suppress
                     * other normal FSM, such as surface or descend FSMs. Here,
                     * the homeFSM was assumed to be OK to work with STUCK positions.
                    if (arms_are_valid(arm2_pos, arm3_pos, arm4_pos) == false){
                        logger.log(Level.INFO, "arms_are_valid() got FALSE result!");
                        state = STATE_STUCK;
                        break;
                    }
                     * 
                     */

                    commands.put("Arm1_pos", ARM1_HOME_POS);
                    state = STATE_GOHOME;
                }

                break;
            case STATE_STUCK:
                //logger.log(Level.INFO, "HomeFSM STATE_STUCK !!");
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

        // Last <-- current
        last_arm1_pos = arm1_pos;
        last_arm2_pos = arm2_pos;
        last_arm3_pos = arm3_pos;
        last_arm4_pos = arm4_pos;
    }

}
