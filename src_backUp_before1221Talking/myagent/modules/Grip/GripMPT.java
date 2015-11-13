/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules.Grip;

import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.modules.FSM;
import myagent.modules.SubsumptionMPT;

/**
 *
 * @author Daqi
 */
public class GripMPT extends SubsumptionMPT{
    private static final Logger logger = Logger.getLogger(GripMPT.class.getCanonicalName());

    private final int FSM_TICKS_PER_RUN = 1;
    private final int SUPPRESS_TICKS_PER_RUN = 1;
    private final int WIRE_TICKS_PER_RUN = 1;
    
    private FSM openFSM, grabFSM, depositFSM;

    private ArmsFSM descendFSM, egyptFSM, bounceFSM, surfaceFSM, stopFSM, homeFSM,
            overFSM, extendFSM;

    private suppress S_Grab_Open, S_Deposit_Grab, S_Descend_Egypt, S_Bounce_Home,
            S_Surface_Descend, S_Bounce_Stop, S_Home_Extend, S_Over_Stop, S_Extend_Surface;

    private boolean beam, grabbing, wrist_force_active, touching1, touching2, IR_crossed;

    private double arm2_pos, arm3_pos, arm4_pos;

    private Map<String, Object> grippers_commands = new HashMap<String, Object>();

    private Map<String, Object> arms_commands = new HashMap<String, Object>();

    private Map<String, Object> base_commands = new HashMap<String, Object>();


    @Override
    public void init(){
       //Input
        beam = false;
        grabbing = false;
        wrist_force_active = false;
        touching1 = false;
        touching2 = false;
        arm2_pos = 0.0;
        arm3_pos = 0.0;
        arm4_pos = 0.0;
        IR_crossed = false;

        //Output
        //1. Hand
        grippers_commands.put("Left_Gripper_pos", null);
        grippers_commands.put("Right_Gripper_pos", null);

        //2. Arms
        arms_commands.put("Arm1_pos", null);
        arms_commands.put("Arm2_pos", null);
        arms_commands.put("Arm3_pos", null);
        arms_commands.put("Arm4_pos", null);
        arms_commands.put("Arm5_pos", null);

        //3. Base
        base_commands.put("base_wheel1", null);
        base_commands.put("base_wheel2", null);
        base_commands.put("base_wheel3", null);
        base_commands.put("base_wheel4", null);

        // FSMs and Suppress Nodes
        // 1. Hand
        // 1) FSMs
        openFSM = new OpenFSM(FSM_TICKS_PER_RUN);
        openFSM.init();

        grabFSM = new GrabFSM(FSM_TICKS_PER_RUN);
        grabFSM.init();

        depositFSM = new DepositFSM(FSM_TICKS_PER_RUN);
        depositFSM.init();

        // 2) Suppress Nodes
        S_Grab_Open = new suppress(SUPPRESS_TICKS_PER_RUN);
        
        S_Deposit_Grab = new suppress(SUPPRESS_TICKS_PER_RUN);

        // 2. Arm
        // 1) FSMs
        homeFSM = new HomeFSM(FSM_TICKS_PER_RUN);
        homeFSM.init();

        descendFSM = new DescendFSM(FSM_TICKS_PER_RUN);
        descendFSM.init();

        egyptFSM = new EgyptFSM(FSM_TICKS_PER_RUN);
        egyptFSM.init();

        bounceFSM = new BounceFSM(FSM_TICKS_PER_RUN);
        bounceFSM.init();

        surfaceFSM = new SurfaceFSM(FSM_TICKS_PER_RUN);
        surfaceFSM.init();

        stopFSM = new StopFSM(FSM_TICKS_PER_RUN);
        stopFSM.init();

        homeFSM = new HomeFSM(FSM_TICKS_PER_RUN);
        homeFSM.init();

        overFSM = new OverFSM(FSM_TICKS_PER_RUN);
        overFSM.init();

        extendFSM = new ExtendFSM(FSM_TICKS_PER_RUN);
        extendFSM.init();

        // 2) Suppress Nodes
        S_Descend_Egypt = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Bounce_Home = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Surface_Descend = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Bounce_Stop = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Home_Extend = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Over_Stop = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Extend_Surface = new suppress(SUPPRESS_TICKS_PER_RUN);

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
        grabbing = (Boolean) data.get("grabbing");
        wrist_force_active = (Boolean) data.get("wristForce_active");
        touching1 = (Boolean) data.get("toucher1_active");
        touching2 = (Boolean) data.get("toucher2_active");
        arm2_pos = (Double) data.get("arm2_pos");
        arm3_pos = (Double) data.get("arm3_pos");
        arm4_pos = (Double) data.get("arm4_pos");
        IR_crossed = (Boolean) data.get("IR_Crossed");

        //1. Hand
        //1) No input to Open FSM

        //2) Input beam-broken Info. to Grab FSM
        grabFSM.receiveData(beam);

        //3) Input wrist force Info. and the side of hand to Deposit FSM
        depositFSM.receiveData(wrist_force_active, touching1, touching2);

        // 2. Arm
        //1) Input grabbing Info. to Home FSM
        homeFSM.receiveData(grabbing);

        //2) Input arms positions Info. to Descend FSM
        descendFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //3) Input arms positions Info. to Egypt FSM
        egyptFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //4-1) Input arms positions Info. to Bounce FSM
        bounceFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //4-2 Input the tips activation situation to Bounce FSM
        // Note that there are two channels sending Info. to Bounce FSM
        bounceFSM.receiveData(touching1, touching2);


        //5-1) Input arms positions Info. to Bounce FSM
        surfaceFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //5-2) Input the tips activation situation to Bounce FSM
        // Note that there are two channels sending Info. to Bounce FSM
        surfaceFSM.receiveData(wrist_force_active, touching1, touching2);

        //6-1) Input arms positions Info. to Stop FSM
        stopFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //6-2) Input the tips activation situation to Stop FSM
        // Note that there are two channels sending Info. to Stop FSM
        stopFSM.receiveData(beam);

        //7) Input arms positions Info. to Home FSM
        homeFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //8-1) Input arms positions Info. to Over FSM
        overFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //8-2) Input the IR Crossed Info. to Over FSM
        // Note that there are two channels sending Info. to Stop FSM
        overFSM.receiveData(IR_crossed);

        //9-1) Input arms positions Info. to Extend FSM
        extendFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //9-2) Input the IR Crossed Info. to Extend FSM
        // Note that there are two channels sending Info. to Stop FSM
        extendFSM.receiveData(IR_crossed);

    }

    @Override
    public Object outputCommands() {

        // Output the commands of Grippers, Arms, and Base together
        // Grippers
        commands.put("Left_Gripper_pos", grippers_commands.get("Left_Gripper_pos"));
        commands.put("Right_Gripper_pos", grippers_commands.get("Left_Gripper_pos"));

        //Arms
        commands.put("Arm1_pos", arms_commands.get("Arm1_pos"));
        commands.put("Arm2_pos", arms_commands.get("Arm2_pos"));
        commands.put("Arm3_pos", arms_commands.get("Arm3_pos"));
        commands.put("Arm4_pos", arms_commands.get("Arm4_pos"));
        commands.put("Arm5_pos", arms_commands.get("Arm5_pos"));

        //Base
        commands.put("base_wheel1", base_commands.get("base_wheel1"));
        commands.put("base_wheel2", base_commands.get("base_wheel2"));
        commands.put("base_wheel3", base_commands.get("base_wheel3"));
        commands.put("base_wheel4", base_commands.get("base_wheel4"));
        
        return commands;
    }

    @Override
    public void run() {

        logger.log(Level.INFO, "GripMPT starts...");
        
        //1. Start FSM(s) and the Suppress nodes
        // ::Hand::
        // 1) OpenFSM
        taskSpawner.addTask(openFSM);

        // 2) GrabFSM
        taskSpawner.addTask(grabFSM);

        // 3) A suppress Node (S_Deposit_Grab + OpenFSM --> Output)
        taskSpawner.addTask(S_Grab_Open);

        // 4) DepositFSM
        taskSpawner.addTask(depositFSM);

        // 5) A suppress Node (DepositFSM + Grab FSM --> Output)
        taskSpawner.addTask(S_Deposit_Grab);

        //::Arm::
        // 1) HomeFSM <-- This is an older version
        //taskSpawner.addTask(homeFSM);

        // 2) DescendFSM
        taskSpawner.addTask(descendFSM);

        // 3) EgyptFSM
        taskSpawner.addTask(egyptFSM);

        // 4) A suppress Node (Descend FSM + Egypt FSM --> arm Output)
        taskSpawner.addTask(S_Descend_Egypt);

        // 5) BounceFSM
        taskSpawner.addTask(bounceFSM);

        // 6)A suppress Node (Bounce FSM + Surface FSM(S) --> arm Output)
        taskSpawner.addTask(S_Bounce_Home);

        // 7) surfaceFSM
        taskSpawner.addTask(surfaceFSM);

        // 8) A suppress Node (Surface FSM + Descend FSM --> Egypt(S))
        taskSpawner.addTask(S_Surface_Descend);

        // 9) stopFSM
        taskSpawner.addTask(stopFSM);

        // 10) A suppress Node (Bound FSM + Stop FSM --> Egypt(S))
        taskSpawner.addTask(S_Bounce_Stop);

        // 11) HomeFSM
        taskSpawner.addTask(homeFSM);

        // 12) A suppress Node (Home FSM + Surface FSM --> bounce(S))
        taskSpawner.addTask(S_Home_Extend);

        // 13) overFSM
        taskSpawner.addTask(overFSM);

        // 14)A suppress Node (Over FSM + Stop FSM --> home(S))
        taskSpawner.addTask(S_Over_Stop);

        // 15) extendFSM
        taskSpawner.addTask(extendFSM);

        // 16) A suppress Node (Extend FSM + Surface(S) --> home(S))
        taskSpawner.addTask(S_Extend_Surface);

        
        

        //2. Link the wires between FSM(s), Suppress nodes and output commands

        /*
        // ::Hand::
        //1) openFSM ===> Suppress Node (GrabFSM + OpenFSM --> Output)
        wire1 w1 = new wire1(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w1);

        //2) Suppress Node (DepositFSM + Grab FSM --> Output) ===>
        //        Suppress Node (GrabFSM + OpenFSM --> Output)
        wire2 w2 = new wire2(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w2);

        //3) Suppress Node (GrabFSM + OpenFSM --> Output) ===> MPT output
        wire3 w3 = new wire3(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w3);

        //4) grabFSM ===> Suppress Node (DepositFSM + Grab FSM --> Output)
        wire5 w5 = new wire5(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w5);

        //5) depositFSM ===> Suppress Node (DepositFSM + Grab FSM --> Output)
        wire6 w6 = new wire6(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w6);

        //::Arm::
        //1) homeFSM ===> Arms MPT output
        // Comment wire4 for testing other operations
        //wire4 w4 = new wire4(WIRE_TICKS_PER_RUN);
        //taskSpawner.addTask(w4);

        //2) descendFSM ===> Arms MPT output
        // This is an older wire
        //wire7 w7 = new wire7(WIRE_TICKS_PER_RUN);
        //taskSpawner.addTask(w7);

        
        //3) egyptFSM ==> Suppress Node (Descend FSM + Egypt FSM --> arm Output)
        wire8 w8 = new wire8(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w8);

        //4) descendFSM ==> Suppress Node (Descend FSM + Egypt FSM)
        // This is an older wire
        //wire9 w9 = new wire9(WIRE_TICKS_PER_RUN);
        //taskSpawner.addTask(w9);

        //5) Suppress Node (Surface FSM + Descend FSM + Egypt FSM) ===>
        //   Suppress Node (Bounce FSM + Surface FSM(S))
        // This is an older wire
        //wire10 w10 = new wire10(WIRE_TICKS_PER_RUN);
        //taskSpawner.addTask(w10);

        //6) bounceFSM ==> Suppress Node (Bounce FSM + Stop FSM)
        wire11 w11 = new wire11(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w11);

        //7) Suppress Node (Bounce FSM + Surface FSM(S)) ==> Arm output
        wire12 w12 = new wire12(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w12);

        //)8) descendFSM ==> Suppress Node (Surface FSM + Descend FSM)
        wire13 w13 = new wire13(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w13);

        //9) surfaceFSM ==> Suppress Node (Surface FSM + Descend FSM)
        wire14 w14 = new wire14(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w14);

        //10) Suppress Node (Surface FSM + Descend FSM) ==>
        //    Suppress Node (Descend FSM + Egypt FSM)
        wire15 w15 = new wire15(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w15);

        //(11) wire16 :: Stop FSM ==> Suppress Node (Bounce FSM + Stop FSM)
        //This is an older one
        //wire16 w16 = new wire16(WIRE_TICKS_PER_RUN);
        //taskSpawner.addTask(w16);

        //(12) wire17 :: Suppress Node (Bounce FSM + Stop FSM) ==>
        //                 Suppress Node (Bounce FSM(S) + Surface FSM(S))
        wire17 w17 = new wire17(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w17);

        // TODO: Add comments to below lines
        // This is an older one
        //wire18 w18 = new wire18(WIRE_TICKS_PER_RUN);
        //taskSpawner.addTask(w18);

        wire19 w19 = new wire19(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w19);

        wire20 w20 = new wire20(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w20);

        wire21 w21 = new wire21(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w21);

        wire22 w22 = new wire22(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w22);

        wire23 w23 = new wire23(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w23);

        wire24 w24 = new wire24(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w24);

        wire25 w25 = new wire25(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w25);

        wire26 w26 = new wire26(WIRE_TICKS_PER_RUN);
        taskSpawner.addTask(w26);
         * 
         */

        //::Hand::
        //1)
        Wire_FSM_S_LOWER w1 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, openFSM, S_Grab_Open);
        taskSpawner.addTask(w1);

        //2)
        Wire_S_S_HIGHER w2 = new Wire_S_S_HIGHER(WIRE_TICKS_PER_RUN, S_Deposit_Grab, S_Grab_Open);
        taskSpawner.addTask(w2);

        //3)
        Wire_S_CMD w3 = new Wire_S_CMD(WIRE_TICKS_PER_RUN, S_Grab_Open, grippers_commands);
        taskSpawner.addTask(w3);

        //4)
        Wire_FSM_S_LOWER w5 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, grabFSM, S_Deposit_Grab);
        taskSpawner.addTask(w5);

        //5)
        Wire_FSM_S_HIGHER w6 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, depositFSM, S_Deposit_Grab);
        taskSpawner.addTask(w6);

        //:Arms:
        //6)
        Wire_FSM_S_LOWER w8 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, egyptFSM, S_Descend_Egypt);
        taskSpawner.addTask(w8);

        //7)
        Wire_FSM_S_HIGHER w11 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, bounceFSM, S_Bounce_Stop);
        taskSpawner.addTask(w11);

        //8)
        Wire_S_CMD w12 = new Wire_S_CMD(WIRE_TICKS_PER_RUN, S_Bounce_Home, arms_commands);
        taskSpawner.addTask(w12);

        //9)
        Wire_FSM_S_LOWER w13 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, descendFSM, S_Surface_Descend);
        taskSpawner.addTask(w13);

        //10)
        Wire_FSM_S_HIGHER w14 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, surfaceFSM, S_Surface_Descend);
        taskSpawner.addTask(w14);

        //11)
        Wire_S_S_HIGHER w15 = new Wire_S_S_HIGHER(WIRE_TICKS_PER_RUN, S_Surface_Descend, S_Descend_Egypt);
        taskSpawner.addTask(w15);

        //12)
        Wire_S_S_HIGHER w17 = new Wire_S_S_HIGHER(WIRE_TICKS_PER_RUN, S_Bounce_Stop, S_Bounce_Home);
        taskSpawner.addTask(w17);

        //13)
        Wire_FSM_S_HIGHER w19 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, homeFSM, S_Home_Extend);
        taskSpawner.addTask(w19);

        //14)
        Wire_S_S_LOWER w20 = new Wire_S_S_LOWER(WIRE_TICKS_PER_RUN, S_Home_Extend, S_Bounce_Home);
        taskSpawner.addTask(w20);

        //15)
        Wire_FSM_S_LOWER w21 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, stopFSM, S_Over_Stop);
        taskSpawner.addTask(w21);

        //16)
        Wire_FSM_S_HIGHER w22 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, overFSM, S_Over_Stop);
        taskSpawner.addTask(w22);

        //17)
        Wire_S_S_LOWER w23 = new Wire_S_S_LOWER(WIRE_TICKS_PER_RUN, S_Over_Stop, S_Bounce_Stop);
        taskSpawner.addTask(w23);

        //18)
        Wire_S_S_LOWER w24 = new Wire_S_S_LOWER(WIRE_TICKS_PER_RUN, S_Descend_Egypt, S_Extend_Surface);
        taskSpawner.addTask(w24);

        //19)
        Wire_FSM_S_HIGHER w25 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, extendFSM, S_Extend_Surface);
        taskSpawner.addTask(w25);

        //20)
        Wire_S_S_LOWER w26 = new Wire_S_S_LOWER(WIRE_TICKS_PER_RUN, S_Extend_Surface, S_Home_Extend);
        taskSpawner.addTask(w26);



        
    }


     /*
     * Wire1 :: openFSM ===> Suppress Node (GrabFSM + OpenFSM --> Output)
     */
    private class wire1 extends FrameworkTaskImpl {

        public wire1(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Grab_Open.inputLowerLayerOutput(openFSM.outputCommands());
        }

    }

     /*
     * Suppress Node (DepositFSM + Grab FSM --> Output) ===>
     *        Suppress Node (GrabFSM + OpenFSM --> Output)
     */
    private class wire2 extends FrameworkTaskImpl {

        public wire2(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Grab_Open.inputHigherLayerOutput(S_Deposit_Grab.output());
        }

    }

     /*
     * Wire3 :: Suppress Node (GrabFSM + OpenFSM --> Output) ===> Hand MPT output
     */
    private class wire3 extends FrameworkTaskImpl {

        public wire3(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            grippers_commands =  S_Grab_Open.output();
        }

    }

     /*
     * Wire4 :: homeFSM ===> Arm MPT output
     */
    private class wire4 extends FrameworkTaskImpl {

        public wire4(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            arms_commands =  (Map<String, Object>)homeFSM.outputCommands();
        }

    }

     /*
     * Wire5 :: grabFSM ===> Suppress Node (DepositFSM + Grab FSM --> Output)
     */
    private class wire5 extends FrameworkTaskImpl {

        public wire5(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Deposit_Grab.inputLowerLayerOutput(grabFSM.outputCommands());
        }

    }

     /*
     * Wire6 :: depositFSM ===> Suppress Node (DepositFSM + Grab FSM --> Output)
     */
    private class wire6 extends FrameworkTaskImpl {

        public wire6(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Deposit_Grab.inputHigherLayerOutput(depositFSM.outputCommands());
        }

    }

     /*
     * Wire7 :: descendFSM ===> Arms MPT output
     */
    private class wire7 extends FrameworkTaskImpl {

        public wire7(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            arms_commands =  (Map<String, Object>)descendFSM.outputCommands();
        }

    }

     /*
     * Wire8 :: egyptFSM ==> Suppress Node (Descend FSM + Egypt FSM --> arm Output)
     */
    private class wire8 extends FrameworkTaskImpl {

        public wire8(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Descend_Egypt.inputLowerLayerOutput(egyptFSM.outputCommands());
        }

    }

     /*
     * Wire9 :: descendFSM ==> Suppress Node (Descend FSM + Egypt FSM --> arm Output)
     */
    private class wire9 extends FrameworkTaskImpl {

        public wire9(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Descend_Egypt.inputHigherLayerOutput(descendFSM.outputCommands());
        }

    }

     /*
     * Wire10 :: Suppress Node (Surface FSM + Descend FSM + Egypt FSM) ===>
      * Suppress Node (Bounce FSM + Surface FSM(S))
     */
    private class wire10 extends FrameworkTaskImpl {

        public wire10(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Bounce_Home.inputLowerLayerOutput(S_Descend_Egypt.output());
        }

    }

    /*
     * Wire11 :: bounceFSM ==> Suppress Node (Bounce FSM + Stop FSM)
     */
    private class wire11 extends FrameworkTaskImpl {

        public wire11(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Bounce_Stop.inputHigherLayerOutput(bounceFSM.outputCommands());
        }

    }

    /*
     * Wire12 :: Suppress Node (Bounce FSM + Surface FSM(S)) ==> Arm output
     */
    private class wire12 extends FrameworkTaskImpl {

        public wire12(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            arms_commands = S_Bounce_Home.output();
        }

    }

    /*
     * wire13 :: descendFSM ==> Suppress Node (Surface FSM + Descend FSM)
     */
    private class wire13 extends FrameworkTaskImpl {

        public wire13(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Surface_Descend.inputLowerLayerOutput(descendFSM.outputCommands());
        }

    }

    /*
     * wire14 :: surfaceFSM ==> Suppress Node (Surface FSM + Descend FSM)
     */
    private class wire14 extends FrameworkTaskImpl {

        public wire14(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Surface_Descend.inputHigherLayerOutput(surfaceFSM.outputCommands());
        }

    }

    /*
     * wire15 :: Suppress Node (Surface FSM + Descend FSM) ==>
     * Suppress Node (Descend FSM + Egypt FSM)
     */
    private class wire15 extends FrameworkTaskImpl {

        public wire15(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Descend_Egypt.inputHigherLayerOutput(S_Surface_Descend.output());
        }

    }

    /*
     * wire16 :: Stop FSM ==> Suppress Node (Bounce FSM + Stop FSM)
     */
    private class wire16 extends FrameworkTaskImpl {

        public wire16(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Bounce_Stop.inputLowerLayerOutput(stopFSM.outputCommands());
        }

    }

    /*
     * wire17 :: Suppress Node (Bounce FSM + Stop FSM) ==>
     * Suppress Node (Bounce FSM(S) + Surface FSM(S))
     */
    private class wire17 extends FrameworkTaskImpl {

        public wire17(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Bounce_Home.inputHigherLayerOutput(S_Bounce_Stop.output());
        }

    }

    /*
     * wire18 :: Suppress Node (Descend FSM + Egypt FSM) ==>
     * Suppress Node (Home FSM + Surface FSM(S))
     */
    private class wire18 extends FrameworkTaskImpl {

        public wire18(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Home_Extend.inputLowerLayerOutput(S_Descend_Egypt.output());
        }
    }

    /*
     * wire19 :: Suppress Node (Descend FSM + Egypt FSM) ==>
     * Suppress Node (Home FSM + Surface FSM(S))
     */
    private class wire19 extends FrameworkTaskImpl {

        public wire19(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Home_Extend.inputHigherLayerOutput(homeFSM.outputCommands());
        }
    }

    /*
     * wire20 :: Suppress Node (Descend FSM + Egypt FSM) ==>
     * Suppress Node (Home FSM + Surface FSM(S))
     */
    private class wire20 extends FrameworkTaskImpl {

        public wire20(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Bounce_Home.inputLowerLayerOutput(S_Home_Extend.output());
        }
    }

    /*
     * wire21 :: Stop FSM ==> Suppress Node (Over FSM + Stop FSM)
     */
    private class wire21 extends FrameworkTaskImpl {

        public wire21(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Over_Stop.inputLowerLayerOutput(stopFSM.outputCommands());
        }
    }

    /*
     * wire22 :: Over FSM ==> Suppress Node (Over FSM + Stop FSM)
     */
    private class wire22 extends FrameworkTaskImpl {

        public wire22(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Over_Stop.inputHigherLayerOutput(overFSM.outputCommands());
        }
    }

    /*
     * wire23 :: Suppress Node (Over FSM + Stop FSM) ==>
     * Suppress Node (Bounce FSM + Stop FSM)
     */
    private class wire23 extends FrameworkTaskImpl {

        public wire23(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Bounce_Stop.inputLowerLayerOutput(S_Over_Stop.output());
        }
    }

    /*
     * wire24 :: Suppress Node (Descend FSM + Eygpt FSM) ==>
     * Suppress Node (Extend FSM + Surface FSM)
     */
    private class wire24 extends FrameworkTaskImpl {

        public wire24(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Extend_Surface.inputLowerLayerOutput(S_Descend_Egypt.output());
        }
    }

    /*
     * wire25 :: Extend FSM ==> Suppress Node (Extend FSM + Surface FSM)
     */
    private class wire25 extends FrameworkTaskImpl {

        public wire25(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Extend_Surface.inputHigherLayerOutput(extendFSM.outputCommands());
        }
    }

    /*
     * wire26 :: Suppress Node (Extend FSM + Surface FSM) ==>
     * Suppress Node (Home FSM + Extend(S))
     */
    private class wire26 extends FrameworkTaskImpl {

        public wire26(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            S_Home_Extend.inputLowerLayerOutput(S_Extend_Surface.output());
        }
    }

}
