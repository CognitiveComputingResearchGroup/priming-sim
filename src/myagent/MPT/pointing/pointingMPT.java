/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.MPT.pointing;

import java.util.Map;

import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.FSM;
import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.MPTension;
import edu.memphis.ccrg.lida.sensorymotormemory.sensorymotorsystem.MPT.SubsumptionMPTImpl;
import java.util.HashMap;
import java.util.logging.Level;
import myagent.modules.PrimingSensoryMotorSystem;

/**
 *
 * @author Daqi
 */
public abstract class pointingMPT extends SubsumptionMPTImpl implements MPTension{
   
    protected FSM theFSM;
    
    protected double tension;
    
    protected boolean behavioralSelected;
    
    protected Map <String, Double> CommandVal = new HashMap <String, Double>();
    
    //default moving force (N)
    public static final double MOVING_FORCE_DEF = 10.0;
    
    //The total force applied to the motors
    //an assumption is that humans apply fixed total force to move its finger, 
    //thus during the medium period (>50% of the total) of the movement, the speed
    //reaches to a fixed max
    //public static final double MAX_TOTOAL_FORCE = 50.0;
    
    //public double current_total_force = 0.0;
        
    //to record the running time of spcify() and update()
    //public int current_t = 1;
    
    //public static final double theta1 = 2.0, theta2 = 10.0, theta3 = 3.0;
    
    //public static final int Ellipse_A = 30, Ellipse_B = 1;

    //default direction in radians (45 degrees)
    public static final double MOVING_DIRECTION_DEF = Math.PI/4;
    
    public static final double TENSION_TO_FORCE_RATE = 0.01;
    
    public double current_adding_t = 1, current_removing_t = 1;
    
    //public int SIGMOID_WIDTH = 25;
    
    //public static final double theta1 = 8.0, theta2 = 100.0;
    
    public static final double TETION_UNIT = 1000.0;
    
    
    
    //private double FOO;

    @Override
    public void init() {
        
        //PointingTopRightFSM = new PointingTopRightFSM(FSM_TICKS_PER_RUN);
        //PointingTopRightFSM.init();
        specifyTheFSM();
        theFSM.init();
        
        //FOO = PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF;
        
        tension = 0.0;
        
        behavioralSelected =false;
        
        CommandVal.put("force", 0.0);
        CommandVal.put("direction", 0.0);
        
    }
    
    public abstract void specifyTheFSM();
    
    public void setTension (double val){
        tension = val;
    }
    
    public double getTension (){
        return tension;
    }
    
    public void addTesion (double val){
        
        if (val > 0.0){
            
            //double addingRate = theta1/(1+Math.pow(Math.E, current_adding_t/SIGMOID_WIDTH));
            
            //current_adding_t += 1;

            //tension = tension + val*addingRate;
            tension = tension + val;
        }
    }
    
    public void removeTension(double val){
        
        if (val >= 0.0 && val < tension){
            
            double removingRate = tension/TETION_UNIT;
                    
            tension = tension - val*removingRate;
        } else{
            tension = 0.0;
        }
        
    }
    
    public void setBehavioralSelected(boolean val){
        behavioralSelected = val;
    }
    
    public boolean getBehavioralSelected(){
        return behavioralSelected;
    }
    
    

    @Override
    public void receiveData(Object o) {
        //no impl
    }

     @Override
    public void specify() {
        //theFSM.specify(PrimingSensoryMotorSystem.MOVING_DIRECTION_DEF);
        
        
        CommandVal.put("direction", MOVING_DIRECTION_DEF);
        
        double force_val = tension*TENSION_TO_FORCE_RATE;
        
        if ((behavioralSelected == true)&& (force_val < MOVING_FORCE_DEF)){
            force_val = MOVING_FORCE_DEF;
        }
        
        CommandVal.put("force", force_val);
        
        theFSM.specify(CommandVal);

    }
    
    @Override
    public void update() {

        CommandVal.put("direction", MOVING_DIRECTION_DEF);
        
        System.out.println("the tension is " + tension);
        
        double force_val = tension*TENSION_TO_FORCE_RATE;
        
        if ((behavioralSelected == true)&& (force_val < MOVING_FORCE_DEF)){
            force_val = MOVING_FORCE_DEF;
        }
        

        /*
        if (behavioralSelected == true)
        {
            System.out.println("update force is" + force_val);

            //tuning the force using a sigmoid function
            //force_val *= theta1/(1 + Math.pow(Math.E, Math.abs(current_t - theta2)/theta3));
            
            if (current_t < 2*Ellipse_A){
                //tuning the force using a ellipse function
                double current_B, rate;

                current_B = (force_val/(MOVING_FORCE_DEF*2))*Ellipse_B;

                //current_B = Ellipse_B;

                //rate = Math.sqrt(Math.pow(current_B, 2) - 
                //        (Math.pow(current_B, 2)/Math.pow(Ellipse_A, 2))*Math.pow(current_t - Ellipse_A, 2));
                rate = current_B*Math.sqrt(1- Math.pow((current_t-Ellipse_A)/Ellipse_A, 2));

                
                System.out.println("current_B->" + current_B + " current_t->" + current_t + " rate->" + rate);
                
                
                force_val*= rate;
                
                current_t += 1;
                
                
            } else{
                force_val = 0.0;
            }
            
            if (force_val < MOVING_FORCE_DEF)
                force_val = MOVING_FORCE_DEF;

            System.out.println("new update force is " + force_val);


        }
                */
        
        CommandVal.put("force", force_val);
        
        theFSM.update(CommandVal);
    }

    @Override
    public void onlineControl() {
       
        taskSpawner.addTask(theFSM);
        
        //commands = (Map<String, Object>) theFSM.outputCommands();
    }
    
    @Override
    public Object outputCommands() {
        //Since there is only one FSM running in this MPT, here we directly return the FSM's output
        return theFSM.outputCommands();
    }

    @Override
    public Object dorsal_MotorValueOf(String cmdName) {
        //no impl
        return -1.0;
    }

    @Override
    public Object behavior_MotorValueOf(String cmdName) {
        // no impl
        return -1.0;
    }
}
