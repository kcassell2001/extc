/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Copyright (c) 2010, Keith Cassell
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following 
      disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the Victoria University of Wellington
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package nz.ac.vuw.ecs.kcassell.callgraph.gui;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.cluster.ClusterCombinationEnum;
import nz.ac.vuw.ecs.kcassell.cluster.MatrixBasedAgglomerativeClusterer;
import nz.ac.vuw.ecs.kcassell.cluster.MemberCluster;
import nz.ac.vuw.ecs.kcassell.cluster.MixedModeClusterer;
import nz.ac.vuw.ecs.kcassell.similarity.ClustererEnum;
import nz.ac.vuw.ecs.kcassell.similarity.CzibulaDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorEnum;
import nz.ac.vuw.ecs.kcassell.similarity.IdentifierDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.IdentifierGoogleDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.JDeodorantDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.LSACalculator;
import nz.ac.vuw.ecs.kcassell.similarity.LevenshteinDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.LocalNeighborhoodDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.SimonDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.VectorSpaceModelCalculator;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.FileUtils;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.forester.archaeopteryx.ArchaeopteryxE;

public class AgglomerationView
implements ClusterUIConstants, ParameterConstants, ActionListener {

	class ArchaeopteryxAppletStub implements AppletStub {

	    HashMap<String,String> params = new HashMap<String,String>();

	    public void appletResize(int width, int height) {}
	    public AppletContext getAppletContext() {
	        return null;
	    }

	    public URL getDocumentBase() {
	        return null;
	    }

	    public URL getCodeBase() {
	        return null;
	    }

	    public boolean isActive() {
	        return true;
	    }

	    public String getParameter(String name) {
	        return params.get(name);
	    }

	    public void setParameter(String name, String value) {
	        params.put(name, value);
	    }
	}	// class ArchaeopteryxAppletStub
	
	public static final String CALCULATOR_COMBO = "CalculatorCombo";
	public static final String CLUSTERER_COMBO = "ClustererCombo";
	public static final String LINK_COMBO = "LinkCombo";

    /** The enclosing application of which this view is a part. */
	protected ExtC app = null;
	
	/** The identifier of the graph, usually the Eclipse handle. */
	protected String graphId = "";
        
	/** The main panel for this view. */
    protected JComponent mainPanel = null;
    
    protected JSplitPane splitPane = null;
    
	/** The panel with the controls (e.g. JComboBoxes) */
	protected JPanel controlPanel = null;

	/** Where the user selects the distance calculator. */
	@SuppressWarnings("rawtypes")
	protected JComboBox calculatorBox = null;

	/** Where the user selects the clusterer. */
	@SuppressWarnings("rawtypes")
	protected JComboBox clustererBox = null;

	/** Where the user selects the group linkage type. */
	@SuppressWarnings("rawtypes")
	protected JComboBox groupLinkageBox = null;

	/** The visualization area for agglomerative clustering. */
    protected ArchaeopteryxE dendrogramComponent = null;

	public AgglomerationView(ExtC extC) {
		app = extC;
		setUpView();
	}

	/**
	 * @return the mainPanel
	 */
	public JComponent getMainPanel() {
		return mainPanel;
	}
	
	/**
	 * @return the graphId
	 */
	public String getGraphId() {
		return graphId;
	}

	/**
	 * Creates the clustering applet and starts it
	 */
	protected void setUpView() {
//		JScrollPane clusterTextScroller = new JScrollPane(clustersTextArea);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		setUpControlPanel();
		splitPane.add(controlPanel);
		setUpDendrogramApplet();
		splitPane.setDividerLocation(0.25);
		mainPanel = splitPane;
		mainPanel.validate();
		mainPanel.repaint();
	}

	protected JPanel setUpControlPanel() {
		controlPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(3, 2);
		JPanel grid = new JPanel(gridLayout);
		gridLayout.setHgap(15);
		controlPanel.add(grid);

		JLabel clustererLabel = new JLabel("Clusterer: ");
		grid.add(clustererLabel);
		clustererBox = createClustererCombo();
		clustererBox.addActionListener(this);
		grid.add(clustererBox);

		JLabel calcLabel = new JLabel("Distance Calculator: ");
		grid.add(calcLabel);
		calculatorBox = createCalculatorCombo();
		calculatorBox.addActionListener(this);
		grid.add(calculatorBox);

		JLabel groupLinkageLabel = new JLabel("Group Linkage: ");
		grid.add(groupLinkageLabel);
		groupLinkageBox = createLinkageCombo();
		groupLinkageBox.addActionListener(this);
		grid.add(groupLinkageBox);

		controlPanel.validate();
		return controlPanel;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JComboBox createLinkageCombo() {
		Vector<String> menuItems = new Vector<String>();
		for (ClusterCombinationEnum linkage : ClusterCombinationEnum.values()) {
			menuItems.add(linkage.toString());
		}
		JComboBox linkageBox = new JComboBox(menuItems);
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		String sLink = parameters.getParameter(
				LINKAGE_KEY,
				ClusterCombinationEnum.AVERAGE_LINK.toString());
		linkageBox.setSelectedItem(sLink);
		linkageBox.setName(LINK_COMBO);
		return linkageBox;
	}

	@SuppressWarnings("rawtypes")
	private JComboBox createCalculatorCombo() {
		Vector<String> menuItems = new Vector<String>();
		for (DistanceCalculatorEnum calc : DistanceCalculatorEnum.values()) {
			// TODO reincorporate GoogleDistance when code to handle its
			// long run-time is in place
			if ( calc != DistanceCalculatorEnum.GoogleDistance) {
				menuItems.add(calc.toString());
			}
		}
		@SuppressWarnings("unchecked")
		JComboBox calculatorBox = new JComboBox(menuItems);
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		String sCalc = parameters.getParameter(
				CALCULATOR_KEY,
				DistanceCalculatorEnum.IntraClass.toString());
		calculatorBox.setSelectedItem(sCalc);
		calculatorBox.setName(CALCULATOR_COMBO);
		return calculatorBox;
	}

	@SuppressWarnings("rawtypes")
	private JComboBox createClustererCombo() {
		Vector<String> menuItems = new Vector<String>();
		for (ClustererEnum calc : ClustererEnum.values()) {
			menuItems.add(calc.toString());
		}
		@SuppressWarnings("unchecked")
		JComboBox clustererBox = new JComboBox(menuItems);
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		String sClusterer = parameters.getParameter(
				CLUSTERER_KEY,
				ClustererEnum.AGGLOMERATIVE.toString());
		clustererBox.setSelectedItem(sClusterer);
		clustererBox.setName(CLUSTERER_COMBO);
		return clustererBox;
	}

	private void setUpDendrogramApplet() {
        String initialTreeFileName = RefactoringConstants.PROJECT_ROOT
        	+ "datasets/Dendrograms/no.tree";
        try {
			showDendrogram(initialTreeFileName);
		} catch (MalformedURLException e) {
			String msg = "Unable to show dendrogram for"
				+  initialTreeFileName;
			JOptionPane.showMessageDialog(mainPanel, msg,
					"Initialization Problem", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}

	public void showDendrogram(String treeFileName) throws MalformedURLException {
		ArchaeopteryxAppletStub stub = new ArchaeopteryxAppletStub();
        String configFileName = RefactoringConstants.PROJECT_ROOT
		        		+ "_aptx_configuration_file.txt";
        String configUrlString = FileUtils.toURLString(configFileName);
		stub.setParameter("config_file",  configUrlString);
//      APPLET_PARAM_NAME_FOR_CONFIG_FILE_URL isn't public!
        String treeUrlString = FileUtils.toURLString(treeFileName);
		stub.setParameter("url_of_tree_to_load", treeUrlString);
//      APPLET_PARAM_NAME_FOR_URL_OF_TREE_TO_LOAD isn't public!
        dendrogramComponent = new ArchaeopteryxE();
		splitPane.add(dendrogramComponent, JSplitPane.RIGHT, -1);
        dendrogramComponent.setStub(stub);
        dendrogramComponent.init();
		dendrogramComponent.start();
		dendrogramComponent.invalidate();
//		dendrogramComponent.validate();
//		splitPane.revalidate();
//		splitPane.repaint();
	}
	
	/**
	 * Resets the agglomerative clusterer based on the chosen
	 * distance calculator.
	 */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof JComboBox) {
			@SuppressWarnings("rawtypes")
			final JComboBox box = (JComboBox) source;
			final String sourceName = box.getName();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						mainPanel.setCursor(RefactoringConstants.WAIT_CURSOR);
						if (CALCULATOR_COMBO.equals(sourceName)) {
							handleCalculatorRequest(box);
						} else if (CLUSTERER_COMBO.equals(sourceName)) {
							handleClustererRequest(box);
						} else if (LINK_COMBO.equals(sourceName)) {
							handleGroupLinkageRequest(box);
						}
						mainPanel.validate();
						mainPanel.repaint();
					} finally {
						mainPanel.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				}
			}); // invokeLater
		}
	}

	@SuppressWarnings("rawtypes")
	protected void handleCalculatorRequest(JComboBox box) {
		ClusteringView.resetParameterValue(box, ParameterConstants.CALCULATOR_KEY);
		JavaCallGraph callGraph = app.getGraphView().getGraph();
		if (callGraph == null) {
			callGraph = app.graphView.getGraph();
		}
		if (callGraph == null) {
			String msg = "Choose a class for agglomerative clustering.";
			JOptionPane.showMessageDialog(mainPanel, msg,
					"Choose Class", JOptionPane.INFORMATION_MESSAGE);
		} else {
			performClustering(callGraph);
		}
	}

	/**
	 * Cluster based on the clusterer chosen by the user.
	 * @param box
	 */
	@SuppressWarnings("rawtypes")
	protected void handleClustererRequest(JComboBox box) {
		ClusteringView.resetParameterValue(box,
				ParameterConstants.CLUSTERER_KEY);
		JavaCallGraph callGraph = app.graphView.getGraph();

		if (callGraph == null) {
			String msg = "Choose a class for agglomerative clustering.";
			JOptionPane.showMessageDialog(mainPanel, msg, "Choose Class",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			performClustering(callGraph);
		}
	}

	protected void performClustering(JavaCallGraph callGraph) {
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		String sClusterer =
			parameters.getParameter(ParameterConstants.CLUSTERER_KEY,
					ClustererEnum.AGGLOMERATIVE.toString());
		
		if (ClustererEnum.AGGLOMERATIVE.toString()
				.equalsIgnoreCase(sClusterer)) {
			performAgglomerativeClustering(callGraph);
		} else if (ClustererEnum.MIXED_MODE.toString()
				.equalsIgnoreCase(sClusterer)) {
			performMixedModeClustering(callGraph);
		} else {
			String msg =
				"Clustering unavailable for " + sClusterer;
			JOptionPane.showMessageDialog(mainPanel, msg, "Unavailable",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	protected void performMixedModeClustering(JavaCallGraph callGraph) {
		String classHandle = callGraph.getHandle();
		JavaCallGraph undirectedGraph =
			JavaCallGraph.toUndirectedGraph(callGraph);
		MixedModeClusterer clusterer = new MixedModeClusterer(undirectedGraph);
		clusterer.cluster();
		Collection<MemberCluster> clusters = clusterer
				.getFinalMemberClusters();
		// Archaeopteryx displays individual trees, not forests, so
		// we group the final two clusters for display purposes
		// TODO find better way to display results
		MemberCluster mergedCluster = new MemberCluster();
		mergedCluster.setClusterName("final2");
		for (MemberCluster cluster : clusters) {
			mergedCluster.addCluster(cluster);
		}
		displayCluster(classHandle, mergedCluster);
	}

	@SuppressWarnings("rawtypes")
	protected void handleGroupLinkageRequest(JComboBox box) {
		ClusteringView.resetParameterValue(box, ParameterConstants.LINKAGE_KEY);
		JavaCallGraph callGraph = app.graphView.getGraph();
		if (callGraph == null) {
			String msg = "Choose a class for agglomerative clustering.";
			JOptionPane.showMessageDialog(mainPanel, msg,
					"Choose Class", JOptionPane.INFORMATION_MESSAGE);
		} else {
			performClustering(callGraph);
		}
	}

	/**
	 * Sets up the parts of the display that are calculator-dependent.
	 * @param callGraph
	 */
	public void performAgglomerativeClustering(JavaCallGraph callGraph) {
		String classHandle = callGraph.getHandle();
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		String sCalc =
			parameters.getParameter(ParameterConstants.CALCULATOR_KEY,
									DistanceCalculatorEnum.IntraClass.toString());
		DistanceCalculatorEnum calcType = DistanceCalculatorEnum.valueOf(sCalc);

		try {
			if (DistanceCalculatorEnum.GoogleDistance.equals(calcType)) {
				IdentifierGoogleDistanceCalculator calc =
					new IdentifierGoogleDistanceCalculator();
				MemberCluster cluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(classHandle, calc);
				displayCluster(classHandle, cluster);
			} else if (DistanceCalculatorEnum.Czibula.equals(calcType)) {
				CzibulaDistanceCalculator calc =
					new CzibulaDistanceCalculator(callGraph);
				MemberCluster cluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(classHandle, calc);
				displayCluster(classHandle, cluster);
			} else if (DistanceCalculatorEnum.Identifier.equals(calcType)) {
				IdentifierDistanceCalculator calc =
					new IdentifierDistanceCalculator();
				MemberCluster cluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(classHandle, calc);
				displayCluster(classHandle, cluster);
			} else if (DistanceCalculatorEnum.JDeodorant.equals(calcType)) {
				JDeodorantDistanceCalculator calc =
					new JDeodorantDistanceCalculator(callGraph);
				MemberCluster cluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(classHandle, calc);
				displayCluster(classHandle, cluster);
			} else if (DistanceCalculatorEnum.Levenshtein.equals(calcType)) {
				LevenshteinDistanceCalculator calc = new LevenshteinDistanceCalculator();
				MemberCluster cluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(classHandle, calc);
				displayCluster(classHandle, cluster);
			} else if (DistanceCalculatorEnum.LocalNeighborhood.equals(calcType)) {
				LocalNeighborhoodDistanceCalculator calc =
					new LocalNeighborhoodDistanceCalculator(callGraph);
				MemberCluster cluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(classHandle, calc);
				displayCluster(classHandle, cluster);
			} else if (DistanceCalculatorEnum.LSA.equals(calcType)) {
				LSACalculator calc =
			    	LSACalculator.getCalculator(classHandle);
				List<String> names =
					EclipseUtils.getFilteredMemberHandles(classHandle);
				MatrixBasedAgglomerativeClusterer clusterer =
					new MatrixBasedAgglomerativeClusterer(names, calc);
				MemberCluster cluster = clusterer.getSingleCluster();
				displayCluster(classHandle, cluster);
			} else if (DistanceCalculatorEnum.Simon.equals(calcType)) {
				SimonDistanceCalculator calc =
					new SimonDistanceCalculator(callGraph);
				MemberCluster cluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(classHandle, calc);
				displayCluster(classHandle, cluster);
			} else if (DistanceCalculatorEnum.VectorSpaceModel.equals(calcType)) {
				VectorSpaceModelCalculator calc =
			    	VectorSpaceModelCalculator.getCalculator(classHandle);
				List<String> names =
					EclipseUtils.getFilteredMemberHandles(classHandle);
				MatrixBasedAgglomerativeClusterer clusterer =
					new MatrixBasedAgglomerativeClusterer(names, calc);
				MemberCluster cluster = clusterer.getSingleCluster();
				displayCluster(classHandle, cluster);
			} else {
				String msg = "Unable to set up agglomerative clustering using " + sCalc;
				JOptionPane.showMessageDialog(mainPanel, msg,
						"Calculator Specification Error", JOptionPane.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			String msg = "Unable to set up agglomerative clustering";
			JOptionPane.showMessageDialog(mainPanel, msg,
					"UI Error", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}

	protected void displayCluster(String classHandle, MemberCluster cluster) {
//		displayClusterString(cluster);
		String file;
		try {
			String className = EclipseUtils.getNameFromHandle(classHandle);
			ApplicationParameters params = ApplicationParameters.getSingleton();
			String sClusterer =
				params.getParameter(ParameterConstants.CLUSTERER_KEY,
									ClustererEnum.AGGLOMERATIVE.toString());
			String sCalc =
				params.getParameter(ParameterConstants.CALCULATOR_KEY,
									DistanceCalculatorEnum.IntraClass.toString());
			String sLinkage = params.getParameter(
					ParameterConstants.LINKAGE_KEY,
					ClusterCombinationEnum.SINGLE_LINK.toString());
			file = MemberCluster.saveResultsToFile(className, cluster,
					sClusterer, sCalc, sLinkage);
			showDendrogram(file);
		} catch (IOException e) {
			String msg = "Problems preparing dendrogram";
			JOptionPane.showMessageDialog(mainPanel, msg,
					"Dendrogram Preparation Error", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets up the parts of the display that are calculator-dependent.
	 * @param callGraph
	 */
	public void setUpMixedModeClustering(JavaCallGraph callGraph) {
		String handle = callGraph.getHandle();
		graphId = callGraph.getGraphId();
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		String sCalc =
			parameters.getParameter(ParameterConstants.CALCULATOR_KEY,
									DistanceCalculatorEnum.IntraClass.toString());
		DistanceCalculatorEnum calcType = DistanceCalculatorEnum.valueOf(sCalc);

		try {
			if (DistanceCalculatorEnum.Identifier.equals(calcType)) {
				IdentifierDistanceCalculator calc =
					new IdentifierDistanceCalculator();
				MemberCluster sCluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(handle, calc);
				displayCluster(handle, sCluster);
			} else if (DistanceCalculatorEnum.GoogleDistance.equals(calcType)) {
				IdentifierGoogleDistanceCalculator calc =
					new IdentifierGoogleDistanceCalculator();
				MemberCluster sCluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(handle, calc);
				displayCluster(handle, sCluster);
			} else if (DistanceCalculatorEnum.Levenshtein.equals(calcType)) {
				LevenshteinDistanceCalculator calc =
					new LevenshteinDistanceCalculator();
				MemberCluster sCluster =
					MatrixBasedAgglomerativeClusterer
					.clusterUsingCalculator(handle, calc);
				displayCluster(handle, sCluster);
			} else if (DistanceCalculatorEnum.LSA.equals(calcType)) {
				LSACalculator calc =
			    	LSACalculator.getCalculator(handle);
				MemberCluster sCluster = MatrixBasedAgglomerativeClusterer.clusterUsingCalculator(handle, calc);
				displayCluster(handle, sCluster);
			} else if (DistanceCalculatorEnum.VectorSpaceModel.equals(calcType)) {
				VectorSpaceModelCalculator calc =
			    	VectorSpaceModelCalculator.getCalculator(handle);
				MemberCluster sCluster = MatrixBasedAgglomerativeClusterer.clusterUsingCalculator(handle, calc);
				displayCluster(handle, sCluster);
			} else {
				String msg = "Unable to set up agglomerative clustering using " + sCalc;
				JOptionPane.showMessageDialog(mainPanel, msg,
						"Calculator Specification Error", JOptionPane.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			String msg = "Unable to set up agglomerative clustering: " + e;
			JOptionPane.showMessageDialog(mainPanel, msg,
					"UI Error", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}

}
