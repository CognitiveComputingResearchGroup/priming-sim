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
    private static final int STATE_STOP_DELAY = 2;
    private static final int STATE_STOP = 3;

    private static final int MONOSTABLE_MAX_TIME = 3072;

    private static final int MONOSTABLE_DELAY_TIME = 64;

    private int monostable_max_time, monostable_delay_time;

    private double beam_distance, beam_dis_last;
    
    public StopFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();
        state = STATE_NIL;

        beam_distance = -1.0;
        beam_dis_last = -1.0;
        
        monostable_max_time = 0;

        monostable_delay_time = 0;
    }

    @Override
    public void receiveData(Object o) {

        beam_distance = (Double) o;
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
                //Check the beam distance changing
                // The beam_changing indicates that there is an object between grippers
                // and the grippers are moving.
                // Note that the beam's distance will not change if there is no object between
                // grippers even though the grippers are moving, because the grippers are not
                // designed with PhysicalBoundary in Webots so the beam is not broken by grippers.
                

                // The condition "beam_dis_last > 0" is used to check whether "beam_dis_last"
                // really hold a valid beam distance value, because beam_dis_last was initialized by -1.0
                //System.out.println("current and last beamDis are: " + beam_distance + " : " + beam_dis_last);
                if ((beam_dis_last > 0) && (Math.abs(beam_distance - beam_dis_last) >= APPROX_EQUAL_MAX_VAL3))
                {
                    //System.out.println("STATE_CHECK: going to STATE_STOP");
                    state = STATE_STOP;
                }else{
                    //System.out.println("STATE_CHECK: going to STATE_NIL");
                    state = STATE_NIL;
                }
                break;
            case STATE_STOP_DELAY:
                //logger.log(Level.INFO, "STATE_STOP_DELAY starts...");
                // Deplay to start real function for a while because of the reading delay of sensory info.
                monostable_delay_time = monostable_delay_time + 1;
                if (monostable_delay_time > MONOSTABLE_DELAY_TIME){
                    monostable_delay_time = 0;
                    state = STATE_STOP;
                } else{
                    state = STATE_STOP_DELAY;
                }
                
                break;
            case STATE_STOP:
                //logger.log(Level.INFO, "StopFSM STATE_STOP starts...");

                //System.out.println("STATE_STOP: Hold the arms.");
                commands.put("Arm2_pos", arm2_pos);
                commands.put("Arm3_pos", arm3_pos);
                commands.put("Arm4_pos", arm4_pos);

                
                monostable_max_time = monostable_max_time + 1;
                if (monostable_max_time > MONOSTABLE_MAX_TIME){
                    monostable_max_time = 0;
                    state = STATE_CHECK;
                } else{
                    state = STATE_STOP;
                }

                break;

            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }

        //Hold the beam distance value as last one
        beam_dis_last = beam_distance;
    }

}
