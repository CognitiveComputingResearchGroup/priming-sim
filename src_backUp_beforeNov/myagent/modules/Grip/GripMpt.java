/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules.Grip;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.modules.Fsm;
import myagent.modules.MptImpl;

/**
 *
 * @author Daqi
 */
public class GripMpt extends MptImpl{
    private static final Logger logger = Logger.getLogger(GripMpt.class.getCanonicalName());
    
    private Fsm openFsm, grabFsm, depositFsm;

    private boolean beam;
    private double wrist_force;

    //private Map<String, Object> commands = new HashMap<String, Object>();

    private Object commands;

    //private static final double CMD_UNDEFINED = -1.0;

    @Override
    public void init(){
        //TODO: Here we just built-in the FSM and their relationship as a
        // subsumption architecture. Actually, it's better to store the set of
        // FSM and their relationship in a long-term memory as a template and
        // load it when use it.
        openFsm = new OpenGrippersFsm();
        grabFsm = new GrippersGrabFsm();
        depositFsm = new DepositGrippersFsm();

        openFsm.init();
        grabFsm.init();
        depositFsm.init();

        beam = false;
        wrist_force = 0.0;

        commands = null;

    }

    @Override
    public void load() {
        //TODO: Impl
    }

    @Override
    public void save() {
        //TODO: Impl
    }
   
    @Override
    public void receiveData(Object o) {
        Map<String, Object> data = (HashMap) o;
        beam = (Boolean) data.get("beam_broken");
        wrist_force = (Double) data.get("wrist_force");

        //logger.log(Level.INFO, "GripMPT:The wrist force is: " + wrist_force);
        
        // Input online data to FSMs
        grabFsm.receiveData(beam);
        depositFsm.receiveData(wrist_force);
    }

    @Override
    public Object outputCommands() {
        return commands;
    }

    @Override
    public void run() {
        //Following the design principles of robot Herbert
        //"A Behavior-Based Arm Controller" (1988 Jonathan H. Connell)

        System.out.println("MPT running!");
        
        // Do these FSMs need to be run in parallel?

        depositFsm.run();
        
        grabFsm.run();
        
        openFsm.run();

        // 'Deposit' suppresses 'grab' and their result suppresses 'open'.
        commands = suppress(suppress(depositFsm.outputCommands(), grabFsm.outputCommands()),
                openFsm.outputCommands());

        Object depositCommands = depositFsm.outputCommands();

        Object grabCommands = grabFsm.outputCommands();

        Object openCommands = openFsm.outputCommands();

        Object higherCmd = suppress(depositCommands,grabCommands);

        commands = suppress(higherCmd, openCommands);

        /*
        System.out.println("depositCommands is:" + depositCommands);

        System.out.println("grabCommands is:" + grabCommands);

        System.out.println("openCommands is:" + openCommands);

        System.out.println("higherCmd is:" + higherCmd);
         * 
         */

        if (commands == null)
            System.out.println("MPT output command is NULL");
        else
            System.out.println("MPT output command is:" + commands);

    }
}
