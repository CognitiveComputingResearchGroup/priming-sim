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
public class HoistFSM extends ArmsFSMImpl{
    //Parameters for task running
    private static final Logger logger = Logger.getLogger(HoistFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_INIT = 2;
    private static final int STATE_STUCK = 9;

    private static final int TIMER_UPPER_BOUND = 1024;

    private int timer;

    private double last_arm2_pos, last_arm3_pos, last_arm4_pos;



    public HoistFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();
        state = STATE_NIL;

        timer = 0;

        last_arm2_pos = 0.0;
        last_arm3_pos = 0.0;
        last_arm4_pos = 0.0;

    }

    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "HoistFSM STATE_NIL starts...");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);
                
                state = STATE_CHECK;
                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "HoistFSM STATE_CHECK starts...");
                if (Math.abs(arm2_pos - last_arm2_pos) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm3_pos - last_arm3_pos) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm4_pos - last_arm4_pos) < APPROX_EQUAL_MIN_VAL){
                    timer = timer + 1;
                    //System.out.println("Timer:" + timer);
                    if (timer > TIMER_UPPER_BOUND){
                        timer = 0;
                        state = STATE_INIT;
                    } else {
                        state = STATE_CHECK;
                    }
                } else {
                    timer = 0;
                    state = STATE_NIL;
                }

                break;
            case STATE_INIT:
                //logger.log(Level.INFO, "HoistFSM STATE_INIT starts...");
                // Waiting for being in the Home position
                if (Math.abs(arm2_pos - ARM2_INIT_POS) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm3_pos - ARM3_INIT_POS ) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm4_pos - ARM4_INIT_POS) < APPROX_EQUAL_MIN_VAL){
                    System.out.println("The hand is in the initial position now.");
                    state = STATE_NIL;
                } else {
                    /* Ignore the STUCK condition because it was used to suppress
                     * other normal FSM, such as surface or descend FSMs. Here,
                     * the hoistFSM was assumed to be OK to work with STUCK positions.
                    // Verify the current arms positions
                    if (arms_are_valid(arm2_pos, arm3_pos, arm4_pos) == false){
                        logger.log(Level.INFO, "arms_are_valid() got FALSE result!");
                        state = STATE_STUCK;
                        break;
                    }
                     * 
                     */

                    // back_home
                    if (back_init() == false){
                        logger.log(Level.INFO, "back_home() got FALSE result!");
                        //state = STATE_STUCK;
                        //break;
                    }
                     
                    commands.put("Arm2_pos", arm2_updated_pos);
                    commands.put("Arm3_pos", arm3_updated_pos);
                    commands.put("Arm4_pos", arm4_updated_pos);
                    
                    state = STATE_INIT;
                }

                break;
            case STATE_STUCK:
                //logger.log(Level.INFO, "HoistFSM STATE_STUCK !!");
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
        last_arm2_pos = arm2_pos;
        last_arm3_pos = arm3_pos;
        last_arm4_pos = arm4_pos;
    }
    
}
