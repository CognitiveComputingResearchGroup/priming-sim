/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/

package myagent.featuredetectors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.framework.shared.Link;
import edu.memphis.ccrg.lida.pam.PamLink;
import edu.memphis.ccrg.lida.pam.PamLinkable;
import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;
import edu.memphis.ccrg.lida.pam.tasks.DetectionAlgorithm;
import edu.memphis.ccrg.lida.pam.tasks.MultipleDetectionAlgorithm;

import java.awt.Color;

public class ColorFeatureDetector extends MultipleDetectionAlgorithm implements DetectionAlgorithm{

	/*
	 * intensity and eyes type
	 */
    private int position= 0;
    private Map<String, Object> smParams = new HashMap<String, Object>();

    @Override
    public void init(){
    	super.init();
		position =(int) getParam("position", 0);
	}

	@Override
	public void detectLinkables() {
		
			String location="";
			int red_position,green_position;
			try{
				red_position=(int)sensoryMemory.getSensoryContent("red_position", smParams);
			}catch(NullPointerException npe){
				red_position=0;
			}
			try{
				green_position=(int)sensoryMemory.getSensoryContent("green_position", smParams);
			}catch(NullPointerException npe){
				green_position=0;
			}
			if(red_position==position){
				PamLinkable pl=pamNodeMap.get((position==1?"topRight":"bottomLeft")+"_red");
				pam.receiveExcitation(pl, 0.02);
			}
			else if(green_position==position){
				PamLinkable pl=pamNodeMap.get((position==1?"topRight":"bottomLeft")+"_green");
				pam.receiveExcitation(pl, 0.02);
			}
				// TODO Auto-generated method stub
	}

    
}
