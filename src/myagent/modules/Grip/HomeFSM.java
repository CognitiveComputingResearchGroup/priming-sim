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
public class HomeFSM extends ArmsFSMImpl{
    //Parameters for task running
    private static final Logger logger = Logger.getLogger(HomeFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_HOME = 2;
    private static final int STATE_FINISH = 3;

    private static final int TIMER_UPPER_BOUND = 1024;

    private int timer;

    private double last_arm2_pos, last_arm3_pos, last_arm4_pos;



    public HomeFSM(int ticksPerRun) {
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
                //logger.log(Level.INFO, "HomeFSM STATE_NIL starts...");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);
                
                state = STATE_CHECK;
                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "HomeFSM STATE_CHECK starts...");
                //System.out.println("Last: " + last_arm2_pos + ", " + last_arm3_pos + ", " + last_arm4_pos);
                //System.out.println("Current: " + arm2_pos + ", " + arm3_pos + ", " + arm4_pos);
                if (Math.abs(arm2_pos - last_arm2_pos) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm3_pos - last_arm3_pos) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm4_pos - last_arm4_pos) < APPROX_EQUAL_MIN_VAL){
                    timer = timer + 1;
                    //System.out.println("Timer:" + timer);
                    if (timer > TIMER_UPPER_BOUND){
                        
                        commands.put("Arm2_pos", arm2_initPos);
                        commands.put("Arm3_pos", arm3_initPos);
                        commands.put("Arm4_pos", arm4_initPos);
                        
                        state = STATE_HOME;
                    } else {
                        state = STATE_CHECK;
                    }
                } else {
                    timer = 0;
                    state = STATE_NIL;
                }

                break;
            case STATE_HOME:
                //logger.log(Level.INFO, "HomeFSM STATE_HOME starts...");
                // Waiting for being in the Home position
                if (Math.abs(arm2_pos - arm2_initPos) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm3_pos - arm3_initPos ) < APPROX_EQUAL_MIN_VAL
                        && Math.abs(arm4_pos - arm4_initPos) < APPROX_EQUAL_MIN_VAL){
                    System.out.println("The hand is in the Home position now.");
                    state = STATE_FINISH;
                } else {
                    state = STATE_HOME;
                }

                break;
            case STATE_FINISH:
                //logger.log(Level.INFO, "HomeFSM STATE_FINISH starts...");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                state = STATE_FINISH;
                
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
