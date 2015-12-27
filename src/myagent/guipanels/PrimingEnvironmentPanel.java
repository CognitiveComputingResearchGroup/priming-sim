/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package myagent.guipanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import myagent.modules.PrimingEnvironment;
import edu.memphis.ccrg.lida.framework.ModuleName;
import edu.memphis.ccrg.lida.framework.gui.panels.GuiPanel;
import edu.memphis.ccrg.lida.framework.gui.panels.GuiPanelImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

import java.util.Map;

/**
 * A {@link GuiPanel} for the {@link PrimingEnvironment}
 * @author Pulin Agrawal 
 */
public class PrimingEnvironmentPanel extends GuiPanelImpl {

	private static final Logger logger = Logger.getLogger(PrimingEnvironmentPanel.class.getCanonicalName());
	private PrimingEnvironment environment;
	private BufferedImage img = new BufferedImage(PrimingEnvironment.ENVIRONMENT_WIDTH,
			PrimingEnvironment.ENVIRONMENT_HEIGHT, BufferedImage.TYPE_INT_RGB);

	/** Creates new form ButtonEnvironmentPanel */
	public PrimingEnvironmentPanel() {
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jToolBar1 = new javax.swing.JToolBar();
		refreshButton = new javax.swing.JButton();
		imgPanel = new ImagePanel();

		jToolBar1.setRollover(true);

		refreshButton.setText("Refresh");
		refreshButton.setFocusable(false);
		refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		refreshButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				refreshButtonActionPerformed(evt);
			}
		});
		jToolBar1.add(refreshButton);

		javax.swing.GroupLayout imgPanelLayout = new javax.swing.GroupLayout(imgPanel);
		imgPanel.setLayout(imgPanelLayout);
		imgPanelLayout.setHorizontalGroup(
				imgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 380, Short.MAX_VALUE)
				);
		imgPanelLayout.setVerticalGroup(
				imgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 236, Short.MAX_VALUE)
				);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(imgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addContainerGap()))
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(294, Short.MAX_VALUE))
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addGap(31, 31, 31)
										.addComponent(imgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addGap(52, 52, 52)))
				);
	}// </editor-fold>//GEN-END:initComponents

	private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
		refresh();
	}//GEN-LAST:event_refreshButtonActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel imgPanel;
	private javax.swing.JToolBar jToolBar1;
	private javax.swing.JButton refreshButton;
	// End of variables declaration//GEN-END:variables

	@Override
	public void initPanel(String[] param) {
		environment = (PrimingEnvironment) agent.getSubmodule(ModuleName.Environment);
		if (environment != null) {
			refresh();
		} else {
			logger.log(Level.WARNING,
					"Unable to parse module {1} Panel not initialized.",
					new Object[]{0L, param[0]});
		}
	}

	@Override
	public void refresh() {
		//img = (BufferedImage) environment.getModuleContent();
		this.imgPanel.repaint();


		//TODO: Show sensed data and commands content in environment panel
		//Map<String,Object> wheelSpeeds = environment.getWheelsSpeed();

		//double leftWheel = (Double)wheelSpeeds.get("leftWheel");
		//double rightWheel = (Double)wheelSpeeds.get("rightWheel");



	}

	private static final int DOT_SIZE=2;
	private static final int DISC_SIZE=8;
	private static final int ANNULI_SIZE=10;
	private class ImagePanel extends JPanel {

		private Dimension dimension = new Dimension();
		double scalingFactor = 1.5;

		public ImagePanel() {
			dimension.setSize(img.getWidth(), img.getHeight());
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.clearRect(0,0, getWidth(), getHeight());
			Image scaledImage = img.getScaledInstance((int)(img.getWidth()*scalingFactor), (int)(img.getHeight()*scalingFactor), Image.SCALE_SMOOTH);
			int xCentered = (getWidth() - scaledImage.getWidth(this)) / 2;
			int yCentered = (getHeight()- scaledImage.getHeight(this)) / 2;

			if(TaskManager.getCurrentTick()<700){
				//blank data
				img.getGraphics().drawOval(img.getWidth()/2, img.getHeight()/2, PrimingEnvironmentPanel.DOT_SIZE, PrimingEnvironmentPanel.DOT_SIZE);
			}
			else if(TaskManager.getCurrentTick()>700 && TaskManager.getCurrentTick()<(10+700)){
				if(environment.getParam("consistent", true)){
					img.getGraphics().fillOval(img.getWidth()/2, img.getHeight()/2, PrimingEnvironmentPanel.DOT_SIZE, PrimingEnvironmentPanel.DOT_SIZE);
					img.getGraphics().setColor(Color.GREEN);
					img.getGraphics().fillOval(img.getWidth()/6, img.getHeight()*5/6, PrimingEnvironmentPanel.DISC_SIZE, PrimingEnvironmentPanel.DISC_SIZE);
					img.getGraphics().setColor(Color.RED);
					img.getGraphics().fillOval(img.getWidth()*5/6, img.getHeight()/2, PrimingEnvironmentPanel.DISC_SIZE, PrimingEnvironmentPanel.DISC_SIZE);
				}
				else{
					img.getGraphics().fillOval(img.getWidth()/2, img.getHeight()/2, PrimingEnvironmentPanel.DOT_SIZE, PrimingEnvironmentPanel.DOT_SIZE);
					img.getGraphics().setColor(Color.RED);
					img.getGraphics().fillOval(img.getWidth()/6, img.getHeight()*5/6, PrimingEnvironmentPanel.DISC_SIZE, PrimingEnvironmentPanel.DISC_SIZE);
					img.getGraphics().setColor(Color.GREEN);
					img.getGraphics().fillOval(img.getWidth()*5/6, img.getHeight()/2, PrimingEnvironmentPanel.DISC_SIZE, PrimingEnvironmentPanel.DISC_SIZE);
				}
			}
			else if(TaskManager.getCurrentTick()>(10+700) && TaskManager.getCurrentTick()<(environment.getParam("blankDuration", 0)+10+700)){
				//blank data
				img.getGraphics().drawOval(img.getWidth()/2, img.getHeight()/2, PrimingEnvironmentPanel.DOT_SIZE, PrimingEnvironmentPanel.DOT_SIZE);
			}
			else{
				img.getGraphics().drawOval(img.getWidth()/2, img.getHeight()/2, PrimingEnvironmentPanel.DOT_SIZE, PrimingEnvironmentPanel.DOT_SIZE);
				img.getGraphics().setColor(Color.RED);
				img.getGraphics().drawOval(img.getWidth()/6, img.getHeight()*5/6, PrimingEnvironmentPanel.ANNULI_SIZE, PrimingEnvironmentPanel.ANNULI_SIZE);
				img.getGraphics().setColor(Color.GREEN);
				img.getGraphics().drawOval(img.getWidth()*5/6, img.getHeight()/2, PrimingEnvironmentPanel.ANNULI_SIZE, PrimingEnvironmentPanel.ANNULI_SIZE);
			}
			g.drawImage(scaledImage, xCentered, yCentered, this);
		}
	}
}
