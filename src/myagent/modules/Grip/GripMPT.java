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


}
