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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.cluster.MatrixBasedAgglomerativeClusterer;
import nz.ac.vuw.ecs.kcassell.cluster.MemberCluster;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorEnum;
import nz.ac.vuw.ecs.kcassell.similarity.IdentifierDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.LevenshteinDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;

import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class SpanningForestView
implements ClusterUIConstants, ActionListener {

    /** The enclosing application of which this view is a part. */
	protected ExtC app = null;
        
	/** The main panel for this view. */
    protected JSplitPane mainPanel = null;
    
    /** Where descriptive text about the clusters is written. */
    protected JTextArea clustersTextArea = null;

	/** The visualization area for agglomerative clustering. */
    protected SpanningForestApplet clusteringApplet = null;

    /** The identifier of the graph on display. */
	protected String graphId = "";

	public SpanningForestView(SpanningForestApplet clusteringApplet, ExtC extC) {
		app = extC;
		this.clusteringApplet = clusteringApplet;
		clusteringApplet.setView(this);
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
	public JTextArea getClustersTextArea() {
		return clustersTextArea;
	}

	/**
	 * Creates the clustering applet and starts it
	 */
	protected void setUpView() {
		clustersTextArea = new JTextArea();
		clustersTextArea.setEditable(false);
		JScrollPane clusterScroller = new JScrollPane(clustersTextArea);
		clusteringApplet.setClustersTextArea(clustersTextArea);
		mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				clusterScroller, clusteringApplet);
        clusteringApplet.start();
		mainPanel.validate();
		mainPanel.repaint();
//		mainPanel.setDividerLocation(0.25);
	}

	/**
	 * Resets the agglomerative clusterer based on the chosen
	 * distance calculator.
	 */
	@SuppressWarnings("rawtypes")
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		String sourceName = "";
		if (source instanceof JComboBox) {
			JComboBox box = (JComboBox) source;
			sourceName = box.getName();
			if (SpanningForestApplet.CALCULATOR_COMBO.equals(sourceName)) {
				handleCalculatorRequest(box);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected void handleCalculatorRequest(JComboBox box) {
        String msg = "Not yet implemented";
        JOptionPane.showMessageDialog(mainPanel, msg,
                "Not yet implemented", JOptionPane.WARNING_MESSAGE);

//        Object selectedItem = box.getSelectedItem();
//		String sCalculator = selectedItem.toString();
//		ApplicationParameters parameters = ApplicationParameters.getSingleton();
//		parameters.setParameter(ParameterConstants.CALCULATOR_KEY,
//				sCalculator);
//		JavaCallGraph callGraph = clusteringApplet.getGraph();
//		setUpAgglomerativeClustering(callGraph);
	}

	/**
	 * Sets up the parts of the display that are calculator-dependent.
	 * 
	 * @param callGraph
	 */
	public void setUpSpanningForest(JavaCallGraph callGraph) {
		graphId = callGraph.getGraphId();
		SpanningForestApplet aggApplet = (SpanningForestApplet)clusteringApplet;
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		String sCalc =
			parameters.getParameter(ParameterConstants.CALCULATOR_KEY,
									DistanceCalculatorEnum.IntraClass.toString());

		try {
			if (DistanceCalculatorEnum.Identifier.toString().equals(sCalc)) {
				setUpIdentifierCalculation(callGraph, aggApplet);
			} else if (DistanceCalculatorEnum.Levenshtein.toString().equals(
							sCalc)) {
				setUpLevenshteinCalculation(callGraph, aggApplet);
			} else if (DistanceCalculatorEnum.IntraClass.toString().equals(
					sCalc)) {
				setUpIntraclassCalculation(aggApplet);
			}
		} catch (Exception e) {
			String msg = "Unable to set up agglomerative clustering";
			JOptionPane.showMessageDialog(mainPanel, msg, "UI Error",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
		mainPanel.setDividerLocation(0.25);
	}

	/**
	 * Sets up the parts of the view that are calculator dependent.
	 * @param aggApplet
	 * @throws IOException
	 */
	protected void setUpIntraclassCalculation(SpanningForestApplet aggApplet)
			throws IOException {
		String msg = "Spanning forest not yet available for IntraClass calculator";
		JOptionPane.showMessageDialog(mainPanel, msg,
				"Not yet implemented", JOptionPane.WARNING_MESSAGE);
		Forest<String, CallGraphLink> forest = null;
		aggApplet.setUpView(forest);

		// if (callGraph != null) {
		// JavaCallGraph undirectedGraph =
		// JavaCallGraph.toUndirectedGraph(callGraph);
		// IntraClassDistanceCalculator calculator =
		// new IntraClassDistanceCalculator(undirectedGraph);
		// GraphBasedAgglomerativeClusterer clusterer =
		// new GraphBasedAgglomerativeClusterer(
		// undirectedGraph, calculator);
		// setUpGenericClustering(clusterer, undirectedGraph);
		// }
	}

	/**
	 * Sets up the parts of the view that are calculator dependent.
	 * @param callGraph
	 * @param aggApplet
	 * @throws IOException
	 * @throws JavaModelException
	 */
	protected void setUpLevenshteinCalculation(JavaCallGraph callGraph,
			SpanningForestApplet aggApplet) throws JavaModelException,
			IOException {
		LevenshteinDistanceCalculator calc = new LevenshteinDistanceCalculator();
		List<String> memberHandles = EclipseUtils
				.getFilteredMemberHandles(callGraph.getHandle());
		MatrixBasedAgglomerativeClusterer clusterer = new MatrixBasedAgglomerativeClusterer(
				memberHandles, calc);
		Forest<String, CallGraphLink> forest = clusterer
				.createMinimumSpanningForest();
		// TODO change below to make something more generic
		MemberCluster cluster = clusterer.getSingleCluster();
		clustersTextArea.setText("Final cluster:\n"
				+ cluster.toNestedString());
		aggApplet.setUpView(forest);
		// aggApplet.setUpSouthPanel();
	}

	/**
	 * Sets up the parts of the view that are calculator dependent.
	 * @param callGraph
	 * @param aggApplet
	 * @throws IOException
	 * @throws JavaModelException
	 */
	protected void setUpIdentifierCalculation(JavaCallGraph callGraph,
			SpanningForestApplet aggApplet) throws JavaModelException,
			IOException {
		IdentifierDistanceCalculator calc = new IdentifierDistanceCalculator();
		List<String> simpleNames = EclipseUtils
				.getFilteredMemberNames(callGraph.getHandle());
		MatrixBasedAgglomerativeClusterer clusterer = new MatrixBasedAgglomerativeClusterer(
				simpleNames, calc);
		Forest<String, CallGraphLink> forest = clusterer
				.createMinimumSpanningForest();
		// calc.setClusterHistory(clusterer.getClusterHistory());
		// TODO change below to make something more generic
		MemberCluster cluster = clusterer.getSingleCluster();
		clustersTextArea.setText("Final cluster:\n"
				+ cluster.toNestedString());
		aggApplet.setUpView(forest);
		// aggApplet.setUpSouthPanel();
	}

	public String getGraphId() {
		return graphId ;
	}

	/**
	 * @return the visualization component containing the graph 
	 */
	protected VisualizationViewer<String, CallGraphLink> getVisualizer() {
		VisualizationViewer<String,CallGraphLink> visualizer =
			clusteringApplet.getVisualizer();
		return visualizer;
	}


}
