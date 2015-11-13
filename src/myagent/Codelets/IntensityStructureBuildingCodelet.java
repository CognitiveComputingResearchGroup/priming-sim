/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package myagent.Codelets;

import edu.memphis.ccrg.lida.framework.initialization.GlobalInitializer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.memphis.ccrg.lida.framework.shared.Linkable;
import edu.memphis.ccrg.lida.framework.shared.Node;
import edu.memphis.ccrg.lida.framework.shared.NodeStructure;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import edu.memphis.ccrg.lida.workspace.WorkspaceContent;
import edu.memphis.ccrg.lida.workspace.workspacebuffers.WorkspaceBuffer;
import edu.memphis.ccrg.lida.workspace.structurebuildingcodelets.StructureBuildingCodeletImpl;
import edu.memphis.ccrg.lida.framework.shared.ElementFactory;

/**
 *
 * @author Daqi
 */
public class IntensityStructureBuildingCodelet extends StructureBuildingCodeletImpl {

    private static Logger logger = Logger.getLogger(IntensityStructureBuildingCodelet.class.getCanonicalName());
    private Node highLevelNode;
    private static ElementFactory factory = ElementFactory.getInstance();
    public static final double HIGHLEVEL_ACTIVATION = 1.0;

    /**
     * Default constructor
     */
    public IntensityStructureBuildingCodelet() {
    }

    @Override
    public void init() {
        super.init();
        String lowNodeLabels = (String) getParam("low_nodes", "leftDark,rightDark");
        if (lowNodeLabels != null) {
            GlobalInitializer globalInitializer = GlobalInitializer.getInstance();
            String[] labels = lowNodeLabels.split(",");
            for (String label : labels) {
                label = label.trim();
                Node node = (Node) globalInitializer.getAttribute(label);
                if (node != null) {
                    soughtContent.addDefaultNode(node);
                } else {
                    logger.log(Level.WARNING, "could not find node with label: {0} in global initializer", label);
                }
            }
        }

        highLevelNode = factory.getNode();
        String highNodeLabel = (String) getParam("high_node", "bothFar");
        if (highNodeLabel != null) {
            highNodeLabel = highNodeLabel.trim();
            highLevelNode.setLabel(highNodeLabel);
            highLevelNode.setActivation(HIGHLEVEL_ACTIVATION);
        }
    }

    @Override
    protected void runThisFrameworkTask() {
        logger.log(Level.FINEST, "SB codelet {1} being run.",
                new Object[]{TaskManager.getCurrentTick(), this});
        for (WorkspaceBuffer readableBuffer : readableBuffers.values()) {
            if (bufferContainsSoughtContent(readableBuffer)) {
                //logger.log(Level.INFO, "high level code is written into CSM");
                writableBuffer.addBufferContent((WorkspaceContent) retrieveWorkspaceContent(null));
            } else {
                //logger.log(Level.INFO, "high level code is NOT written into CSM");
            }
        }

        logger.log(Level.FINEST, "SB codelet {1} finishes one run.",
                new Object[]{TaskManager.getCurrentTick(), this});
    }

    @Override
    public NodeStructure retrieveWorkspaceContent(WorkspaceBuffer buffer) {
        NodeStructure ns = factory.getNodeStructure();
        ns.addDefaultNode(highLevelNode);
        return ns;
    }

    @Override
    public boolean bufferContainsSoughtContent(WorkspaceBuffer buffer) {
        NodeStructure ns = (NodeStructure) buffer.getBufferContent(null);
        for (Linkable ln : soughtContent.getLinkables()) {
            if (!ns.containsLinkable(ln)) {
                return false;
            }
        }
        logger.log(Level.FINEST, "SBcodelet {1} found sought content",
                new Object[]{TaskManager.getCurrentTick(), this});
        return true;
    }
}
