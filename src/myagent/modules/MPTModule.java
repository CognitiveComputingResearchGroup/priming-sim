/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules;

import edu.memphis.ccrg.lida.framework.FrameworkModule;
import edu.memphis.ccrg.lida.framework.tasks.TaskSpawner;

/**
 * A {@link FrameworkModule} that manages {@link FSM}s
 * @author Daqi
 */
public interface MPTModule extends FrameworkModule{

   
    /*
     * Store MPT in long-term memory
     */
    public void load();

    public void save();

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

    /*
     * This method receives the available taskspawner for running tasks.
     * TODO: This is just a temporary way to take TS. It should be done by
     * utilizing SMM as the holder of FSMs directly so no need to deliver TS.
     * Specifically, there are two ways:
     * 1) Using SMM as current MPTModule and using SMMInitializer as SMM
     * 2) Confige MPTModule as a submodule of SMM and share TS to it
     *
     */
    public void receiveTS(TaskSpawner ts);

}
