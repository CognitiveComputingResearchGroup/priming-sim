/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules;

import edu.memphis.ccrg.lida.framework.tasks.TaskSpawner;

/**
 * MPT is a certain data structure inside Sensory Motor Memory. It involves
 * executable elements, such as FSM, a kind of FrameworkTask. Therefore we
 * need a receiveTS method to reuse the TS of SMM.
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
     * To run the MPT
     * TODO: Maybe it's necessary to divide run() to specification() and onlineControl()
     */
    public void run();


    public void receiveTS(TaskSpawner ts);

}
