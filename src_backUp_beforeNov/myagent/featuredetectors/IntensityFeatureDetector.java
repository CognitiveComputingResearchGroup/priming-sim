/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/

package myagent.featuredetectors;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;

public class IntensityFeatureDetector extends BasicDetectionAlgorithm{

    //Boundary threshold used for Fuzzy-logic
    private static final double INTENSITY_BOUNDARY = 300.0;
    private double boundary;
	/*
	 * intensity and eyes type
	 */
    private String soughtIntensity = "dark";
    private String soughtEye = "rightEye";
    private Map<String, Object> smParams = new HashMap<String, Object>();

    @Override
    public void init() {
       super.init();
       smParams.put("mode","intensity");
       soughtIntensity = (String) getParam("intensity", "dark");
       soughtEye = (String) getParam("eye", "rightEye");
       boundary = (Double) getParam("intensityBoundary", INTENSITY_BOUNDARY);
    }

    @Override
    public double detect() {
        Map intensity = (HashMap) sensoryMemory.getSensoryContent("visual",smParams);

        if ("dark".equalsIgnoreCase(soughtIntensity))
        {
            if ((Double)intensity.get(soughtEye) <= boundary )
            {
                return 1.0;
            }else{
                return 0.0;
            }
        }

        if ("bright".equalsIgnoreCase(soughtIntensity))
        {
            if ((Double)intensity.get(soughtEye) > boundary )
            {
                return 1.0;
            }else{
                return 0.0;
            }
        }

       return 0.0;
    }
}
