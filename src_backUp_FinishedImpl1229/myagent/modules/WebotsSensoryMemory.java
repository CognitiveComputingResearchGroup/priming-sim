/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package myagent.modules;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.sensorymemory.SensoryMemoryImpl;
import edu.memphis.ccrg.lida.sensorymemory.SensoryMemoryListener;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebotsSensoryMemory extends SensoryMemoryImpl {

    /*
     * a subset of light content
     */
    private static final Logger logger = Logger.getLogger(WebotsSensoryMemory.class.getCanonicalName());
    private Map<String, Object> mapRange = new HashMap<String, Object>();
    private Map<String, Object> sensorParam = new HashMap<String, Object>();

    @Override
    public void init() {
        //TODO: Initialize sensorParam
        mapRange.put("beam_broken", false);
        mapRange.put("beam_distance", 0.0);
        mapRange.put("grabbing", false);
        mapRange.put("wristForce_active", false);
        mapRange.put("toucher1_active", false);
        mapRange.put("toucher2_active", false);
        mapRange.put("arm1_pos", 0.0);
        mapRange.put("arm2_pos", 0.0);
        mapRange.put("arm3_pos", 0.0);
        mapRange.put("arm4_pos", 0.0);
        mapRange.put("arm5_pos", 0.0);
        mapRange.put("IR_Crossed", false);
    }

    @Override
    public void runSensors() {
        mapRange = (HashMap) environment.getState(sensorParam);

        for (SensoryMemoryListener listener : sensoryMemoryListeners) {
            listener.receiveSensoryMemoryContent(mapRange);
        }
    }

    @Override
    public Object getSensoryContent(String string, Map<String, Object> params) {

        //TODO: Support sensed data to Ventral Stream (Begin from the Feature Detector)
        // The codes below are faked and just for drive this framework to work (to do action selection)
        Map<String, Object> currentMap = new HashMap<String, Object>();
        String mode = (String) params.get("mode");
        if ("intensity".equalsIgnoreCase(mode)) {
            double leftI, rightI;

            leftI = 200.0;
            rightI = 200.0;
            currentMap.put("leftEye", leftI);
            currentMap.put("rightEye", rightI);

            return currentMap;

        } else if ("color".equalsIgnoreCase(mode)) {
            Color color = Color.WHITE;

            return color;
        }
        return null;
    }

    @Override
    public void decayModule(long l) {
        //NA
    }

    @Override
    public Object getModuleContent(Object... os) {
        return null;
    }
}
