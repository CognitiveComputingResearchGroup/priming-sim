/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules;

import edu.memphis.ccrg.lida.framework.shared.ExtendedId;
import edu.memphis.ccrg.lida.framework.shared.Node;
import edu.memphis.ccrg.lida.framework.strategies.DecayStrategy;
import edu.memphis.ccrg.lida.framework.strategies.ExciteStrategy;
import edu.memphis.ccrg.lida.pam.PamNode;
import java.util.Map;

/**
 * Basic implement of FSM
 * @author Daqi
 */
public class FsmImpl implements Fsm {

    private int state;
    private static final int STATE_NIL = 0;
    //Add more states for a certain FSM, e.g., STATE_PLAN, STATE_START, and so on.

    private Object commands;

    @Override
    public void init() {
        state = STATE_NIL;

        //Undefined commands
        commands = null;
    }

    @Override
    public void receiveData(Object o) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object outputCommands() {
        return commands;
    }

    @Override
    public void run() {
        switch (state){
            case STATE_NIL:
                commands = new Object();
                break;
            default:
                break;
        }
    }

    // The methods below are overridden for interface Node
    @Override
    public PamNode getGroundingPamNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setGroundingPamNode(PamNode pn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setId(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLabel(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNodeValues(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ExtendedId getExtendedId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFactoryType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFactoryType(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getActivation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setActivation(double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getTotalActivation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void excite(double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setExciteStrategy(ExciteStrategy es) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ExciteStrategy getExciteStrategy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void decay(long l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDecayStrategy(DecayStrategy ds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DecayStrategy getDecayStrategy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setActivatibleRemovalThreshold(double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getActivatibleRemovalThreshold() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRemovable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(Map<String, ?> map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T getParam(String string, T t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsParameter(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, ?> getParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getConditionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
