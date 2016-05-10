package myagent.modules;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrimingEnvironment extends EnvironmentImpl{

    private static final Logger logger = Logger.getLogger(PrimingEnvironment.class.getCanonicalName());
    public static final int FIXATION_PERIOD=1;
    public static final int PRIME_DURATION=10;

    public static final int ENVIRONMENT_WIDTH = 100;
    public static final int ENVIRONMENT_HEIGHT = 100;

    private Map<String, Object> blankData= new HashMap<String, Object>();
    private Map<String, Object> targetData= new HashMap<String, Object>();
    private Map<String, Object> consistentPrimingData= new HashMap<String, Object>();
    private Map<String, Object> inconsistentPrimingData= new HashMap<String, Object>();
    
    public Point2d p=new Point2d(PrimingEnvironment.ENVIRONMENT_HEIGHT/2,PrimingEnvironment.ENVIRONMENT_WIDTH/2);
    private int blankDuration=0;
    
    //Define two motors to conrol a simulated "finger" to execute pointing action
    //-- upper motor pull the finger moving to up, here it is top right area
    //-- lower motor pull the finger moving to down, here it is bottom left area
    public static final String UPPER_MOTOR_NAME = "upper_motor";
    public static final String LOWER_MOTOR_NAME = "lower_motor";
    
    //The power of the motor represents the speed the movement could be.
    //Its range is 1~10, higher is stronger (quicker)
    public static final double MOTOR_POWER = 1;


    @Override
    public void init(){
    blankDuration=(int) getParam("blankDuration", 10);

    blankData.put("dot_color","white");
    blankData.put("dot_Xpos", 1);
    blankData.put("dot_Ypos", 1);


    targetData.put( "red",true);
    targetData.put( "green",true);
    targetData.put("red_position", 1);
    targetData.put("green_position", 3);

    consistentPrimingData.put("red", true);
    consistentPrimingData.put("green", true );
    consistentPrimingData.put("red_position", 1);
    consistentPrimingData.put("green_position", 3);

    inconsistentPrimingData.put( "red",true);
    inconsistentPrimingData.put( "green",true);
    inconsistentPrimingData.put("red_position", 3);
    inconsistentPrimingData.put("green_position", 1);
    }

    @Override
    public Object getState(Map<String, ?> arg0) {
        
        if(TaskManager.getCurrentTick()<=FIXATION_PERIOD){
                return blankData;
        }
        else if(TaskManager.getCurrentTick()>FIXATION_PERIOD && TaskManager.getCurrentTick()<=(PRIME_DURATION+FIXATION_PERIOD)){
                if((boolean) getParam("consistent", true)){
                 return consistentPrimingData;	
                }
                else{
                        return inconsistentPrimingData;
                }
        }
        else if(TaskManager.getCurrentTick()>(PRIME_DURATION+FIXATION_PERIOD) && TaskManager.getCurrentTick()<=(blankDuration+PRIME_DURATION+FIXATION_PERIOD)){
                return blankData;
        }
        else{
                return targetData;
        }
    }

    @Override
    public void processAction(Object commands) {

        String motorName;

        double force, direction;
        
        double XS_total = 0.0, YS_total = 0.0;
        
        double t = 1.0;//the time duration (sec)
        
        Map <String, Double> speeds = new HashMap <String, Double> ();
        
        speeds.put("xS", 0.0);
        speeds.put("yS", 0.0);
        
        for (Object theCmd: ((Map)commands).values()){
            motorName = (String)((Map<String, Object>) theCmd).get("MotorName");

            force = (Double)((Map<String, Object>) theCmd).get("Force");
            direction = (Double)((Map<String, Object>) theCmd).get("Direction");
            
            if (motorName.equals(UPPER_MOTOR_NAME)){
            
                speeds = upperMotorControl(force, direction);

            } else if (motorName.equals(LOWER_MOTOR_NAME)){
                speeds = lowerMotorControl(force, direction + Math.PI);

            } else{
                logger.log(Level.WARNING, "Required motor is not available!", TaskManager.getCurrentTick());
            }
            
            XS_total += speeds.get("xS");
            YS_total += speeds.get("yS");
        
        }

        //The orginal point of the canvas is on the top left
        //Thus, x dim increases while y decreases
    	p.x= p.x + XS_total * t;
    	p.y= p.y - YS_total * t;
        
        //System.out.println("p.x is " + p.x + " and p.y os " + p.y);

    }
           
    
    private Map<String, Double> upperMotorControl(double forceMag, double forceDirection){
        
        double xF, yF, xS, yS;
        
        Map <String, Double> output_speed = new HashMap <String, Double> ();
        
        //System.out.println("UPMotor: forceDirection is " + forceDirection);
        
        xF = Math.cos(forceDirection)*forceMag;
        yF = Math.sin(forceDirection)*forceMag;
        
        //System.out.println("xF: " + xF + "yF: " + yF);
        
        xS = force2speed(xF);
        yS = force2speed(yF);
        
        output_speed.put("xS", xS);
        output_speed.put("yS", yS);
        
        return output_speed;

    }

    private Map<String, Double> lowerMotorControl(double forceMag, double forceDirection){
        
    	double xF, yF, xS, yS;
        
        Map <String, Double> output_speed = new HashMap <String, Double> ();
        
        //System.out.println("LOWERMotor: forceDirection is " + forceDirection);
        
        xF = Math.cos(forceDirection)*forceMag;
        yF = Math.sin(forceDirection)*forceMag;
        
        //System.out.println("xF: " + xF + "yF: " + yF);
        
        xS = force2speed(xF);
        yS = force2speed(yF);
        
        output_speed.put("xS", xS);
        output_speed.put("yS", yS);
        
        return output_speed;
   
    }
    
    private double force2speed(double force){
        //This is suppose to simulate an engine that translate force to speed
        
        double speed;
        
        //here we just use a 'fake' fomula
        speed = (MOTOR_POWER/50)*force;
        
        return speed;
        
    }

    @Override
    public void resetState() {
            // TODO Auto-generated method stub

    }

}
