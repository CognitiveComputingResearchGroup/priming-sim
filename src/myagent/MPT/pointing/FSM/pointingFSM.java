/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.MPT.pointing.FSM;

import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.SMS.MPT.FSMImpl;
import myagent.SMS.MPT.Grip.GrabFSM;

/**
 *
 * @author Daqi
 */
public abstract class pointingFSM extends FSMImpl{

    //Parameters for task running
    private static final Logger logger = Logger.getLogger(pointingFSM.class.getCanonicalName());

    private int state;

    private static final int STATE_NIL = 0;
    private static final int STATE_MOVE = 1;

    //default moving force (N)
    protected static final double MOVING_FORCE_DEF = 1.0;

    //default direction (90 degrees)
    protected static final double MOVING_DIRECTION_DEF = Math.PI/4;
    
    public pointingFSM(int ticksPerRun) {
        super(ticksPerRun);
    }
    
    @Override
    public void init() {

        state = STATE_NIL;

        commands.put("MotorName", null);
        setMotorName();
        
        commands.put("Force", null);
        commands.put("Direction", null);

    }
    
    public abstract void setMotorName();
    
    @Override
    public void receiveData(Object o) {
        //No impl
    }
    
    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:

                commands.put("Force", null);
                commands.put("Direction", null);
                
                state = STATE_MOVE;
                break;
                
            case STATE_MOVE:
                
                commands.put("Force", MOVING_FORCE_DEF);
                //TODO:Do we need to specify a specific direction degree here,
                //maybe driven by the sensory data passed from sensory memory?
                commands.put("Direction", MOVING_DIRECTION_DEF);
                
                state = STATE_MOVE;
                break;
          
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }
}
