package myagent.modules;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jmatio.types.MLDouble;
import com.jmatio.io.MatFileWriter;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;

import java.util.ArrayList;
import java.io.File;

import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    //public static final double MOTOR_POWER = 1;
    
    
    public static final double MASS = 2000.0;

    public double XS_total = 0.0, YS_total = 0.0;
    
    public static final double MAX_Y = 0.35;
    
    public static final double MAX_FORCE = MAX_Y*MASS;
    public double current_total_force = 0.0;
    
    ///////////////For the output of experimental results//////////////////
    private final static int MAX_TICK_SIZE = 1000;
    
    private final static double TARGET_X = ENVIRONMENT_WIDTH*5/6;
    private final static double TARGET_y = ENVIRONMENT_WIDTH/6;
    
    //two data in one item: distance and tick
    double[][] distanceData = new double[MAX_TICK_SIZE][2];
    
    int arrayIndex = 0;
    
    boolean ouputdata_flag = true;


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
        
        ouputdata_flag = true;
        
        for (int i = 0; i< MAX_TICK_SIZE; i++){
            distanceData[i][0] = 0.0;
            distanceData[i][1] = 0.0;
        }
        
        arrayIndex = 0;
        
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
                synchronized(this) {
                    if (ouputdata_flag == true){

                        //This flag must to be switched in the beginning of this code block
                        //since this method may be called multiply in parallel 
                        ouputdata_flag = false;

                        //For producing the experiment result
                        System.out.println("Creating the output task...");
                        generateDistanceToTarget t1 = new generateDistanceToTarget();
                        taskSpawner.addTask(t1);
                    }
                }
                
                return targetData;
        }

              
    }
                
    @Override
    public void processAction(Object commands) {

        String motorName;

        double force, direction;
        
        double t = 1.0;//the time duration (sec)
        
        Map <String, Double> speeds = new HashMap <String, Double> ();
        
        double XS_current = 0.0, YS_current = 0.0;
        
        speeds.put("xS", 0.0);
        speeds.put("yS", 0.0);
        
        double tmp_total_force = 0.0;
        
        for (Object theCmd: ((Map)commands).values()){
            motorName = (String)((Map<String, Object>) theCmd).get("MotorName");

            force = (Double)((Map<String, Object>) theCmd).get("Force");
            direction = (Double)((Map<String, Object>) theCmd).get("Direction");
            
            //System.out.println("output:: " + motorName + "-->" + force);
            
            if (motorName.equals(UPPER_MOTOR_NAME)){
            
                speeds = upperMotorControl(force, direction);
                
                tmp_total_force +=force;

            } else if (motorName.equals(LOWER_MOTOR_NAME)){
                speeds = lowerMotorControl(force, direction + Math.PI);
                
                tmp_total_force -=force;

            } else{
                logger.log(Level.WARNING, "Required motor is not available!", TaskManager.getCurrentTick());
            }
            
            XS_current += speeds.get("xS");
            YS_current += speeds.get("yS");
            
            
        
        }
        
        //System.out.println("current_total_force is " + current_total_force + "and tmp_total_force is " + tmp_total_force);
        
        double total_f = current_total_force + tmp_total_force;

        if ( Math.abs(total_f) <= MAX_FORCE){
            
            XS_total += XS_current;
            YS_total += YS_current;
            
            current_total_force = total_f;
            
        }

        //The orginal point of the canvas is on the top left
        //Thus, x dim increases while y decreases
        //System.out.println("XS_total is " + XS_total + " and YS_total " + YS_total);
        
        /*
        if (XS_total > MAX_Y)
            XS_total = MAX_Y;
        else if (XS_total < (0-MAX_Y))
            XS_total = 0-MAX_Y;
        
        if (YS_total > MAX_Y)
            YS_total = MAX_Y;
        else if (YS_total < (0 - MAX_Y))
            YS_total = 0-MAX_Y;
        */
        
        
        //System.out.println("new XS_total is " + XS_total + " and XS_current " + XS_current + " in " + TaskManager.getCurrentTick());
        
        
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
        
        //System.out.println("xS: " + xS + "yS: " + yS);
        
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
        
        //
        speed = (force/MASS);
        
        return speed;
        
    }

    @Override
    public void resetState() {
            // TODO Auto-generated method stub

    }
    
    
    private class generateDistanceToTarget extends FrameworkTaskImpl {


        @Override
        protected void runThisFrameworkTask() {
            
            double distance = 0.0, tick = 0;

            distance = Math.sqrt(Math.pow((TARGET_X - p.x), 2) + Math.pow((TARGET_y - p.y), 2));

            tick = TaskManager.getCurrentTick();

            //System.out.println("dis:" + distance + " tick:" + tick);

            if (arrayIndex < MAX_TICK_SIZE){
                
                 if (arrayIndex > 0){//if there is at least one item in the array
                    double lastTick = distanceData[arrayIndex - 1][1];

                    //System.out.println("arrayIndex is " + arrayIndex + " lastTick:" + lastTick + " tick:" + tick);

                    if (lastTick == tick){//ignore the repeated tick

                        //System.out.println("arrayIndex is " + arrayIndex + " returnred tick is " + tick);

                        return;
                    }
                }
                
                //System.out.println("arrayIndex is " + arrayIndex);
                distanceData[arrayIndex][0] = distance;
                distanceData[arrayIndex][1] = tick;

                //System.out.println("dis:" + distance + " tick:" + tick);
                
                if (distance < 1.0){//to make the next one to be the time to record data
                    arrayIndex = MAX_TICK_SIZE - 1;
                }

                arrayIndex = arrayIndex + 1;
                
            } else {// (arrayIndex >= MAX_TICK_SIZE)
                System.out.println("Saving the distance data...");
                
                //Save the distance data in the format of Matlab
                MLDouble mlDouble1 = new MLDouble("distance", distanceData);

                ArrayList list1 = new ArrayList();

                list1.add(mlDouble1);
                
                /*
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                Date date = new Date();
                
                String dateName =  dateFormat.format(date);
                */

                String dateName = Long.toString(System.currentTimeMillis());

                try{
                    new MatFileWriter(".\\data\\dis" + blankDuration + "\\dis" + blankDuration + "_" +dateName+ ".mat", list1);
                }
                catch(IOException e)
                {
                    System.out.println("distanceData IOError");
                }
                
                System.out.println("The distance data has been saved!");
                
                cancel();

            }

            

        }
        
    }

}
