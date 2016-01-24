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
import java.awt.Color;

public class ColorFeatureDetector extends BasicDetectionAlgorithm{

	/*
	 * intensity and eyes type
	 */
    private String soughtColor = "red";
    private Map<String, Object> smParams = new HashMap<String, Object>();

    @Override
    public void init() {
       super.init();
       smParams.put("mode","color");
       soughtColor = (String) getParam("color", "red");
    }

    @Override
    public double detect() {
       Color color = (Color) sensoryMemory.getSensoryContent("visual",smParams);

        if ("red".equalsIgnoreCase(soughtColor))
        {
            if (color == Color.RED )
            {
                return 1.0;
            }else{
                return 0.0;
            }
        }

        if ("white".equalsIgnoreCase(soughtColor))
        {
            if (color == Color.WHITE )
            {
                return 1.0;
            }else{
                return 0.0;
            }
        }

       return 0.0;
    }
}
