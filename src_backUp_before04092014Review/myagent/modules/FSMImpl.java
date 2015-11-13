/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules;

import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import java.util.HashMap;
import java.util.Map;


/**
 * Basic implement of FSM
 * @author Daqi
 */
public abstract class FSMImpl extends FrameworkTaskImpl
		implements FSM {
   
    protected Map<String, Object> sensedData = new HashMap<String, Object>();
    
    protected Map<String, Object> commands = new HashMap<String, Object>();

    public FSMImpl(int ticksPerRun) {
        super(ticksPerRun);
    }

    @Override
    public void receiveData(Object o) {
        // No Impl
    }

    @Override
    public void receiveData(Object o1, Object o2) {
        // No Impl
    }

    @Override
    public void receiveData(Object o1, Object o2, Object o3) {
        // No Impl
    }

    @Override
    public Object outputCommands() {
        return commands;
    }

    /*
     * The specify() method is optional to a FSM instance
     */
    @Override
    public void specify(Object o) {
        //No Impl
    }

    /*
     * The update() method is optional to a FSM instance
     */
    @Override
    public void update(Object o) {
        //No Impl
    }

    @Override
    protected void runThisFrameworkTask() {
            execute();
    }

    @Override
    public abstract void execute();

}
