/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.SMS.MPT;

import edu.memphis.ccrg.lida.framework.tasks.TaskSpawner;

/**
 * A Motor Plan Template (MPT) is a certain data structure inside Sensory Motor Memory.
 * A MPT becomes a Motor Plan (MP) after it was specified; and a MP is executable
 * in a online control process
 * @author Daqi
 */
public interface MPT{

   
    /*
     * Store MPT in long-term memory
     */
    public void load();

    public void save();

    public void init();

    /*
     * Receive data on running time
     */
    public void receiveData(Object o);

    /*
     * Output the command
     */
    public Object outputCommands();

    /*
     * To specify a MPT to a MP
     */
    public void specify();

    /*
     * To update the command's values in running time
     */
    public void update();
    
    /*
     * Starting to run a Motor Plan (MP), specified MPT
     */
    public void onlineControl();


    /*
     * Collection of mapping functions from sensed data through dorsal stream to motor command values
     */
    public Object dorsal_MotorValueOf(String cmdName);

    /*
     * Collection of mapping functions from a selected behavior (context) to motor command values
     */
    public Object behavior_MotorValueOf(String cmdName);


    /*
     * A MPT involves executable elements, such as FSM, a kind of FrameworkTask. 
     * Therefore we need a receiveTS method to reuse the TS of SMM.
     */
    public void receiveTS(TaskSpawner ts);

}
