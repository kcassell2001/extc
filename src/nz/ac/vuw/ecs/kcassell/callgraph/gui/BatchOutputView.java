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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.cluster.BetweennessClusterer;
import nz.ac.vuw.ecs.kcassell.cluster.ClusterCombinationEnum;
import nz.ac.vuw.ecs.kcassell.cluster.MatrixBasedAgglomerativeClusterer;
import nz.ac.vuw.ecs.kcassell.cluster.MemberCluster;
import nz.ac.vuw.ecs.kcassell.cluster.MixedModeClusterer;
import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;
import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth.FrequentMethodsMiner;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.persistence.RecordInserter;
import nz.ac.vuw.ecs.kcassell.persistence.SoftwareMeasurement;
import nz.ac.vuw.ecs.kcassell.similarity.ClientDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.ClustererEnum;
import nz.ac.vuw.ecs.kcassell.similarity.CzibulaDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorEnum;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorIfc;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCollector;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceMatrix;
import nz.ac.vuw.ecs.kcassell.similarity.IdentifierDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.IdentifierGoogleDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.IntraClassDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.JDeodorantDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.LevenshteinDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.LocalNeighborhoodDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.SimonDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.VectorSpaceModelCalculator;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseSearchUtils;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.GodClassesMM30;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.jung.graph.util.EdgeType;

public class BatchOutputView implements ActionListener, ParameterConstants {
	private static final String C3V_BUTTON_LABEL = "Calculate C3V";
	private static final String CLUSTER_BUTTON_LABEL = "Cluster Selections";
	private static final String CLUSTER30_BUTTON_LABEL = "Cluster 30 Open Source";
	private static final String CLUSTER6x30_BUTTON_LABEL = "Cluster 6x30 Open Source";

	/** The label used for the button to initiate a count of the
	 * number of disconnected subgraphs.  */
	private static final String DISCONNECTED_BUTTON_LABEL =
		"Disconnected Subgraphs";

	private static final String FREQUENT_METHODS_BUTTON_LABEL =
		"Frequent Methods";

	/** The label used for the button to initiate a count of the
	 * number of disconnected subgraphs after a single split based
	 * on betweenness clustering.  */
	private static final String TEST_BUTTON = "Test of the Day";
	private static final String DISTANCES_BUTTON_LABEL = "Compute Distances";
	
	private static final Dimension BUTTON_SIZE = new Dimension(150, 60);
//	private static final String  CLASS_SEPARATOR =
//		"--------------------------------------\n";

	private static final String  RUN_SEPARATOR =
		"======================================\n";

	/** The field separator for the CSV file. */
	static final String  CSV_SEP = "|";
	
	/** The main panel for this view. */
    private JSplitPane mainPanel = null;
    
    /** Where descriptive text about the clusters is written. */
    protected JTextArea textArea = null;

	/** The visualization area for agglomerative clustering. */
    private JPanel leftPanel = null;
    
    /** The enclosing application. */
    private ExtC app = null;
    
	private JLabel progressLabel = null;
    private JProgressBar progressBar = null;
    
    /** Accumulates the clustering results. */
	private StringBuffer buf = new StringBuffer(RUN_SEPARATOR);
	
	/** A writer for the cluster size data. */
	private BufferedWriter clusterCountWriter = null;

	/** A writer for the cluster size data. */
	private BufferedWriter clusterSizesWriter = null;


    protected static final UtilLogger logger =
    	new UtilLogger("BatchOutputView");

	public BatchOutputView(ExtC app) {
    	this.app = app;
		setUpView();
	}


	/**
	 * @return the mainPanel
	 */
	public JSplitPane getMainPanel() {
		return mainPanel;
	}

	/**
	 * @return the clustersTextArea
	 */
	public JTextArea getTextArea() {
		return textArea;
	}

	/**
	 * Creates the agglomeration applet and starts it
	 */
	protected void setUpView() {
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(12, 1));
		JButton aggButton = new JButton(CLUSTER_BUTTON_LABEL);
		aggButton.setPreferredSize(BUTTON_SIZE);
		aggButton.addActionListener(this);
		leftPanel.add(aggButton);

		JButton agg30Button = new JButton(CLUSTER30_BUTTON_LABEL);
		agg30Button.setPreferredSize(BUTTON_SIZE);
		agg30Button.addActionListener(this);
		leftPanel.add(agg30Button);

		JButton agg6x30Button = new JButton(CLUSTER6x30_BUTTON_LABEL);
		agg6x30Button.setPreferredSize(BUTTON_SIZE);
		agg6x30Button.addActionListener(this);
		leftPanel.add(agg6x30Button);

		JButton subgraphButton = new JButton(DISCONNECTED_BUTTON_LABEL);
		subgraphButton.setPreferredSize(BUTTON_SIZE);
		subgraphButton.addActionListener(this);
		leftPanel.add(subgraphButton);

		JButton distancesButton = new JButton(DISTANCES_BUTTON_LABEL);
		distancesButton.setPreferredSize(BUTTON_SIZE);
		distancesButton.addActionListener(this);
		leftPanel.add(distancesButton);

		JButton frequentMethodsButton = new JButton(FREQUENT_METHODS_BUTTON_LABEL);
		frequentMethodsButton.setPreferredSize(BUTTON_SIZE);
		frequentMethodsButton.addActionListener(this);
		leftPanel.add(frequentMethodsButton);
		
		JButton c3vButton = new JButton(C3V_BUTTON_LABEL);
		c3vButton.setPreferredSize(BUTTON_SIZE);
		c3vButton.addActionListener(this);
		leftPanel.add(c3vButton);

		progressLabel = new JLabel("Progress:");
		progressLabel.setVisible(false);
		progressLabel.setPreferredSize(BUTTON_SIZE);
		leftPanel.add(progressLabel);
		
		JButton subgraph1Button = new JButton(TEST_BUTTON);
		subgraph1Button.setPreferredSize(BUTTON_SIZE);
		subgraph1Button.addActionListener(this);
		leftPanel.add(subgraph1Button);

		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane textScroller = new JScrollPane(textArea);
		mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, textScroller);
		mainPanel.validate();
		mainPanel.repaint();
	}

	/**
	 * Reacts to the various push buttons
	 */
	public void actionPerformed(ActionEvent event) {
		final String command = event.getActionCommand();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					mainPanel.setCursor(RefactoringConstants.WAIT_CURSOR);

					if (C3V_BUTTON_LABEL.equals(command)) {
						calculateC3V(mainPanel);
					} else if (CLUSTER_BUTTON_LABEL.equals(command)) {
						clusterAllSelections(mainPanel);
					} else if (CLUSTER30_BUTTON_LABEL.equals(command)) {
						clusterOpen30(mainPanel);
					} else if (CLUSTER6x30_BUTTON_LABEL.equals(command)) {
						cluster6x30(mainPanel);
					} else if (C3V_BUTTON_LABEL.equals(command)) {
							calculateC3V(mainPanel);
					} else if (DISCONNECTED_BUTTON_LABEL.equals(command)) {
						countAllDisconnectedSubgraphs(mainPanel);
					} else if (DISTANCES_BUTTON_LABEL.equals(command)) {
						collectDistances(mainPanel);
					} else if (FREQUENT_METHODS_BUTTON_LABEL.equals(command)) {
						collectFrequentMethods(mainPanel);
					} else if (TEST_BUTTON.equals(command)) {
							clusterOpen30(mainPanel);
//						GodClassesMM30 godClassesMM30 = new GodClassesMM30();
//						godClassesMM30.printMetricValues();
//						calculateC3V(mainPanel);
//						clusterUsingClientDistances();
					} // TEST_BUTTON
					textArea.repaint();
				} finally {
					mainPanel.setCursor(RefactoringConstants.DEFAULT_CURSOR);
				}
			}
		}); // invokeLater
	}

	@SuppressWarnings("unused")
	private void clusterUsingClientDistances() {
		GraphView graphView = app.getGraphView();
		JavaCallGraph callGraph = graphView.getGraph();

		if (callGraph == null) {
			String msg = "Choose a class.";
			JOptionPane.showMessageDialog(mainPanel, msg,
				"No class chosen", JOptionPane.WARNING_MESSAGE);
		} else {
			ClientDistanceCalculator calculator;
			try {
				textArea.setText("");
				// initialize the calculator and build the data file
				String classHandle = callGraph.getHandle();
				calculator = new ClientDistanceCalculator(classHandle);
				String memberClientsFile = 
					ClientDistanceCalculator.getClientDataFileNameFromHandle(classHandle);
				String documents = calculator
					.buildDocumentsForPublicMethods(callGraph, memberClientsFile);
				textArea.setText(documents);
				String fileName = calculator.getDataFileNameFromHandle(classHandle);
				calculator.initializeSemanticSpace(fileName);
				
				// Aggl. clustering using the ClientDistanceCalculator
				List<String> memberHandles = calculator.getMemberHandles();
				MatrixBasedAgglomerativeClusterer clusterer =
					new MatrixBasedAgglomerativeClusterer(
						memberHandles, calculator);
				MemberCluster cluster = clusterer.getSingleCluster();
				String clusterString =
					(cluster == null) ? "no cluster" : cluster.toNestedString();
				buf.append("Final cluster:\n" + clusterString);
				textArea.append(clusterString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	// else
		textArea.repaint();
	}

	/**
	 * Calculate Conceptual Cohesion of Classes (C3V) using a 
	 * vector space model.
	 */
	private void calculateC3VForGodClasses() {
		try {
			textArea.setText("");
			// initialize the calculator and build the data file
			GodClassesMM30 mm30 = new GodClassesMM30();
			List<String> types = mm30.getCommandLineClasses();
			
			VectorSpaceModelCalculator calc = null;
			int prefKey = 5; // TODO RecordInserter.getPreferencesKey();
			List<SoftwareMeasurement> measurements = new ArrayList<SoftwareMeasurement>();
			for (String typeId : types) {
				try {
					calc = VectorSpaceModelCalculator.getCalculator(typeId);
					Double cohesion = calc.calculateConceptualCohesion(typeId);
					// TODO get value based on graph view see
					// MetricsDBTransaction.getPreferencesKey
					SoftwareMeasurement measurement = new SoftwareMeasurement(
							typeId, SoftwareMeasurement.C3V, cohesion, prefKey);
					measurements.add(measurement);
					textArea.append("C3V for " + typeId + " = " + cohesion + "\n");
				} catch (Exception jme) {
					logger.warning("Unable to calculate C3V for " + typeId);
					textArea.append("Unable to calculate C3V for " + typeId + "\n");
					//jme.printStackTrace();
				}
			}
			RecordInserter inserter = new RecordInserter();
			inserter.saveMeasurementsToDB(measurements);
		} catch (Exception e) {
			e.printStackTrace();
		}
		textArea.repaint();
	}

	/**
	 * Calculate Conceptual Cohesion of Classes (C3V) using a 
	 * vector space model.
	 */
	@SuppressWarnings("unused")
	private void calculateC3VForTypesInProject() {
		GraphView graphView = app.getGraphView();
		JavaCallGraph callGraph = graphView.getGraph();

		if (callGraph == null) {
			String msg = "Choose a class.";
			JOptionPane.showMessageDialog(mainPanel, msg,
				"No class chosen", JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				textArea.setText("");
				// initialize the calculator and build the data file
				String classHandle = callGraph.getHandle();
				VectorSpaceModelCalculator calc =
					VectorSpaceModelCalculator.getCalculator(classHandle);
//				LSACalculator calc =
//			    	LSACalculator.getCalculator(classHandle);
				IType type = EclipseUtils.getTypeFromHandle(classHandle);
				IJavaProject project =
					(IJavaProject)type.getAncestor(IJavaElement.JAVA_PROJECT);
				List<IType> types = EclipseSearchUtils.getTypes(project);
			    //Integer prefKey = 1;
				int prefKey = RecordInserter.getPreferencesKey();
				List<SoftwareMeasurement> measurements = new ArrayList<SoftwareMeasurement>();
				for (IType aType : types) {
					String typeId = aType.getHandleIdentifier();
				    Double cohesion = calc.calculateConceptualCohesion(typeId);
				    // TODO get value based on graph view see MetricsDBTransaction.getPreferencesKey
					SoftwareMeasurement measurement =
				    	new SoftwareMeasurement(typeId, SoftwareMeasurement.C3V, cohesion, prefKey);
				    measurements.add(measurement);
					textArea.append("C3V for " + typeId + " = " + cohesion + "\n");
				}
				RecordInserter inserter = new RecordInserter();
				inserter.saveMeasurementsToDB(measurements);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	// else
		textArea.repaint();
	}

	/**
	 * Clusters the members of 30 open source classes.
	 * @return
	 */
	protected StringBuffer clusterOpen30() {
		ApplicationParameters params = ApplicationParameters.getSingleton();
		String sClusterer = params.getParameter(
				CLUSTERER_KEY, ClustererEnum.MIXED_MODE.toString());
		textArea.append("Params = " + params + "\n");
		String sCalc = params.getParameter(
				ParameterConstants.CALCULATOR_KEY,
				DistanceCalculatorEnum.IntraClass.toString());
		String sLinkage = params.getParameter(
				ParameterConstants.LINKAGE_KEY,
				ClusterCombinationEnum.SINGLE_LINK.toString());
		cluster30(sClusterer, sCalc, sLinkage);
		return buf;
	}

	/**
	 * Agglomeratively clusters the members of 30 open source classes using
	 *  6 different combinations of distance function and group linkage.
	 */
	protected StringBuffer cluster6x30() {
		String[] calculators = { DistanceCalculatorEnum.LocalNeighborhood.toString(),
				DistanceCalculatorEnum.Simon.toString()
		};
		String[] linkages = { ClusterCombinationEnum.SINGLE_LINK.toString(),
				ClusterCombinationEnum.AVERAGE_LINK.toString(),
				ClusterCombinationEnum.COMPLETE_LINK.toString()
		};
		
		for (int i = 0; i < calculators.length; i++) {
			for (int j = 0; j < linkages.length; j++) {
				textArea.append("Agglomerating with: " + calculators[i] +
						" (" + linkages[j] + ")\n");
				cluster30(ClustererEnum.AGGLOMERATIVE.toString(),
						calculators[i], linkages[j]);
			}
		}
		return buf;
	}


	/**
	 * Clusters the members of 30 open source classes.
	 */
	private void cluster30(String sClusterer, String sCalc, String sLinkage) {
		logger.info("Aggregating using " + sClusterer + " and " + sCalc);
		GodClassesMM30 mm30 = new GodClassesMM30();
		List<String> classHandles = mm30.getAllClasses();
		// "=Weka/<weka.classifiers.meta{MultiClassClassifier.java[MultiClassClassifier";

		String clusterCountsFile = RefactoringConstants.DATA_DIR +
			sClusterer + sCalc + sLinkage + "Counts.csv";
		String clusterSizesFile = RefactoringConstants.DATA_DIR +
		sClusterer + sCalc + sLinkage + "Sizes.csv";

		try {
			clusterCountWriter = new BufferedWriter(new FileWriter(clusterCountsFile));
			clusterSizesWriter = new BufferedWriter(new FileWriter(clusterSizesFile));
			String csvHeader = "Class" + CSV_SEP + 0.999 + CSV_SEP
				+ 0.9 + CSV_SEP + 0.75 + CSV_SEP + 0.5 + "\n";
			clusterCountWriter.write(csvHeader);
			clusterSizesWriter.write(csvHeader);
			buf.append(csvHeader);

			int iterations = classHandles.size();
			activateProgressBar(iterations);
			for (int i = 0; i < iterations; i++) {
				progressBar.setValue(i);
				String handle = classHandles.get(i);
				clusterOne(sClusterer, sCalc, sLinkage, handle);
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
		} finally {
			closeClusterFiles();
			inactivateProgressBar();
		}
	}


	private void closeClusterFiles() {
		if (clusterCountWriter != null) {
			try {
				clusterCountWriter.close();
			} catch (Exception e1) {
				// just ignore it
			}
		}
		if (clusterSizesWriter != null) {
			try {
				clusterSizesWriter.close();
			} catch (Exception e1) {
				// just ignore it
			}
		}
	}


	/**
	 * Clusters the members of all classes in the metrics view.
	 * @return
	 */
	protected StringBuffer clusterSelections() {
		ApplicationParameters params = ApplicationParameters.getSingleton();
		String sClusterer = params.getParameter(
				CLUSTERER_KEY, ClustererEnum.MIXED_MODE.toString());
		textArea.append("Parame = " + params + "\n");
		String sCalc = params.getParameter(
				ParameterConstants.CALCULATOR_KEY,
				DistanceCalculatorEnum.IntraClass.toString());
		String linkage = params.getParameter(ParameterConstants.LINKAGE_KEY,
				ClusterCombinationEnum.AVERAGE_LINK.toString());
		logger.info("Aggregating using " + sClusterer + " and " + sCalc);
		MetricsView metricsView = app.getMetricsView();
		String[] classHandles = metricsView.getClassHandles();
		// "=Weka/<weka.classifiers.meta{MultiClassClassifier.java[MultiClassClassifier";

		int iterations = classHandles.length; // Math.min(20, classHandles.length);
		activateProgressBar(iterations);
		for (int i = 0;  i < iterations; i++) {
			progressBar.setValue(i);
			String handle = classHandles[i];
			clusterOne(sClusterer, sCalc, linkage, handle);
		}
		inactivateProgressBar();
		return buf;
	}


	protected void clusterOne(String sClusterer, String sCalc, String linkage,
			String handle) {
//		buf = new StringBuffer(RUN_SEPARATOR);
//		buf.append(handle).append("\n");
//		long start = System.currentTimeMillis();
		try {
			JavaCallGraph callGraph = getGraphFromHandle(handle);
			// TODO other calculators for all clusterers
			DistanceCalculatorEnum calcEnum = DistanceCalculatorEnum.valueOf(sCalc);
			if (ClustererEnum.AGGLOMERATIVE.toString().equalsIgnoreCase(
					sClusterer)) {
				try {
					if (DistanceCalculatorEnum.IntraClass.equals(calcEnum)) {
						DistanceCalculatorIfc<String> calc = new IntraClassDistanceCalculator(
								callGraph);
						agglomerateUsingCalculator(handle, calc, linkage);
					} else if (DistanceCalculatorEnum.Czibula.equals(calcEnum)) {
						DistanceCalculatorIfc<String> calc = new CzibulaDistanceCalculator(
								callGraph);
						agglomerateUsingCalculator(handle, calc, linkage);
					} else if (DistanceCalculatorEnum.Identifier.equals(calcEnum)) {
						DistanceCalculatorIfc<String> calc = new IdentifierDistanceCalculator();
						agglomerateUsingCalculator(handle, calc, linkage);
					} else if (DistanceCalculatorEnum.JDeodorant.equals(calcEnum)) {
						DistanceCalculatorIfc<String> calc = new JDeodorantDistanceCalculator(
								callGraph);
						agglomerateUsingCalculator(handle, calc, linkage);
					} else if (DistanceCalculatorEnum.Levenshtein.equals(calcEnum)) {
						DistanceCalculatorIfc<String> calc = new LevenshteinDistanceCalculator();
						agglomerateUsingCalculator(handle, calc, linkage);
					} else if (DistanceCalculatorEnum.LocalNeighborhood.equals(calcEnum)) {
						DistanceCalculatorIfc<String> calc = new LocalNeighborhoodDistanceCalculator(
								callGraph);
						agglomerateUsingCalculator(handle, calc, linkage);
					} else if (DistanceCalculatorEnum.Simon.equals(calcEnum)) {
						DistanceCalculatorIfc<String> calc = new SimonDistanceCalculator(
								callGraph);
						agglomerateUsingCalculator(handle, calc, linkage);
					} else if (DistanceCalculatorEnum.VectorSpaceModel.equals(calcEnum)) {
						DistanceCalculatorIfc<String> calc = VectorSpaceModelCalculator
								.getCalculator(handle);
						agglomerateUsingCalculator(handle, calc, linkage);
					} else if (DistanceCalculatorEnum.GoogleDistance.equals(calcEnum)) {
						try {
							DistanceCalculatorIfc<String> calc = new IdentifierGoogleDistanceCalculator();
							agglomerateUsingCalculator(handle, calc, linkage);
						} catch (Exception e) {
							String msg = "Unable to calculate distances.  (No web access?)";
							JOptionPane.showMessageDialog(mainPanel, msg,
									"Error Clustering",
									JOptionPane.WARNING_MESSAGE);
						}
					}
				} catch (Exception e) {
					showAgglomerationError(sCalc, e);
				}
			} else if (ClustererEnum.BETWEENNESS.toString().equalsIgnoreCase(
					sClusterer)) {
				Collection<CallGraphNode> clusters = clusterUsingBetweenness(callGraph);
				buf.append("Final clusters for " + callGraph.getName());
				appendClusterSizes(clusters);
				String sClusters = toOutputString(clusters);
				buf.append(":\n" + sClusters);
			} else if (ClustererEnum.MIXED_MODE.toString().equalsIgnoreCase(
					sClusterer)) {
				Collection<CallGraphNode> clusters = clusterUsingMixedMode(callGraph);
				buf.append("Final clusters for " + callGraph.getName());
				appendClusterSizes(clusters);
				String sClusters = toOutputString(clusters);
				buf.append(":\n" + sClusters);
			}
			textArea.append(buf.toString());
		} catch (JavaModelException e) {
			buf.append(e.toString());
			e.printStackTrace();
		}
//		long end = System.currentTimeMillis();
//		buf.append("Clustering above took " + (end - start) + " millis");
//		buf.append(CLASS_SEPARATOR);
	}

	protected void showAgglomerationError(String sCalc, Exception e) {
		String msg = "Problem agglomerating with the " + sCalc + "calculator: " + e;
		JOptionPane.showMessageDialog(mainPanel, msg,
			"Error Clustering", JOptionPane.WARNING_MESSAGE);
	}


	/**
	 * Use agglomerative clustering with the indicated distance
	 * calculator to form clusters.
	 * @param sCalc the name of the calculator
	 * @param handle the handle of the class whose members are to be clustered
	 * @param calc the distance calculator
	 * @throws JavaModelException
	 */
	protected MemberCluster agglomerateUsingCalculator(String handle,
			DistanceCalculatorIfc<String> calc,
			String linkage) throws Exception {
		MemberCluster cluster =
			MatrixBasedAgglomerativeClusterer
			.clusterUsingCalculator(handle, calc, linkage);
		String className = EclipseUtils.getNameFromHandle(handle);
		MemberCluster.saveResultsToFile(className, cluster,
				ClustererEnum.AGGLOMERATIVE.toString(),
				calc.getType().toString(), linkage);
		// TODO move this elsewhere
		ArrayList<Object> clusters999 = cluster.getClustersAtDistance(0.999);
		ArrayList<Object> clusters9 = cluster.getClustersAtDistance(0.9);
		ArrayList<Object> clusters75 = cluster.getClustersAtDistance(0.75);
		ArrayList<Object> clusters5 = cluster.getClustersAtDistance(0.5);
		String countRow = writeClusterCounts(className, clusters999, clusters9,
				clusters75, clusters5);
		writeClusterSizes(className, clusters999, clusters9,
				clusters75, clusters5);
		buf.append(countRow);

		return cluster;
	}


	private String writeClusterCounts(String className, ArrayList<Object> clusters999,
			ArrayList<Object> clusters9, ArrayList<Object> clusters75, ArrayList<Object> clusters5)
			throws IOException {
		int count999 = clusters999.size();
		int count9 = clusters9.size();
		int count75 = clusters75.size();
		int count5 = clusters5.size();
		String row = className + CSV_SEP + count999 + CSV_SEP
		+ count9 + CSV_SEP + count75 + CSV_SEP + count5 + "\n";
		clusterCountWriter.write(row);
		clusterCountWriter.flush();
		return row;
	}

	private String writeClusterSizes(String className, ArrayList<Object> clusters999,
			ArrayList<Object> clusters9, ArrayList<Object> clusters75, ArrayList<Object> clusters5)
			throws IOException {
		String sizes999 = MemberCluster.clusterSizesToString(clusters999);
		String sizes9 = MemberCluster.clusterSizesToString(clusters9);
		String sizes75 = MemberCluster.clusterSizesToString(clusters75);
		String sizes5 = MemberCluster.clusterSizesToString(clusters5);
		String row = className + CSV_SEP + sizes999 + CSV_SEP
		+ sizes9 + CSV_SEP + sizes75 + CSV_SEP + sizes5 + "\n";
		clusterSizesWriter.write(row);
		clusterSizesWriter.flush();
		return row;
	}


	/**
	 * Append the sizes of the clusters to the output buffer.
	 * @param clusters
	 */
	private void appendClusterSizes(Collection<CallGraphNode> clusters) {
		buf.append(" (");
		for (CallGraphNode node : clusters) {
			if (node instanceof CallGraphCluster) {
				CallGraphCluster cluster = (CallGraphCluster)node;
				buf.append(cluster.getElementCount());
			} else {
				buf.append("1");
			}
			buf.append(", ");
		}
		buf.delete(buf.length()-2, buf.length()); // omit last ", "
		buf.append(")");
	}

	/**
	 * Cluster using the BetweennessClusterer.
	 * @param callGraph
	 * @throws JavaModelException
	 */
	public static Collection<CallGraphNode> clusterUsingBetweenness(JavaCallGraph callGraph)
	throws JavaModelException {
		JavaCallGraph undirectedGraph =
			JavaCallGraph.toUndirectedGraph(callGraph);
		BetweennessClusterer clusterer = new BetweennessClusterer(undirectedGraph);

		// Get intial group sizes, without clustering
		Collection<CallGraphNode> clusters = clusterer.cluster(0);
		List<Integer> sizes =
			CallGraphCluster.getClusterSizes(clusters);
		Collections.sort(sizes);
		int clusterCount = sizes.size();
		
		// If there are disconnected groups before clustering, check their
		// sizes
		if (clusterCount > 1) {
			Integer largest2 = sizes.get(clusterCount - 2);
			
			// If the second largest cluster is above the threshold for new
			// class size, do no more.  
			if (largest2 < 7) { 
				clusters = clusterer.cluster();
			}
		} else { // Break up the single group
			clusters = clusterer.cluster();
		}
		ArrayList<CallGraphNode> nodeClusters =
			new ArrayList<CallGraphNode>(clusters);
		Collections.sort(nodeClusters, BetweennessClusterer.getSizeComparator());
		return nodeClusters;
	}

	/**
	 * Cluster using the MixedModeClusterer.
	 * @param callGraph
	 * @throws JavaModelException
	 */
	public static Collection<CallGraphNode> clusterUsingMixedMode(JavaCallGraph callGraph)
	throws JavaModelException {
		JavaCallGraph undirectedGraph =
			JavaCallGraph.toUndirectedGraph(callGraph);
		MixedModeClusterer clusterer = new MixedModeClusterer(undirectedGraph);
		Collection<CallGraphNode> clusters = clusterer.cluster();
		return clusters;
	}


	private static String toOutputString(Collection<CallGraphNode> clusters) {
		StringBuffer buf = new StringBuffer();
		for (CallGraphNode node: clusters) {
			buf.append(node.toNestedString());
			buf.append("\n");
		}
		String clusterString = buf.toString();
		return clusterString;
	}

	protected void activateProgressBar(int limit) {
		if (progressBar != null) {
			leftPanel.remove(progressBar);
		}
		progressBar = new JProgressBar(0, limit);
		progressBar.setPreferredSize(BUTTON_SIZE);
		leftPanel.add(progressBar);
		progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressLabel.setVisible(true);
		leftPanel.validate();
		leftPanel.repaint();
	}

	protected void inactivateProgressBar() {
		if (progressBar != null) {
			leftPanel.remove(progressBar);
		}
		progressBar = null;
        progressLabel.setVisible(false);
		leftPanel.validate();
		leftPanel.repaint();
	}

	/**
	 * Counts the number of members in each disconnected subgraph for each
	 * of the class handles obtained from the metrics view.
	 * condensed
	 */
	protected void countDisconnectedSubgraphs() {
		textArea.append(RUN_SEPARATOR);
		MetricsView metricsView = app.getMetricsView();
		String[] classHandles = metricsView.getClassHandles();
		
		if ((classHandles == null)
				|| ((classHandles.length == 1)
						&& classHandles[0].contains("none found"))) {
			String msg = "Nothing (from the metrics view) to count";
			JOptionPane.showMessageDialog(mainPanel, msg,
				"Error Clustering", JOptionPane.WARNING_MESSAGE);
		} else {
			ArrayList<String> candidates = new ArrayList<String>();
			activateProgressBar(classHandles.length);

			for (int i = 0; i < classHandles.length; i++) {
				long start = System.currentTimeMillis();

				try {
					JavaCallGraph callGraph =
						getGraphFromHandle(classHandles[i]);
					BetweennessClusterer calc = new BetweennessClusterer(callGraph);
					Collection<CallGraphNode> clusters = calc.cluster(0);
					List<Integer> sizes =
						CallGraphCluster.getClusterSizes(clusters);
					Collections.sort(sizes);
					int clusterCount = sizes.size();
					if (clusterCount > 1) {
						Integer largest1 = sizes.get(clusterCount - 1);
						Integer largest2 = sizes.get(clusterCount - 2);
						ApplicationParameters parameters =
							app.getApplicationParameters();
						Integer maxMembers =
							parameters.getIntParameter(MAX_MEMBERS_KEY, 20);

						if ((largest1 + largest2) > maxMembers) {
							candidates.add(classHandles[i]);
						}
					}
					textArea.append("Initial cluster sizes for " +
							classHandles[i] + ": " + sizes);
					progressBar.setValue(i);
				} catch (JavaModelException e) {
					textArea.append(e.toString());
					e.printStackTrace();
				}
				long end = System.currentTimeMillis();
				textArea.append("Clustering above took " + (end - start) + " millis\n");
			}
			textArea.append("Extract class candidates: (" + 
					candidates.size() + "/" + classHandles.length + ")\n\t" + 
					candidates + "\n");
			inactivateProgressBar();
		}
	}

	/**
	 * Get the call graph corresponding to the handle
	 * @param classHandle the handle indicating the class
	 * @return a java call graph
	 * @throws JavaModelException
	 */
	protected JavaCallGraph getGraphFromHandle(
			String classHandle) throws JavaModelException {
		// A temporary graph to use for calling getAltGraphUsingParams
		JavaCallGraph callGraph = new JavaCallGraph();
//			new JavaCallGraph(classHandle, EdgeType.DIRECTED);
		callGraph.setHandle(classHandle);
		callGraph.setDefaultEdgeType(EdgeType.DIRECTED);
		callGraph = callGraph.getAltGraphUsingParams();
		return callGraph;
	}

	/**
	 * Run agglomerative clustering on all classes in the metric view and
	 * report the results in the text area.
	 * @param mainPane the component on which to put the wait cursor
	 */
	public void clusterAllSelections(final Component mainPane) {
		System.out.println("clustering...");

		Thread worker = new Thread("BatchClusterThread") {

			public void run() {

				try {
					try {
						textArea.setText("");
						mainPane.setCursor(RefactoringConstants.WAIT_CURSOR);
						clusterSelections();
					} finally {
						mainPane.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while clustering: "
							+ e.getMessage();
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainPane, msg,
							"Error Clustering", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Run agglomerative clustering on all classes in the metric view and
	 * report the results in the text area.
	 * @param mainPane the component on which to put the wait cursor
	 */
	public void clusterOpen30(final Component mainPane) {
		System.out.println("clustering...");

		Thread worker = new Thread("BatchClusterThread") {

			public void run() {

				try {
					try {
						textArea.setText("");
						mainPane.setCursor(RefactoringConstants.WAIT_CURSOR);
						clusterOpen30();
					} finally {
						mainPane.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while clustering: "
							+ e.getMessage();
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainPane, msg,
							"Error Clustering", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Agglomeratively clusters the members of 30 open source classes using
	 *  6 different combinations of distance function and group linkage.
	 * @param mainPane the component on which to put the wait cursor
	 */
	public void cluster6x30(final Component mainPane) {
		System.out.println("clustering...");

		Thread worker = new Thread("Batch6x30ClusterThread") {

			public void run() {

				try {
					try {
						textArea.setText("");
						mainPane.setCursor(RefactoringConstants.WAIT_CURSOR);
						cluster6x30();
					} finally {
						mainPane.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Determine the disconnected subgraphs for each class and print the sizes
	 * of them.
	 * @param component the component on which to put the wait cursor
	 */
	public void countAllDisconnectedSubgraphs(final Component component) {
		System.out.println("counting subgraphs...");

		Thread worker = new Thread("SubgraphsThread") {

			public void run() {

				try {
					try {
						component.setCursor(RefactoringConstants.WAIT_CURSOR);
						countDisconnectedSubgraphs();
					} finally {
						component.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while counting subgraphs: "
							+ e.getMessage();
					JOptionPane.showMessageDialog(component, msg,
							"Error Clustering", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Determine the distances between the members for each class.
	 * @param mainPane the component on which to put the wait cursor
	 */
	public void collectDistances(final Component mainPane) {
		System.out.println("collecting distances...");

		Thread worker = new Thread("CollectDistancesThread") {

			public void run() {

				try {
					try {
						mainPane.setCursor(RefactoringConstants.WAIT_CURSOR);
						collectDistances();
					} finally {
						mainPane.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while collecting distances: "
							+ e.getMessage();
					JOptionPane.showMessageDialog(mainPane, msg,
							"Error Collecting Distances", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Determines the frequently called methods in a client class
	 * @param mainPane the component on which to put the wait cursor
	 */
	public void collectFrequentMethods(final Component mainPane) {
		System.out.println("collecting frequentMethods...");

		Thread worker = new Thread("CollectFrequentMethodsThread") {

			public void run() {

				try {
					try {
						mainPane.setCursor(RefactoringConstants.WAIT_CURSOR);
						collectFrequentMethods();
					} finally {
						mainPane.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while collecting frequentMethods: "
							+ e.getMessage();
					JOptionPane.showMessageDialog(mainPane, msg,
							"Error Collecting FrequentMethods", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Calculates conceptual cohesion of classes (C3V) using a vector
	 * space model
	 * @param mainPane the component on which to put the wait cursor
	 */
	private void calculateC3V(final Component mainPane) {
		System.out.println("calculating C3V...");

		Thread worker = new Thread("calculateC3VThread") {

			public void run() {

				try {
					try {
						mainPane.setCursor(RefactoringConstants.WAIT_CURSOR);
						calculateC3VForGodClasses();
//						calculateC3VForTypesInProject();
					} finally {
						mainPane.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while calculating C3V: "
							+ e.getMessage();
					JOptionPane.showMessageDialog(mainPane, msg,
							"Error calculating C3V", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Collects distance measurements between the members of the
	 * class visible in the graph view.
	 */
	protected void collectDistances() {
		textArea.append(RUN_SEPARATOR);
		GraphView graphView = app.getGraphView();
		JavaCallGraph callGraph = graphView.getGraph();

		if (callGraph == null) {
			String msg = "Choose a class.";
			JOptionPane.showMessageDialog(mainPanel, msg,
				"No class chosen", JOptionPane.WARNING_MESSAGE);
		} else {
			List<CallGraphNode> nodes = callGraph.getNodes();
			List<String> memberNames = getMemberNames(nodes);
			ArrayList<DistanceCalculatorIfc<String>> calculators =
				initializeCalculators(callGraph);
			DistanceCollector collector = new DistanceCollector(calculators);
			activateProgressBar(calculators.size());
			int i = 0;

			for (DistanceCalculatorIfc<String> calc : calculators) {
				long start = System.currentTimeMillis();

				DistanceMatrix<String> matrix =
					collector.collectDistances(memberNames, calc);
				textArea.append(calc.getType().toString() +
						" distances for " + callGraph.getName() + ":\n");
				textArea.append(matrix.toString());
				progressBar.setValue(i++);
				long end = System.currentTimeMillis();
				textArea.append("Distance calculation above took " + (end - start) + " millis\n");
			}
			inactivateProgressBar();
		}
	}

	/**
	 * Collects the frequently called methods of the
	 * class visible in the graph view.
	 * @throws CoreException 
	 */
	protected void collectFrequentMethods() throws CoreException {
		textArea.append(RUN_SEPARATOR);
		GraphView graphView = app.getGraphView();
		JavaCallGraph callGraph = graphView.getGraph();

		if (callGraph == null) {
			String msg = "Choose a class.";
			JOptionPane.showMessageDialog(mainPanel, msg,
				"No class chosen", JOptionPane.WARNING_MESSAGE);
		} else {
			FrequentMethodsMiner miner = new FrequentMethodsMiner();
			String handle = callGraph.getHandle();
			Collection<ItemSupportList> frequentMethods =
				miner.getFrequentFrequentlyUsedMethods(handle);
			String patternsToString =
				ItemSupportList.patternsToString(frequentMethods);
			textArea.append("frequent patterns using " + handle + ":\n" + patternsToString);
			textArea.append("---- end frequent patterns ----" + patternsToString);
			inactivateProgressBar();
		}
	}

	/**
	 * @param callGraph a dependency graph between class members
	 * @return a list of calculators that will calculate distances
	 * between the nodes of the supplied graph
	 */
	private ArrayList<DistanceCalculatorIfc<String>> initializeCalculators(
			JavaCallGraph callGraph) {
		ArrayList<DistanceCalculatorIfc<String>> calculators =
			new ArrayList<DistanceCalculatorIfc<String>>();
		
//		CzibulaDistanceCalculator czibulaCalculator =
//			new CzibulaDistanceCalculator(callGraph);
//		calculators.add(czibulaCalculator);
		
		IdentifierGoogleDistanceCalculator googleCalculator = null;
		try {
			googleCalculator = new IdentifierGoogleDistanceCalculator();
			googleCalculator.clearCache();
			calculators.add(googleCalculator);
		} catch (Exception e) {
			String msg = "Unable to initialize GoogleDistanceCalculator:\n" + e;
			JOptionPane.showMessageDialog(mainPanel, msg,
				"Error Initializing", JOptionPane.WARNING_MESSAGE);
		}
		
//		IntraClassDistanceCalculator intraCalculator =
//			new IntraClassDistanceCalculator(callGraph);
//		calculators.add(intraCalculator);
//		
//		SimonDistanceCalculator simonCalculator =
//			new SimonDistanceCalculator(callGraph);
//		calculators.add(simonCalculator);
		return calculators;
	}

	/**
	 * Collects the member names from the graph's nodes.
	 * @param nodes
	 * @return the list of names
	 */
	private List<String> getMemberNames(List<CallGraphNode> nodes) {
		List<String> memberNames = new ArrayList<String>();
		
		for (CallGraphNode node : nodes) {
			memberNames.add(node.getSimpleName());
		}
		Collections.sort(memberNames);
		return memberNames;
	}


}
