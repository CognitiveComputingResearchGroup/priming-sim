/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myagent.SMS;

import edu.memphis.ccrg.lida.sensorymotormemory.BasicSensoryMotorMemory;
import myagent.SMS.MPT.MPT;

/**
 *
 * @author Daqi
 */
public abstract class SensoryMotorSystem extends BasicSensoryMotorMemory{
    
    //Load possible motor plan templates (MPTs)
    //the conctete data structure of the variable that store the MPTs need to 
    //be defined in the concrete method
    public abstract void loadMPT();
    
    //select a proper motor plan tempate (MPT) based on the selected behavior, a algorithem defined here
    public abstract MPT selectMPT(Object alg);
    
}
