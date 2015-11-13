/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules;

import edu.memphis.ccrg.lida.framework.shared.activation.Learnable;

/**
 * MPT: Motor Plan Template
 * @author Daqi
 */
public interface Mpt extends Learnable{

    @Override
    public void init();
    
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

}
