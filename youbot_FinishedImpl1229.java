// File:          youbot.java
// Date:          07/27/2013
// Description:   This is the controller used to link the robot and start the LIDA Framework
// Author:        Daqi             
// Modifications:                            
         
import com.cyberbotics.webots.controller.Robot;
import edu.memphis.ccrg.lida.framework.initialization.AgentStarter;

import com.cyberbotics.webots.controller.DistanceSensor;
import com.cyberbotics.webots.controller.Servo;


// Controller class
public class youbot extends Robot{

  private static final int TIME_STEP = 32;
  
  private DistanceSensor distance1;
  
  private double distanceVal, servoPos1, servoPos2, servoForce;
  
  private Servo gripper1, gripper2;
  
  
   
  // Constructor  
  public youbot() {
      
    // call the Robot constructor
    super();  
     
    distance1 = getDistanceSensor("beam");
    
    distance1.enable(TIME_STEP);
    
    gripper1 = getServo("finger1");
    gripper2 = getServo("finger2");
    
    gripper1.enablePosition(TIME_STEP);
    gripper2.enablePosition(TIME_STEP);
    
  } 
   
  public void run(){
  
    
    do {
    
      // Beam::Receive data
      
      servoForce = gripper1.getMotorForceFeedback();
      System.out.println("Gripper1 force = " + servoForce);
      
      distanceVal = distance1.getValue();
      
      //System.out.println("Distance = " + distanceVal);
      
      servoPos1 = gripper1.getPosition();
      servoPos2 = gripper2.getPosition();
      
      System.out.println("Pos = " + servoPos1 + " and " + servoPos2);
      
      gripper1.setPosition(0.0);
      
      gripper2.setPosition(0.0);
      
    } while (step(TIME_STEP) != -1);
  }
    
  public static void main(String[] args) {   
    
    // Start the LIDA Framework
    AgentStarter.main(args);
    
    //youbot agent = new youbot();
    //agent.run();
    
  }
}
