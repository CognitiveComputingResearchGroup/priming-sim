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
public class EgyptFSM extends ArmsFSMImpl {

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(EgyptFSM.class.getCanonicalName());
    
    private int state;
    private static final int STATE_NIL = 0;
    private static final int STATE_STAY = 1;

    private static final double arm2_initPos = -0.18;
    private static final double arm3_initPos = -1.19;
    private static final double arm4_initPos = -1.77;

    public EgyptFSM(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void init() {

        // Call ArmsFSM.init()
        super.init();
        
        state = STATE_NIL;

    }

    @Override
    public void execute() {
        switch (state) {
            case STATE_NIL:
                state = STATE_STAY;
                break;
            case STATE_STAY:
                commands.put("Arm2_pos", arm2_pos);
                commands.put("Arm3_pos", arm3_pos);
                commands.put("Arm4_pos", arm4_pos);
                //System.out.println("Stuck pos:" + arm2_pos + " : " + arm3_pos + " : " + arm4_pos);
                
                state = STATE_STAY;
                break;
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }
}
