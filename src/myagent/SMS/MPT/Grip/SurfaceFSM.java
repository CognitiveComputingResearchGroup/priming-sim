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
public class SurfaceFSM extends ArmsFSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(SurfaceFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_FORWARD_DOWN = 2;
    private static final int STATE_STUCK = 3;

    private static final int MONOSTABLE_MAX_TIME = 15360;

    private int monostable_time;

    private boolean wrist_force_active, toucher1_active, toucher2_active;

    public SurfaceFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();
        state = STATE_NIL;

        wrist_force_active = false;
        toucher1_active = false;
        toucher2_active = false;

        monostable_time = 0;
    }

    @Override
    public void receiveData(Object wristForce, Object toucher1, Object toucher2) {

        wrist_force_active = (Boolean) wristForce;

        toucher1_active = (Boolean) toucher1;
        toucher2_active = (Boolean) toucher2;
    }


    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "SurfaceFSM STATE_NIL starts...");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                state = STATE_CHECK;
                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "SurfaceFSM STATE_CHECK starts...");
                if (wrist_force_active == true || toucher1_active == true || toucher2_active == true)
                    state = STATE_FORWARD_DOWN;
                else{
                    state = STATE_NIL;
                }
                break;
            case STATE_FORWARD_DOWN:
                //logger.log(Level.INFO, "SurfaceFSM STATE_COIL starts...");

                // Verify the current arms positions
                if (arms_are_valid(arm2_pos, arm3_pos, arm4_pos) == false){
                    logger.log(Level.INFO, "arms_are_valid() got FALSE result!");
                    state = STATE_STUCK;
                    break;
                }

                // forward_down
                if (forward_down() == false){
                    logger.log(Level.INFO, "forward_down() got FALSE result!");
                    state = STATE_STUCK;
                    break;
                }

                commands.put("Arm2_pos", arm2_updated_pos);
                commands.put("Arm3_pos", arm3_updated_pos);
                commands.put("Arm4_pos", arm4_updated_pos);

                // Initialize the timer if the tact triggers were activated in the
                // forward down process.
                if (wrist_force_active == true || toucher1_active == true || toucher2_active == true){
                    monostable_time = 0;
                }

                // Istead of infinitely do forward_down, a timer controls it to
                // execute finitely.
                monostable_time = monostable_time + 1;
                if (monostable_time > MONOSTABLE_MAX_TIME){
                    monostable_time = 0;
                    state = STATE_CHECK;
                } else{
                    state = STATE_FORWARD_DOWN;
                }
                
                break;

            case STATE_STUCK:
                //logger.log(Level.INFO, "SurfaceFSM STATE_STUCK !!");
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
