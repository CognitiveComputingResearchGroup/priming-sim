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

import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.FSMImpl;

import myagent.modules.PrimingSensoryMotorSystem;

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
    
    protected double moveing_force, moving_direction;
    
    public pointingFSM(int ticksPerRun) {
        super(ticksPerRun);
    }
    
    @Override
    public void init() {

        state = STATE_NIL;
        
        moveing_force = 0.0;
        
        moving_direction = 0.0;

        commands.put("MotorName", null);
        //Fixed force
        commands.put("Force", PrimingSensoryMotorSystem.MOVING_FORCE_DEF);
        commands.put("Direction", 0.0);
        
        setMotorName();

    }
    
    public abstract void setMotorName();
    
    @Override
    public void receiveData(Object o) {
        //No impl
    }
    
    @Override
    public void specify(Object o){
        //this Object o supose to pass the direction value to this FSM
        moving_direction = (Double) o;

    }
    
    @Override
    public void update(Object o){
        //this Object o supose to pass the direction value to this FSM
        moving_direction = (Double) o;

    }
    
    @Override
    public void execute() {
        switch (state){
            case STATE_NIL:

                //commands.put("Force", null);
                commands.put("Direction", 0.0);
                
                state = STATE_MOVE;
                break;
                
            case STATE_MOVE:
                
                //commands.put("Force", moveing_force);
                //TODO:Do we need to specify a specific direction degree here,
                //maybe driven by the sensory data passed from sensory memory?
                commands.put("Direction", moving_direction);
                
                state = STATE_MOVE;
                break;
          
            default:
                logger.log(Level.SEVERE, "Invalid state.", TaskManager.getCurrentTick());
                break;
        }
    }
}
