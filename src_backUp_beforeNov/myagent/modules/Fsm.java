/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules;

import edu.memphis.ccrg.lida.framework.shared.Node;

/**
 * FSM: Finite State Machine
 * @author Daqi
 */
public interface Fsm extends Node{

    /*
     * Initialize the variables,
     * when the Motor Plan Template was instantiated to the Motor Plan
     */
    @Override
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
     * To run the FSM
     */
    public void run();

    

}
