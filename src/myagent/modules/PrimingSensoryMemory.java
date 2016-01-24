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

public class PrimingSensoryMemory extends SensoryMemoryImpl {

    /*
     * a subset of light content
     */
    private static final Logger logger = Logger.getLogger(PrimingSensoryMemory.class.getCanonicalName());
    private Map<String, Object> sensedData = new HashMap<String, Object>();
    private Map<String, Object> sensorParam = new HashMap<String, Object>();

    @Override
    public void init() {

    }

    @Override
    public void runSensors() {
    	sensedData = (HashMap) environment.getState(sensorParam);

        for (SensoryMemoryListener listener : sensoryMemoryListeners) {
            listener.receiveSensoryMemoryContent(sensedData);
        }
    }

    @Override
    public Object getSensoryContent(String string, Map<String, Object> params) {
    	return sensedData.get(string);
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
