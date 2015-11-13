/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.SMS.MPT.Grip;

import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskSpawner;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import myagent.SMS.MPT.FSM;
import myagent.SMS.MPT.SubsumptionMPTImpl;

/**
 *
 * @author Daqi
 */
public class GripMPT extends SubsumptionMPTImpl{
    private static final Logger logger = Logger.getLogger(GripMPT.class.getCanonicalName());

    private final int FSM_TICKS_PER_RUN = 1;
    private final int SUPPRESS_TICKS_PER_RUN = 1;
    private final int WIRE_TICKS_PER_RUN = 1;
    
    private FSM openFSM, grabFSM, depositFSM;

    private ArmsFSM descendFSM, egyptFSM, bounceFSM, surfaceFSM, stopFSM, hoistFSM,
            overFSM, extendFSM, homeFSM, diagonalFSM, uncrashFSM, engardeFSM;

    private suppress S_Grab_Open, S_Deposit_Grab, S_Descend_Egypt, S_Bounce_Hoist,
            S_Surface_Descend, S_Bounce_Stop, S_Hoist_Extend, S_Over_Stop, S_Extend_Surface,
            S_Hoist_Home, S_Surface_Diagonal, S_Bounce_Uncrash, S_Home_Engarde;

    private boolean beam, grabbing, wrist_force_active, touching1, touching2, IR_crossed;

    private double arm1_pos, arm2_pos, arm3_pos, arm4_pos, beam_distance;

    private Map<String, Object> dorsal_visual_data = new HashMap<String, Object>();

    private Map<String, Object> grippers_commands = new HashMap<String, Object>();

    private Map<String, Object> arms_commands = new HashMap<String, Object>();

    private Map<String, Object> base_commands = new HashMap<String, Object>();

    private static final double GRIPPERS_GAP = 0.0156;
    
    private static final double MGA_OVER_RATE = 1.2;

    private static final double MGA_CLEAREST_RATE = 1.1;

    private double MGA_RATE_DIS;

    private static final double CONTEXT_DEVIATION_RATE = 0.1; //Percentage, e.g., value 0.1 means 10%


    @Override
    public void init(){
       //Input
        beam = false;
        beam_distance =0.0;
        grabbing = false;
        wrist_force_active = false;
        touching1 = false;
        touching2 = false;
        arm1_pos = 0.0;
        arm2_pos = 0.0;
        arm3_pos = 0.0;
        arm4_pos = 0.0;
        IR_crossed = false;

        dorsal_visual_data.put("targetObjectWidth", 0.0);


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

        hoistFSM = new HoistFSM(FSM_TICKS_PER_RUN);
        hoistFSM.init();

        overFSM = new OverFSM(FSM_TICKS_PER_RUN);
        overFSM.init();

        extendFSM = new ExtendFSM(FSM_TICKS_PER_RUN);
        extendFSM.init();

        homeFSM = new HomeFSM(FSM_TICKS_PER_RUN);
        homeFSM.init();

        diagonalFSM = new DiagonalFSM(FSM_TICKS_PER_RUN);
        diagonalFSM.init();

        uncrashFSM = new UncrashFSM(FSM_TICKS_PER_RUN);
        uncrashFSM.init();

        engardeFSM = new EngardeFSM(FSM_TICKS_PER_RUN);
        engardeFSM.init();

        // 2) Suppress Nodes
        S_Descend_Egypt = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Bounce_Hoist = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Surface_Descend = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Bounce_Stop = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Hoist_Extend = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Over_Stop = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Extend_Surface = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Hoist_Home = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Surface_Diagonal = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Bounce_Uncrash = new suppress(SUPPRESS_TICKS_PER_RUN);

        S_Home_Engarde = new suppress(SUPPRESS_TICKS_PER_RUN);

        MGA_RATE_DIS = 0.0; // Count how close the hand to the target object: bigger means closer

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
        beam_distance = (Double) data.get("beam_distance");
        grabbing = (Boolean) data.get("grabbing");
        wrist_force_active = (Boolean) data.get("wristForce_active");
        touching1 = (Boolean) data.get("toucher1_active");
        touching2 = (Boolean) data.get("toucher2_active");
        arm1_pos = (Double) data.get("arm1_pos");
        arm2_pos = (Double) data.get("arm2_pos");
        arm3_pos = (Double) data.get("arm3_pos");
        arm4_pos = (Double) data.get("arm4_pos");
        IR_crossed = (Boolean) data.get("IR_Crossed");

        dorsal_visual_data.put("targetObjectWidth", (Double) data.get("targetObjectWidth"));

        //1. Hand
        //1) No input to Open FSM

        //2) Input beam-broken Info. to Grab FSM
        grabFSM.receiveData(beam);

        //3) Input wrist force Info. and the side of hand to Deposit FSM
        depositFSM.receiveData(wrist_force_active, touching1, touching2);

        // 2. Arm
        //1) Input grabbing Info. to Hoist FSM
        // This is an old homeFSM, was changed to Hoist cause of FSM renaming.
        //hoistFSM.receiveData(grabbing);

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
        // Note that there are two channels sending Info. to surface FSM
        surfaceFSM.receiveData(wrist_force_active, touching1, touching2);

        //6-1) Input arms positions Info. to Stop FSM
        stopFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //6-2) Input the beam distance to Stop FSM
        // Note that there are two channels sending Info. to Stop FSM
        stopFSM.receiveData(beam_distance);

        //7) Input arms positions Info. to Hoist FSM
        hoistFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //8-1) Input arms positions Info. to Over FSM
        overFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //8-2) Input the IR Crossed Info. to Over FSM
        // Note that there are two channels sending Info. to over FSM
        overFSM.receiveData(IR_crossed);

        //9-1) Input arms positions Info. to Extend FSM
        extendFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //9-2) Input the IR Crossed Info. to Extend FSM
        // Note that there are two channels sending Info. to extend FSM
        extendFSM.receiveData(IR_crossed);

        //10-1) Input arms positions Info. to Home FSM
        homeFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //10-2) Input the home arm (arm1) position
        homeFSM.recieveHomeArm(arm1_pos);

        //11-1) Input arms positions Info. to diagonal FSM
        diagonalFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //11-2)
        diagonalFSM.receiveData(grabbing);

        //12-1)Input arms positions Info. to uncrash FSM
        uncrashFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //12-2)
        uncrashFSM.receiveData(wrist_force_active, touching1, touching2);

        //13-1)Input arms positions Info. to engarde FSM
        engardeFSM.recieveArmsPositions(arm2_pos, arm3_pos, arm4_pos);

        //13-2)
        engardeFSM.recieveHomeArm(arm1_pos);

        //13-3)
        engardeFSM.receiveData(beam);
        

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
    public void specify() {
        //The specification process replies on both sensed data through dorsal stream
        // and the context of a selected behavior
        // Typically, the context is a backup to the sensed data
        // whereas some command values replies on the context only
        // Check the paper "Two visual systems re-viewed (AD Milner, MA Goodale, 2008)"

        Double cmdValue = 0.0;

        // 1. openFSM <-- MGA
        // Based on dorsal stream
        //TODO: Replace this temporal while-loop Impl by multiple tasks

        //while (cmdValue <= 0.0){
        cmdValue = this.dorsal_MotorValueOf("MGA");
        //}
        
        System.out.println("the cmdValue of dorsal_MotorValueOf() in specify is:" + cmdValue);

        if (cmdValue <= 0.0){
            // Based on the selected behavior
            cmdValue = this.behavior_MotorValueOf("MGA");
        }

        System.out.println("the cmdValue of behavior_MotorValueOf() in specify is:" + cmdValue);
        openFSM.specify(cmdValue);
        cmdValue = 0.0;//clear
    }

    @Override
    public void update() {
        //the update method, part of online control process, replies on the
        // sensed data through dorsal stream only

        //System.out.println("The upadte() is called!!");

        Double cmdValue = 0.0;

        // 1. openFSM <-- MGA
        // Based on dorsal stream
        cmdValue = this.dorsal_MotorValueOf("MGA");

        //System.out.println("The updating command value is: " + cmdValue);

        openFSM.update(cmdValue);
        cmdValue = 0.0;//clear
        
    }

    @Override
    public Double dorsal_MotorValueOf (String cmdName){
        //Since this code is developed on Java 1.6, not yet supporting switch for type String,
        // if-else strategy was used
        
        Double cmdValue = 0.0, objWidth = 0.0;

        Double mga_rate = 0.0;

        double distance = 1200.0;
       
        if (cmdName.equals("MGA")){

            //objWidth = (Double) dorsal_visual_data.get("targetObjectWidth");
            //TODO: Replace this temporal fixed value by trigger the selected behavior
            //by the agent is in the Init situation
            objWidth = 0.025;

            mga_rate = MGA_OVER_RATE - (MGA_OVER_RATE - MGA_CLEAREST_RATE)*(MGA_RATE_DIS/distance);

            // Closer to the target object when much time ran
            MGA_RATE_DIS = MGA_RATE_DIS + 1.0;

            if (MGA_RATE_DIS > distance){
                MGA_RATE_DIS = distance;
            }

            if (objWidth > 0.0){
                cmdValue = (objWidth * mga_rate - GRIPPERS_GAP) *0.5;
            }else{
                //The sensed data through dorsal stream delayed
                System.out.println("The sensed data through dorsal stream delayed.");
                cmdValue = -1.0;
            }
        }else{
            //The command name is not registered (learned) in the MPT
            System.out.println("The " + cmdName + " is not learnt yet.");
            cmdValue = -1.0;
        }

        return cmdValue;

    }

    @Override
    public Double behavior_MotorValueOf (String cmdName){
        //Since this code is developed on Java 1.6, not yet supporting switch for type String,
        // if-else strategy was used

        Double cmdValue = 0.0, objWidthInContext = 0.0;

        if (cmdName.equals("MGA")){

            //TODO: Replace this fixed code by take the context component from a
            // selected behavior's context

            //0.025 (1 + 0.0 ~ 0.1)
            objWidthInContext = 0.025 * (1 + Math.random() * CONTEXT_DEVIATION_RATE);

            if (objWidthInContext > 0.0){
                cmdValue = (objWidthInContext * MGA_OVER_RATE - GRIPPERS_GAP) *0.5;
            }else{
                //The sensed data through dorsal stream delayed
                System.out.println("The context of a selected behavior is delayed.");
                cmdValue = -1.0;
            }
        }else{
            //The command name is not registered (learned) in the MPT
            System.out.println("The " + cmdName + " is not learnt yet.");
            cmdValue = -1.0;
        }

        return cmdValue;
    }
    
    @Override
    public void onlineControl() {

        logger.log(Level.INFO, "GripMP starts...");
        
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
        taskSpawner.addTask(S_Bounce_Hoist);

        // 7) surfaceFSM
        taskSpawner.addTask(surfaceFSM);

        // 8) A suppress Node (Surface FSM + Descend FSM --> Egypt(S))
        taskSpawner.addTask(S_Surface_Descend);

        // 9) stopFSM
        taskSpawner.addTask(stopFSM);

        // 10) A suppress Node (Bound FSM + Stop FSM --> Egypt(S))
        taskSpawner.addTask(S_Bounce_Stop);

        // 11) HoistFSM
        taskSpawner.addTask(hoistFSM);

        // 12) A suppress Node (Hoist FSM + Surface FSM --> bounce(S))
        taskSpawner.addTask(S_Hoist_Extend);

        // 13) overFSM
        taskSpawner.addTask(overFSM);

        // 14)A suppress Node (Over FSM + Stop FSM --> hoist(S))
        taskSpawner.addTask(S_Over_Stop);

        // 15) extendFSM
        taskSpawner.addTask(extendFSM);

        // 16) A suppress Node (Extend FSM + Surface(S) --> hoist(S))
        taskSpawner.addTask(S_Extend_Surface);

        // 17) homeFSM
        taskSpawner.addTask(homeFSM);

        //18) A suppress Node (Hoist FSM + Home FSM --> surface(S))
        taskSpawner.addTask(S_Hoist_Home);

        //19)diagonalFSM
        taskSpawner.addTask(diagonalFSM);

        //20) Asuppress Node(Surface FSM + Diagonal FSM --> descend(S))
        taskSpawner.addTask(S_Surface_Diagonal);

        //21)uncrashFSM
        taskSpawner.addTask(uncrashFSM);

        //22)
        taskSpawner.addTask(S_Bounce_Uncrash);

        //23)
        taskSpawner.addTask(engardeFSM);

        //24)
        taskSpawner.addTask(S_Home_Engarde);


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

        //7) This is an older one
        //Wire_FSM_S_HIGHER w11 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, bounceFSM, S_Bounce_Stop);
        //taskSpawner.addTask(w11);

        //8)
        Wire_S_CMD w12 = new Wire_S_CMD(WIRE_TICKS_PER_RUN, S_Bounce_Hoist, arms_commands);
        taskSpawner.addTask(w12);

        //9)
        Wire_FSM_S_LOWER w13 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, descendFSM, S_Surface_Descend);
        taskSpawner.addTask(w13);

        //10) This is an older one
        //Wire_FSM_S_HIGHER w14 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, surfaceFSM, S_Surface_Descend);
        //taskSpawner.addTask(w14);

        //11)
        Wire_S_S_HIGHER w15 = new Wire_S_S_HIGHER(WIRE_TICKS_PER_RUN, S_Surface_Descend, S_Descend_Egypt);
        taskSpawner.addTask(w15);

        //12)
        Wire_S_S_HIGHER w17 = new Wire_S_S_HIGHER(WIRE_TICKS_PER_RUN, S_Bounce_Stop, S_Bounce_Hoist);
        taskSpawner.addTask(w17);

        //13) This is an older one
        //Wire_FSM_S_HIGHER w19 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, hoistFSM, S_Hoist_Extend);
        //taskSpawner.addTask(w19);

        //14)
        Wire_S_S_LOWER w20 = new Wire_S_S_LOWER(WIRE_TICKS_PER_RUN, S_Hoist_Extend, S_Bounce_Hoist);
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
        Wire_S_S_LOWER w26 = new Wire_S_S_LOWER(WIRE_TICKS_PER_RUN, S_Extend_Surface, S_Hoist_Extend);
        taskSpawner.addTask(w26);

        //21) This is an older one
        //Wire_FSM_S_LOWER w27 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, homeFSM, S_Hoist_Home);
        //taskSpawner.addTask(w27);

        //22)
        Wire_FSM_S_HIGHER w28 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, hoistFSM, S_Hoist_Home);
        taskSpawner.addTask(w28);

        //23)
        Wire_S_S_HIGHER w29 = new Wire_S_S_HIGHER(WIRE_TICKS_PER_RUN, S_Hoist_Home, S_Hoist_Extend);
        taskSpawner.addTask(w29);

        //24)
        Wire_FSM_S_LOWER w30 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, diagonalFSM, S_Surface_Diagonal);
        taskSpawner.addTask(w30);

        //25)
        Wire_FSM_S_HIGHER w31 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, surfaceFSM, S_Surface_Diagonal);
        taskSpawner.addTask(w31);

        //26)
        Wire_S_S_HIGHER w32 = new Wire_S_S_HIGHER(WIRE_TICKS_PER_RUN, S_Surface_Diagonal, S_Surface_Descend);
        taskSpawner.addTask(w32);

        //27)
        Wire_FSM_S_LOWER w33 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, uncrashFSM, S_Bounce_Uncrash);
        taskSpawner.addTask(w33);

        //28)
        Wire_FSM_S_HIGHER w34 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, bounceFSM, S_Bounce_Uncrash);
        taskSpawner.addTask(w34);

        //29)
        Wire_S_S_HIGHER w35 = new Wire_S_S_HIGHER(WIRE_TICKS_PER_RUN, S_Bounce_Uncrash, S_Bounce_Stop);
        taskSpawner.addTask(w35);

        //30)
        Wire_FSM_S_LOWER w36 = new Wire_FSM_S_LOWER(WIRE_TICKS_PER_RUN, engardeFSM, S_Home_Engarde);
        taskSpawner.addTask(w36);

        //31)
        Wire_FSM_S_HIGHER w37 = new Wire_FSM_S_HIGHER(WIRE_TICKS_PER_RUN, homeFSM, S_Home_Engarde);
        taskSpawner.addTask(w37);

        //32)
        Wire_S_S_LOWER w38 = new Wire_S_S_LOWER(WIRE_TICKS_PER_RUN, S_Home_Engarde, S_Hoist_Home);
        taskSpawner.addTask(w38);
        
    }


}
