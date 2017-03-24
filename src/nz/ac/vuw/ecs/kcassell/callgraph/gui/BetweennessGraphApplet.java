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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.cluster.BetweennessClusterer;

import org.apache.commons.collections15.functors.MapTransformer;

import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class BetweennessGraphApplet extends ClusteringGraphApplet implements
		ClusterUIConstants {
	protected static final long serialVersionUID = 1L;
	protected BetweennessClusterer betweennessClusterer = null;
	protected AggregateLayout<CallGraphNode, CallGraphLink> aggregateLayout = null;

	public BetweennessGraphApplet() {
		sliderLabelBase = "Edges removed for clusters: ";
	}

	public void setUpView(final JavaCallGraph callGraph) throws IOException {
		betweennessClusterer = new BetweennessClusterer(callGraph);
		graph = callGraph;
		Graph<CallGraphNode, CallGraphLink> jungGraph =
			callGraph.getJungGraph();
		if (aggregateLayout != null) {
			aggregateLayout.removeAll();
		}
		setupVisualizerAndLayout(jungGraph);
		
		betweennessClusterer.cluster(0);
		Collection<CallGraphNode> clusters =
			betweennessClusterer.getClusters();
		final JToggleButton clusterToggle = createClusterToggle(jungGraph);
		redisplayNodes(clusterToggle.isSelected(), clusters);

		TitledBorder sliderBorder = setUpSliderPanel();
		addChangeListenerToSlider(clusterToggle, sliderBorder);

		Container content = getContentPane();
        content.removeAll();
		content.add(new GraphZoomScrollPane(visualizer));
		JPanel southPanel = setUpSouthPanel(clusterToggle);
		content.add(southPanel, BorderLayout.SOUTH);
		displayClusterText();
		visualizer.validate();
		visualizer.repaint();
	}

	/**
	 * Create a simple layout frame with the Fruchterman-Rheingold layout
	 * algorithm
	 * 
	 * @param graph
	 */
	protected void setupVisualizerAndLayout(
			final Graph<CallGraphNode, CallGraphLink> graph) {
		aggregateLayout = new AggregateLayout<CallGraphNode, CallGraphLink>(
				new FRLayout<CallGraphNode, CallGraphLink>(graph));

		visualizer = new VisualizationViewer<CallGraphNode, CallGraphLink>(
				aggregateLayout);
		setUpMouseMode();
		visualizer.setBackground(Color.white);
		// Tell the renderer to use our own customized color rendering
		setRenderContextTransformers();
	}

	protected JToggleButton createClusterToggle(
			final Graph<CallGraphNode, CallGraphLink> graph) {
		final JToggleButton clusterToggle = new JToggleButton("Group/Ungroup");
		clusterToggle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean doGroup = (e.getStateChange() == ItemEvent.SELECTED);
				redrawGraph(graph, doGroup, iterationSlider.getValue());
				sliderPanel.repaint();
			}
		});
		return clusterToggle;
	}

	protected JPanel createToIgnorePanel() {
		Dimension desiredSize = new Dimension(250, 100);

		JPanel ignorePanel = new JPanel();
		// ignorePanel.setPreferredSize(desiredSize);
		ignorePanel.setLayout(new BoxLayout(ignorePanel, BoxLayout.X_AXIS));

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		JLabel ignoreLable = new JLabel("Select members to filter out:");
		leftPanel.add(ignoreLable);

		JButton nowButton = createActivateFilterButton();
		leftPanel.add(nowButton);
		ignorePanel.add(leftPanel);

		final JScrollPane list = setUpNodesToIgnoreList(desiredSize);
		ignorePanel.add(list);
		return ignorePanel;
	}

	/** (non-Javadoc)
	 * Sets up the node renderer to color groups differently
	 */
	protected void setRenderContextVertexTransformers(
			RenderContext<CallGraphNode, CallGraphLink> renderContext) {
	    setVertexLabeler(renderContext);
	    renderContext.setVertexFillPaintTransformer(
	    		MapTransformer.<CallGraphNode, Paint> getInstance(vertexPaints));
	    setVertexDrawPaintTransformer(renderContext);
	    setVertexShapeTransformer(renderContext);
	}

	/**
	 * @param desiredSize
	 * @return
	 */
	protected JScrollPane setUpNodesToIgnoreList(Dimension desiredSize) {
		Vector<String> contents = collectCentralNodeNames();
		final JList list = new JList(contents);
		JScrollPane listScroller = new JScrollPane(list);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(5);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					membersToIgnore = list.getSelectedValues();
					for (Object node : membersToIgnore) {
						logger.fine("User wants to ignore " + node);
						System.out.println("User wants to ignore " + node);
					}
				}
			}
		});
		return listScroller;
	}

	/**
	 * @return
	 */
	protected Vector<String> collectCentralNodeNames() {
		Vector<String> contents = new Vector<String>();
		JavaCallGraph callGraph = betweennessClusterer.getCallGraph();
		Graph<CallGraphNode, CallGraphLink> jungGraph = callGraph.getJungGraph();
		TreeSet<CallGraphNode> orderedNodes = betweennessClusterer
				.calculateHubScores(jungGraph);
		// betweennessCalculator.calculateBarycentricScores(jungGraph);

		for (CallGraphNode node : orderedNodes) {
			contents.add(node.getLabel());
		}
		return contents;
	}

	/**
	 * @return
	 */
	protected JButton createActivateFilterButton() {
		JButton nowButton = new JButton("Activate Filter");
		nowButton.setActionCommand(FILTER_COMMAND);
		nowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (FILTER_COMMAND.equals(e.getActionCommand())) {
					graph.removeFromGraph(membersToIgnore);
					redrawGraph(graph.getJungGraph(), false, 0);
				}
			}
		});
		return nowButton;
	}

	protected JPanel setUpSouthPanel(final JToggleButton clusterToggle) {
		JPanel south = new JPanel();
		JPanel grid = new JPanel(new GridLayout(1, 2));
		grid.add(clusterToggle);
		grid.add(sliderPanel);
		south.add(grid);
		JPanel ignorePanel = createToIgnorePanel();
		south.add(ignorePanel);
		setUpMouseMode();
		// The line below is commented out, because it's not clear what
		// the TRANSFORMING mode is providing us, so we use the functionality
		// w/o giving the user access to the control.
		// TODO figure out a more elegant way to have pickable/movable nodes
		// south.add(mouseModePanel);
		south.validate();
		return south;
	}

	protected void addChangeListenerToSlider(
	// final AggregateLayout<CallGraphNode, CallGraphLink> layout,
			final JToggleButton groupVertices, final TitledBorder sliderBorder) {
		// final Graph<CallGraphNode, CallGraphLink> graph = layout.getGraph();

		iterationSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					boolean doGroup = groupVertices.isSelected();
					int numEdgesToRemove = source.getValue();
					redrawGraph(graph.getJungGraph(), doGroup, numEdgesToRemove);
					sliderBorder.setTitle(sliderLabelBase
							+ iterationSlider.getValue());
					sliderPanel.validate();
					sliderPanel.repaint();
				}
			}
		});
	}

	protected void redrawGraph(final Graph<CallGraphNode, CallGraphLink> graph,
			boolean doGroup, int numEdgesToRemove) {
		aggregateLayout.removeAll();
		betweennessClusterer.cluster(numEdgesToRemove);
		Collection<CallGraphNode> clusters =
			betweennessClusterer.getClusters();
		redisplayNodes(doGroup, clusters);
		recolorEdges(graph, betweennessClusterer.getEdgesRemoved());
		displayClusterText();
		visualizer.validate();
		visualizer.repaint();
	}

	/**
	 * @return the betweennessCalculator
	 */
	public BetweennessClusterer getBetweennessCalculator() {
		return betweennessClusterer;
	}

	/**
	 * @param betweennessClusterer
	 *            the betweennessCalculator to set
	 */
	public void setBetweennessCalculator(
			BetweennessClusterer betweennessClusterer) {
		this.betweennessClusterer = betweennessClusterer;
	}

	public void redisplayNodes(boolean doGroup, Collection<CallGraphNode> nodes) {
		int i = 0;
		// Set the colors of each node so that each cluster's vertices have the
		// same color
		for (CallGraphNode node : nodes) {
			Color color = CLUSTER_COLORS[i % CLUSTER_COLORS.length];

			if (node instanceof CallGraphCluster) {
				CallGraphCluster cluster = (CallGraphCluster) node;
				colorCluster(cluster, color);
				if (doGroup) {
					condenseClusterGroup(cluster);
				} else {
					diluteClusterGroup(cluster);
				}
				i++;
			}
		}
	}

	public void recolorEdges(Graph<CallGraphNode, CallGraphLink> graph,
			List<CallGraphLink> edgesRemoved) {
		for (CallGraphLink e : graph.getEdges()) {
			if (edgesRemoved.contains(e)) {
				edgePaints.put(e, DELETED_EDGE_COLOR);
			} else {
				edgePaints.put(e, CONNECTED_EDGE_COLOR);
			}
		}
	}

	protected void condenseClusterGroup(CallGraphCluster cluster) {
		logger.info("Nodes to condense " + cluster.toString());
		SortedSet<CallGraphNode> nodes = cluster.getElements();

		if (nodes.size() < aggregateLayout.getGraph().getVertexCount()) {
			// Arbitrarily choose a node whose position will be the
			// center of the cluster's circle
			// (the node with the highest betweenness score for now)
			CallGraphNode centralNode = nodes.first();
			logger.finer("Centering cluster on " + centralNode);
			Point2D center = aggregateLayout.transform(centralNode);
			Graph<CallGraphNode, CallGraphLink> subGraph = SparseMultigraph
					.<CallGraphNode, CallGraphLink> getFactory().create();
			for (CallGraphNode node : nodes) {
				node.setShowToString(node.getId() == centralNode.getId());
				subGraph.addVertex(node);
			}
			Layout<CallGraphNode, CallGraphLink> subLayout = new CircleLayout<CallGraphNode, CallGraphLink>(
					subGraph);
			subLayout.setInitializer(visualizer.getGraphLayout());
			subLayout.setSize(new Dimension(40, 40));

			aggregateLayout.put(subLayout, center);
			visualizer.repaint();
		}
	}

	protected void diluteClusterGroup(CallGraphCluster cluster) {
		Set<CallGraphNode> nodes = cluster.getElements();

		if (nodes.size() < graph.getJungGraph().getVertexCount()) {
			for (CallGraphNode node : nodes) {
				node.setShowToString(true);
			}
		}
		visualizer.repaint();
	}
	
	/**
	 * Displays text corresponding to the clusters in the text area.
	 */
	protected void displayClusterText() {
		StringBuffer buf = new StringBuffer();
		Collection<CallGraphNode> clusters =
			betweennessClusterer.getClusters();
		
		if (clustersTextArea != null) {
			String clustersString = groupsToString(buf, clusters);
			clustersTextArea.setText(clustersString);
			clustersTextArea.setCaretPosition(0);
			clustersTextArea.repaint();
		}
	}

}
