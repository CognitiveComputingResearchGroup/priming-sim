/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules;

import edu.memphis.ccrg.lida.framework.tasks.FrameworkTask;

/**
 * FSM: Finite State Machine
 * @author Daqi
 */
public interface FSM extends FrameworkTask{

    /*
     * Initialize the variables,
     * when the Motor Plan Template was instantiated to the Motor Plan
     */
    @Override
    public void init();

    /*
     * Receive data on running time with one parameter
     */
    public void receiveData(Object o);

    /*
     * Receive data on running time with two parameters
     */
    public void receiveData(Object o1, Object o2);

    /*
     * Receive data on running time with three parameters
     */
    public void receiveData(Object o1, Object o2, Object o3);

    /*
     * Output the command
     */
    public Object outputCommands();

     /*
     * To run the FSM
     */
    public void execute();

}
