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

import edu.memphis.ccrg.lida.pam.PamLinkable;
import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;
import edu.memphis.ccrg.lida.pam.tasks.DetectionAlgorithm;
import edu.memphis.ccrg.lida.pam.tasks.MultipleDetectionAlgorithm;

import java.awt.Color;

public class LocationFeatureDetector extends MultipleDetectionAlgorithm implements DetectionAlgorithm{

	/*
	 * intensity and eyes type
	 */
    private String soughtColor = "";
    private Map<String, Object> smParams = new HashMap<String, Object>();

    @Override
    public void init(){
    	super.init();
		String defaultValue="white";
		soughtColor =(String) getParam("color", defaultValue);
	}

	@Override
	public void detectLinkables() {
		
			String location="";
			if((boolean)sensoryMemory.getSensoryContent(soughtColor, smParams)){
				pam.receiveExcitation(pamNodeMap.get(soughtColor), 1.0);
				location=(int)sensoryMemory.getSensoryContent(soughtColor+"_location", smParams)==1?"topRight":"bottomLeft";
				
				pam.receiveExcitation(pamNodeMap.get(location), 1.0);
				pam.receiveExcitation(pamNodeMap.get(soughtColor+":"+location), 1.0);
			}
				// TODO Auto-generated method stub
	}

    
}
