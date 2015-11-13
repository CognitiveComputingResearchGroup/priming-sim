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
public class StopFSM extends ArmsFSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(StopFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_CHECK = 1;
    private static final int STATE_STOP = 2;

    private static final int MONOSTABLE_MAX_TIME = 3072;

    private int monostable_time;

    private boolean beam, beam_last;
    
    public StopFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();
        state = STATE_NIL;

        beam = false;
        beam_last = false;
        monostable_time = 0;
    }

    @Override
    public void receiveData(Object o) {

        beam = (Boolean) o;
    }

    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:
                //logger.log(Level.INFO, "StopFSM STATE_NIL starts...");
                commands.put("Arm2_pos", null);
                commands.put("Arm3_pos", null);
                commands.put("Arm4_pos", null);

                state = STATE_CHECK;
                break;
            case STATE_CHECK:
                //logger.log(Level.INFO, "StopFSM STATE_CHECK starts...");
                if (beam == true && beam_last == false){//The first time beam was broken
                    state = STATE_STOP;
                } else {
                    state = STATE_NIL;
                }

                // To update beam_last after the state has been determined
                beam_last = beam;
                
                break;
            case STATE_STOP:
                //logger.log(Level.INFO, "StopFSM STATE_STOP starts...");

                
                commands.put("Arm2_pos", arm2_pos);
                commands.put("Arm3_pos", arm3_pos);
                commands.put("Arm4_pos", arm4_pos);

                monostable_time = monostable_time + 1;
                if (monostable_time > MONOSTABLE_MAX_TIME){
                    monostable_time = 0;
                    state = STATE_CHECK;
                } else{
                    state = STATE_STOP;
                }

                break;

            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }

}
