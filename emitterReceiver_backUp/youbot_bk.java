// File:          youbot.java
// Date:          07/27/2013
// Description:   This is the controller used to link the robot and start the LIDA Framework
// Author:        Daqi             
// Modifications:                            
         
import com.cyberbotics.webots.controller.Robot;
import edu.memphis.ccrg.lida.framework.initialization.AgentStarter;

import com.cyberbotics.webots.controller.Emitter;
import com.cyberbotics.webots.controller.Receiver;


// Controller class
public class youbot extends Robot{

  private static final int TIME_STEP = 64;
  
  private Emitter emitter1;
  private Receiver receiver1;
  private static final String REQUEST = "beep";

   
  // Constructor  
  public youbot() {
      
    // call the Robot constructor
    super();  
    
    emitter1 = getEmitter("emitter");
    
    receiver1 = getReceiver("receiver");
    receiver1.enable(TIME_STEP);
    
    int ech = emitter1.getChannel();
    
    int rch = receiver1.getChannel();
    
    System.out.println("The channel for both emitter and receiver are:" + ech+ " and " + rch);
 
  } 
   
  public void run(){
  
      try{
        emitter1.send(REQUEST.getBytes("US-ASCII"));
      }catch (java.io.UnsupportedEncodingException e) {
        System.out.println(e);
      }
    
    do {
    
      // Beam::Receive data
      if (receiver1.getQueueLength() != 0){
        System.out.println("Ready");
        //Clear the packet in the reception queue
        while (receiver1.getQueueLength() != 0)
          receiver1.nextPacket();
          
      } else{
        System.out.println("Broken!");
      }
      
           
      // Beam:: Send data
      //emitter1.send("beep");
      try{
        emitter1.send(REQUEST.getBytes("US-ASCII"));
      }catch (java.io.UnsupportedEncodingException e) {
        System.out.println(e);
      }
      
    } while (step(TIME_STEP) != -1);
  }
    
  public static void main(String[] args) {   
    
    // Start the LIDA Framework
    //AgentStarter.main(args);
    
    youbot agent = new youbot();
    agent.run();
    
  }
}
