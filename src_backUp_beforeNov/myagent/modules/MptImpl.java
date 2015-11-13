/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules;

import edu.memphis.ccrg.lida.framework.shared.activation.LearnableImpl;

/**
 * This basic MPT is designed based on the idea of Subsumption Architecture
 * @author Daqi
 */
public class MptImpl extends LearnableImpl implements Mpt{

    private Fsm FSM1, FSM2;

    private Object commands;

    @Override
    public void init(){
        FSM1 = new FsmImpl();
        FSM2 = new FsmImpl();
        
    }

    @Override
    public void load() {
        //TODO: Impl
    }

    @Override
    public void save() {
        //TODO: Impl
    }

    /*
     * Suppress: Replace the lower-level FSM's output
     */
    public Object suppress(Object HigherFSMOutput, Object LowerFSMOutput){
        if (HigherFSMOutput != null)
            return HigherFSMOutput;
        else if (LowerFSMOutput != null)
            return LowerFSMOutput;
        else
            return null;
    }

    /*
     * Inhibit: Stop the lower-level FSM's output
     */
    public Object inhibit(Object HigherFSMOutput, Object LowerFSMOutput){
        if (HigherFSMOutput != null)
            return null;
        else if (LowerFSMOutput != null)
            return LowerFSMOutput;
        else
            return null;
    }


    public void receiveData(Object o) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object outputCommands() {
        return commands;
    }

}
