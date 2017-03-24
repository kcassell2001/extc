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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.GraphCondenser;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.ScoreType;
import nz.ac.vuw.ecs.kcassell.callgraph.gui.transformers.CallGraphNodeShapeTransformer;
import nz.ac.vuw.ecs.kcassell.callgraph.gui.transformers.NodeStrokeTransformer;
import nz.ac.vuw.ecs.kcassell.callgraph.gui.transformers.NodeTypeColorer;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;

/**
 * This class contains the UI components for display of a
 * call graph.
 * @author Keith
 *
 */
public class GraphView 
implements ActionListener, ParameterConstants, ClusterUIConstants, ItemListener
{
	private static final String FALSE = "false";
	private static final String TRUE = "true";
	private static final String EDGE_TYPE_COMBO = "EdgeTypeCombo";
	private static final String LAYOUT_COMBO = "LayoutCombo";
    private static final String SIZING_COMBO = "SizingCombo";

    /** The enclosing application of which this view is a part. */
	private ExtC app = null;
        
	/** The main panel has a graph and graph controls. */
    protected JPanel mainPanel = new JPanel();
     
	/** This panel will contain graph info. */
    protected JPanel graphPanel = new JPanel();
     
	/** This panel will contain the graph controls. */
    protected JPanel controlPanel = null;
    
    /** The name of the class being graphed. */
    protected JLabel graphLabel = null;
     
    /** Where the user selects the edge type - directed or undirected. */
    @SuppressWarnings("rawtypes")
	protected JComboBox edgeTypeBox = null;

    /** Where the user selects the layout. */
    @SuppressWarnings("rawtypes")
	protected JComboBox layoutBox = null;

    /** Where the user selects how to size nodes. */
    @SuppressWarnings("rawtypes")
	protected JComboBox sizingBox = null;
	
	/** Indicates whether constructors should be included in the graph. */
    protected JCheckBox includeConstructorsButton = null;
	
	/** Indicates whether nodes involved in recursive cycles
	 *  should be condensed. */
    protected JCheckBox condenseCyclesButton = null;
	
	/** Indicates whether inherited members should be included in the graph. */
    protected JCheckBox includeInheritedButton = null;
	
	/** Indicates whether inner class members should be included in the graph. */
    protected JCheckBox includeInnersButton = null;
	
	/** Indicates whether methods declared on Object
	 *  should be filtered. */
    protected JCheckBox includeObjectMethodsButton = null;
	
	/** Indicates whether fields of type java.util.logging.Logger
	 *  should be filtered. */
    protected JCheckBox includeLoggerButton = null;
	
	/** Indicates whether static members should be included in the graph. */
    protected JCheckBox includeStaticsButton = null;
	
	/** Indicates whether nodes representing required methods
	 *  should be condensed. */
    protected JCheckBox condenseRequiredMethodsButton = null;

	/** Indicates whether nodes representing methods inherited from Object 
	 * should be condensed. */
    protected JCheckBox condenseObjectsMethodsButton = null;

	/** The graph to display. */
    protected JavaCallGraph graph = null;
    
    /** The directed graph to display. */
    protected JavaCallGraph directedGraph = null;
    
    /** The undirected graph to display. */
    protected JavaCallGraph undirectedGraph = null;
    
    /** How to lay out the graph. */
//    protected Layout<CallGraphNode, CallGraphLink> graphLayout = null;
    
    /** The panel where the class's call graph is displayed. */
    protected VisualizationViewer<CallGraphNode, CallGraphLink> visViewer = null;
    
    protected CallGraphNodeShapeTransformer vertexShapeTransformer = null;
    
    /** A flag to indicate whether the graph needs to be laid out afresh. */
    protected boolean doLayout = true;

    /** The default vertex to paint transformer colors fields red
     * and methods white.   */
    protected Transformer<CallGraphNode, Paint> vertexPaintTransformer =
        new NodeTypeColorer();

    public GraphView(ExtC extC)
    {
    	app = extC;
    	createControlPanel();
    	BorderLayout borderLayout = new BorderLayout();
    	mainPanel.setLayout(borderLayout);
    	mainPanel.add(controlPanel, BorderLayout.WEST);
    	mainPanel.add(graphPanel, BorderLayout.CENTER);
    }


    protected void createControlPanel() {
    	controlPanel = new JPanel();
    	GridLayout gridLayout = new GridLayout(25, 2);
    	controlPanel.setLayout(gridLayout);
    	ApplicationParameters parameters = app.getApplicationParameters();
    	
    	graphLabel = new JLabel("Graph View");
    	graphLabel.setBackground(Color.WHITE);
    	graphLabel.setForeground(Color.BLUE);
    	controlPanel.add(graphLabel);

    	controlPanel.add(new JLabel("Graph Edge Type:"));
    	edgeTypeBox = createEdgeTypeCombo();
    	edgeTypeBox.addActionListener(this);
    	controlPanel.add(edgeTypeBox);

    	controlPanel.add(new JLabel("Layout:"));
    	layoutBox = createLayoutCombo();
    	layoutBox.addActionListener(this);
    	controlPanel.add(layoutBox);

    	controlPanel.add(new JLabel("Node Sizing:"));
    	sizingBox = createSizingCombo();
    	sizingBox.addActionListener(this);
    	controlPanel.add(sizingBox);

    	controlPanel.add(new JSeparator());
    	controlPanel.add(new JLabel("Include Nodes:"));

    	includeConstructorsButton = new JCheckBox(INCLUDE_CONSTRUCTORS);
    	boolean includeConstructors =
    		parameters.getBooleanParameter(INCLUDE_CONSTRUCTORS_KEY, false);
        includeConstructorsButton.setSelected(includeConstructors);
        includeConstructorsButton.addItemListener(this);
        controlPanel.add(includeConstructorsButton);
        
    	includeStaticsButton = new JCheckBox(INCLUDE_STATIC_MEMBERS);
    	boolean includeStatics =
    		parameters.getBooleanParameter(INCLUDE_STATICS_KEY, false);
        includeStaticsButton.setSelected(includeStatics);
        includeStaticsButton.addItemListener(this);
        controlPanel.add(includeStaticsButton);
        
    	includeInheritedButton = new JCheckBox(INCLUDE_INHERITED);
    	boolean includeInherited =
    		parameters.getBooleanParameter(INCLUDE_INHERITED_KEY, false);
        includeInheritedButton.setSelected(includeInherited);
        includeInheritedButton.addItemListener(this);
        controlPanel.add(includeInheritedButton);

    	includeInnersButton = new JCheckBox(INCLUDE_INNERS);
    	boolean includeInners =
    		parameters.getBooleanParameter(INCLUDE_INNERS_KEY, false);
        includeInnersButton.setSelected(includeInners);
        includeInnersButton.addItemListener(this);
        controlPanel.add(includeInnersButton);

    	includeLoggerButton = new JCheckBox(INCLUDE_LOGGERS);
    	boolean includeLogger =
    		parameters.getBooleanParameter(INCLUDE_LOGGERS_KEY, true);
        includeLoggerButton.setSelected(includeLogger);
        includeLoggerButton.addItemListener(this);
        controlPanel.add(includeLoggerButton);

    	includeObjectMethodsButton = new JCheckBox(INCLUDE_OBJECT_METHODS);
    	boolean includeObjectMethods =
    		parameters.getBooleanParameter(INCLUDE_OBJECT_METHODS_KEY, true);
        includeObjectMethodsButton.setSelected(includeObjectMethods);
        includeObjectMethodsButton.addItemListener(this);
        controlPanel.add(includeObjectMethodsButton);

    	controlPanel.add(new JSeparator());
    	controlPanel.add(new JLabel("Condense Nodes:"));
    	
    	condenseCyclesButton = new JCheckBox(CONDENSE_CYCLES);
    	boolean condenseCycles =
    		parameters.getBooleanParameter(CONDENSE_RECURSIVE_CYCLES_KEY, false);
        condenseCyclesButton.setSelected(condenseCycles);
        condenseCyclesButton.addItemListener(this);
        controlPanel.add(condenseCyclesButton);
        
        condenseRequiredMethodsButton = new JCheckBox(CONDENSE_REQUIRED_METHODS);
    	boolean condenseRequired =
    		parameters.getBooleanParameter(CONDENSE_IMPOSED_METHODS_KEY, false);
    	condenseRequiredMethodsButton = new JCheckBox(CONDENSE_REQUIRED_METHODS);
    	condenseRequiredMethodsButton.setSelected(condenseRequired);
        condenseRequiredMethodsButton.addItemListener(this);
        controlPanel.add(condenseRequiredMethodsButton);
        
        condenseObjectsMethodsButton = new JCheckBox(CONDENSE_OBJECTS_METHODS);
    	boolean condenseObjects =
    		parameters.getBooleanParameter(CONDENSE_OBJECTS_METHODS_KEY, false);
    	condenseObjectsMethodsButton = new JCheckBox(CONDENSE_OBJECTS_METHODS);
    	condenseObjectsMethodsButton.setSelected(condenseObjects);
        condenseObjectsMethodsButton.addItemListener(this);
        controlPanel.add(condenseObjectsMethodsButton);
	}


	@SuppressWarnings("rawtypes")
	private JComboBox createLayoutCombo() {
		Vector<String> layouts = new Vector<String>();
        for (GraphLayoutEnum layout : GraphLayoutEnum.values())
        {
        	layouts.add(layout.toString());
        }
    	@SuppressWarnings("unchecked")
		JComboBox layoutBox = new JComboBox(layouts);
    	ApplicationParameters parameters = app.getApplicationParameters();
    	String sLayout =
    		parameters.getParameter(GRAPH_LAYOUT_KEY, GraphLayoutEnum.FRLayout.toString());
    	layoutBox.setSelectedItem(sLayout);
    	layoutBox.setName(LAYOUT_COMBO);
		return layoutBox;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JComboBox createEdgeTypeCombo() {
		Vector<String> edgeTypes = new Vector<String>();
        for (EdgeType edgeType : EdgeType.values())
        {
        	edgeTypes.add(edgeType.toString());
        }
    	JComboBox edgeTypeBox = new JComboBox(edgeTypes);
    	ApplicationParameters parameters = app.getApplicationParameters();
    	String sEdgeType =
    		parameters.getParameter(EDGE_TYPE_KEY, EdgeType.DIRECTED.toString());
    	edgeTypeBox.setSelectedItem(sEdgeType);
    	edgeTypeBox.setName(EDGE_TYPE_COMBO);
		return edgeTypeBox;
	}

	@SuppressWarnings("rawtypes")
	private JComboBox createSizingCombo() {
		String[] options = {
//				ScoreType.MCCABE,
//				ScoreType.NESTEDBLOCKDEPTH,
				ScoreType.AUTHORITY,
				ScoreType.HUB,
				ScoreType.INDEGREE,
				ScoreType.OUTDEGREE};
    	@SuppressWarnings("unchecked")
		JComboBox box = new JComboBox(options);
    	ApplicationParameters parameters = app.getApplicationParameters();
    	String sSizing =
    		parameters.getParameter(NODE_SIZING_KEY, ScoreType.AUTHORITY.toString());
    	box.setSelectedItem(sSizing);
    	box.setName(SIZING_COMBO);
		return box;
    }

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

    /**
	 * @return the graph
	 */
	public JavaCallGraph getGraph() {
		return graph;
	}


	/**
	 * @param graph the graph to set
	 */
	public void setGraph(JavaCallGraph graph) {
		this.graph = graph;
	}


	/**
	 * @return the directedGraph
	 */
	public JavaCallGraph getDirectedGraph() {
		return directedGraph;
	}


	/**
	 * @param directedGraph the directedGraph to set
	 */
	public void setDirectedGraph(JavaCallGraph directedGraph) {
		this.directedGraph = directedGraph;
	}


	/**
	 * @return the undirectedGraph
	 */
	public JavaCallGraph getUndirectedGraph() {
		return undirectedGraph;
	}


	/**
	 * @param undirectedGraph the undirectedGraph to set
	 */
	public void setUndirectedGraph(JavaCallGraph undirectedGraph) {
		this.undirectedGraph = undirectedGraph;
	}


    private void setUpVertexLabeling(
            BasicVisualizationServer<CallGraphNode, CallGraphLink> visServer)
    {
        RenderContext<CallGraphNode, CallGraphLink> renderContext =
        	visServer.getRenderContext();
        ToStringLabeller<CallGraphNode> labeller = 
        	new ToStringLabeller<CallGraphNode>();
		renderContext.setVertexLabelTransformer(labeller);
        Renderer<CallGraphNode, CallGraphLink> renderer = visServer.getRenderer();
        BasicVertexLabelRenderer<CallGraphNode, CallGraphLink> labelRenderer =
            new BasicVertexLabelRenderer<CallGraphNode, CallGraphLink>();
        labelRenderer.setPosition(Renderer.VertexLabel.Position.S);
        renderer.setVertexLabelRenderer(labelRenderer);
    }

    protected void setEdgeLabeler(
            RenderContext<CallGraphNode, CallGraphLink> renderContext) {
        renderContext.setEdgeLabelTransformer(new ToStringLabeller<CallGraphLink>());
        Renderer<CallGraphNode, CallGraphLink> renderer = visViewer.getRenderer();
        BasicEdgeLabelRenderer<CallGraphNode, CallGraphLink> labelRenderer =
            new BasicEdgeLabelRenderer<CallGraphNode, CallGraphLink>();
        //labelRenderer.setPosition(Renderer.EdgeLabel.Position.CNTR);
        renderer.setEdgeLabelRenderer(labelRenderer);
    }
    
    protected void makeMousePickable() {
        DefaultModalGraphMouse<?, ?> graphMouse =
            new DefaultModalGraphMouse<Object, Object>();
        graphMouse.setMode(Mode.PICKING);
        visViewer.setGraphMouse(graphMouse);
    }
    
	public static Layout<CallGraphNode, CallGraphLink> getLayout(
			String sLayout, Graph<CallGraphNode, CallGraphLink> graph) {
		Layout<CallGraphNode, CallGraphLink> layout;
		if (GraphLayoutEnum.CircleLayout.toString().equals(sLayout)) {
			layout = new CircleLayout<CallGraphNode, CallGraphLink>(graph);
		} else if (GraphLayoutEnum.DAGLayout.toString().equals(sLayout)) {
			// layout = new DAGLayoutBattista<CallGraphNode,
			// CallGraphLink>(graph);
			DAGLayout<CallGraphNode, CallGraphLink> dagLayout = new DAGLayout<CallGraphNode, CallGraphLink>(
					graph);
			/*
			 * This value is used to specify how strongly an edge "wants" to be
			 * its default length (higher values indicate a greater attraction
			 * for the default length), which affects how much its endpoints
			 * move at each timestep. The default value is 1/3.
			 */
			// double forceMultiplier = dagLayout.getForceMultiplier();
			/*
			 * Outside this range, nodes do not repel each other. The default
			 * value is 100.
			 */
			// int repulsionRange = dagLayout.getRepulsionRange();
			// dagLayout.setRepulsionRange(5);
			/*
			 * This value specifies how much the degrees of an edge's incident
			 * vertices should influence how easily the endpoints of that edge
			 * can move (that is, that edge's tendency to change its
			 * length).</p> The default value is 0.70. Positive values less than
			 * 1 cause high-degree vertices to move less than low-degree
			 * vertices, and values > 1 cause high-degree vertices to move more
			 * than low-degree vertices. Negative values will have unpredictable
			 * and inconsistent results.
			 */
			// double stretch = dagLayout.getStretch();
			layout = dagLayout;
		} else if (GraphLayoutEnum.ISOMLayout.toString().equals(sLayout)) {
			layout = new ISOMLayout<CallGraphNode, CallGraphLink>(graph);
		} else if (GraphLayoutEnum.KKLayout.toString().equals(sLayout)) {
			layout = new KKLayout<CallGraphNode, CallGraphLink>(graph);
		} else if (GraphLayoutEnum.SpringLayout.toString().equals(sLayout)) {
			layout = new SpringLayout<CallGraphNode, CallGraphLink>(graph);
		} else if (GraphLayoutEnum.SpringLayout2.toString().equals(sLayout)) {
			layout = new SpringLayout2<CallGraphNode, CallGraphLink>(graph);
		} else {
			layout = new FRLayout<CallGraphNode, CallGraphLink>(graph);
		}
		return layout;
	}
    
	protected void determineDirectedness() {
		if (graph.getDefaultEdgeType().equals(EdgeType.DIRECTED)) {
			directedGraph = graph;
		} else {
			undirectedGraph = graph;
		}
	}
	
    /**
     * Display the call graph, taking into account the various parameters
     * that might affect the display.  NOTE: this method may physically
     * alter the graph to affect the display.
     * @param callGraph
     */
	public void loadGraph(JavaCallGraph callGraph)
    {
        graph = callGraph;
    	ApplicationParameters parameters = app.getApplicationParameters();

        // TODO be smarter about when to create new graphs vs. relayout
    	try {
    		graph = callGraph.getAltGraphUsingParams();
		} catch (Exception e) {
			String msg =
				"Unable to modify graph using parameters: " + e.getMessage();
			JOptionPane.showMessageDialog(graphPanel, msg,
					"Error showing graph", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}

        determineDirectedness();
		doLayout = true;
		String sLayout = parameters.getParameter(GRAPH_LAYOUT_KEY,
				GraphLayoutEnum.FRLayout.toString());
		layoutGraph(sLayout);
    	graphLabel.setText(graph.getName());
        mainPanel.validate();
        mainPanel.repaint();
    }


	private void updateVisViewerRenderContext(
			Graph<CallGraphNode, CallGraphLink> jungGraph) {
        setUpVertexLabeling(visViewer);
        makeMousePickable();
		RenderContext<CallGraphNode, CallGraphLink> renderContext =
            visViewer.getRenderContext();
        setEdgeLabeler(renderContext);
        updateVertexShapeTransformer(jungGraph);
        renderContext.setVertexShapeTransformer(vertexShapeTransformer);
        renderContext.setVertexFillPaintTransformer(vertexPaintTransformer);
        renderContext.setVertexStrokeTransformer(new NodeStrokeTransformer());
        renderContext.getMultiLayerTransformer().setToIdentity();
	}


	private void updateVertexShapeTransformer(
			Graph<CallGraphNode, CallGraphLink> jungGraph) {
		String scoreToScaleOn = null;
        if (vertexShapeTransformer != null) {
        	scoreToScaleOn = vertexShapeTransformer.getScoreToScaleOn();
        }
    	vertexShapeTransformer = new CallGraphNodeShapeTransformer(jungGraph);
    	
    	if (scoreToScaleOn != null) {
    		vertexShapeTransformer.setScoreToScaleOn(scoreToScaleOn);
    	}
	}

    /**
     * Redisplays the graph based on the user's menu selections.
     */
	@SuppressWarnings("rawtypes")
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof JComboBox) {
			final JComboBox box = (JComboBox) source;
			final String sourceName = box.getName();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						mainPanel.setCursor(RefactoringConstants.WAIT_CURSOR);
						if (EDGE_TYPE_COMBO.equals(sourceName)) {
							handleEdgeTypeRequest(box);
						} else if (LAYOUT_COMBO.equals(sourceName)) {
							handleLayoutRequest(box);
						} else if (SIZING_COMBO.equals(sourceName)) {
							handleSizingRequest(box);
						}
					} finally {
						mainPanel.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				}
			}); // invokeLater

		} // if
	} // actionPerformed

    public void itemStateChanged(ItemEvent event) {
        Object source = event.getItemSelectable();
    	ApplicationParameters parameters = app.getApplicationParameters();

		if (source == includeConstructorsButton) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				parameters.setParameter(INCLUDE_CONSTRUCTORS_KEY, TRUE);
			} else {
				parameters.setParameter(INCLUDE_CONSTRUCTORS_KEY, FALSE);
			}
        } else if (source == includeStaticsButton) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				parameters.setParameter(INCLUDE_STATICS_KEY, TRUE);
			} else {
				parameters.setParameter(INCLUDE_STATICS_KEY, FALSE);
			}
        } else if (source == includeObjectMethodsButton) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				parameters.setParameter(INCLUDE_OBJECT_METHODS_KEY, TRUE);
			} else {
				parameters.setParameter(INCLUDE_OBJECT_METHODS_KEY, FALSE);
			}
        } else if (source == includeLoggerButton) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				parameters.setParameter(INCLUDE_LOGGERS_KEY, TRUE);
			} else {
				parameters.setParameter(INCLUDE_LOGGERS_KEY, FALSE);
			}
        } else if (source == condenseCyclesButton) {
			JOptionPane.showMessageDialog(graphPanel,
					"Recursive cycles not yet implemented",
					"Not yet implemented", JOptionPane.WARNING_MESSAGE);
        } else if (source == includeInheritedButton) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				parameters.setParameter(INCLUDE_INHERITED_KEY, TRUE);
			} else {
				parameters.setParameter(INCLUDE_INHERITED_KEY, FALSE);
			}
        } else if (source == includeInnersButton) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				parameters.setParameter(INCLUDE_INNERS_KEY, TRUE);
			} else {
				parameters.setParameter(INCLUDE_INNERS_KEY, FALSE);
			}
		} else if (source == condenseRequiredMethodsButton) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				condenseObjectsMethodsButton.setVisible(true);
	    		parameters.setParameter(CONDENSE_IMPOSED_METHODS_KEY, TRUE);
			}	// if SELECTED
			else if (event.getStateChange() == ItemEvent.DESELECTED) {
	    		parameters.setParameter(CONDENSE_IMPOSED_METHODS_KEY, FALSE);
				condenseObjectsMethodsButton.setSelected(false);
				condenseObjectsMethodsButton.setVisible(false);
	    		parameters.setParameter(CONDENSE_OBJECTS_METHODS_KEY, FALSE);
			}	// if deselected
        } else if (source == condenseObjectsMethodsButton) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
	    		parameters.setParameter(CONDENSE_OBJECTS_METHODS_KEY, TRUE);
			}	// if SELECTED
			else if (event.getStateChange() == ItemEvent.DESELECTED) {
	    		parameters.setParameter(CONDENSE_OBJECTS_METHODS_KEY, FALSE);
			}	// if deselected
        }
		loadGraph(graph);
    }


    @SuppressWarnings("rawtypes")
	private void handleSizingRequest(JComboBox box) {
		Object selectedItem = box.getSelectedItem();
		String selected = selectedItem.toString();
		ApplicationParameters parameters = app.getApplicationParameters();
		parameters.setParameter(ParameterConstants.NODE_SIZING_KEY, selected);
		vertexShapeTransformer =
			new CallGraphNodeShapeTransformer(graph.getJungGraph());
		vertexShapeTransformer.setScoreToScaleOn(selected);
		loadGraph(graph);
	}


	@SuppressWarnings("rawtypes")
	private void handleEdgeTypeRequest(JComboBox box) {
		Object selectedItem = box.getSelectedItem();
		String selected = selectedItem.toString();
		ApplicationParameters parameters = app.getApplicationParameters();
		parameters.setParameter(ParameterConstants.EDGE_TYPE_KEY, selected);
		
		if (selected.equals(EdgeType.UNDIRECTED.toString())) {
			// If an undirected version of this graph is not available,
			// make one and load it.
			if ((undirectedGraph == null)
					|| !graph.getName().equals(undirectedGraph.getName())) {
				undirectedGraph =
					JavaCallGraph.toUndirectedGraph(graph);
			}
			loadGraph(undirectedGraph);
		}
		// If we're currently looking at an undirected graph, switch to
		// the original directed version, if one is available.
		// Otherwise, pop up a bad luck message to the user.
		else {
			if (directedGraph == null) {
				String msg = "Directed graph unavailable.";
				JOptionPane.showMessageDialog(app.frame, msg,
						"Change unsuccessful", JOptionPane.INFORMATION_MESSAGE);
			} else {
				loadGraph(directedGraph);
			}
		}
	}

	/**
	 * Get the layout corresponding to item in the combo box.
	 * @param box contains the list of possible layouts
	 * @return the Layout object
	 */
	@SuppressWarnings("rawtypes")
	private Layout<CallGraphNode, CallGraphLink> handleLayoutRequest(JComboBox box) {
		doLayout = true;
		Object selectedItem = box.getSelectedItem();
		String layoutString = selectedItem.toString();
		ApplicationParameters parameters = app.getApplicationParameters();
		parameters.setParameter(ParameterConstants.GRAPH_LAYOUT_KEY, layoutString);
        Layout<CallGraphNode, CallGraphLink> endLayout = layoutGraph(layoutString);
        return endLayout;
	}

	/**
	 * Show the graph according to the specified layout
	 * @param sLayout the layout to use
	 * @return the Layout object used
	 */
	protected Layout<CallGraphNode, CallGraphLink> layoutGraph(
			String sLayout) {
        Graph<CallGraphNode, CallGraphLink> jungGraph = graph.getJungGraph();
        // The layouts correspond with the Jung graphs
		Layout<CallGraphNode, CallGraphLink> startLayout = null;
        boolean createNewStartLayout = loadingNewGraph(jungGraph);
		
		if (createNewStartLayout) {
			startLayout = getLayout(sLayout, jungGraph);
//        	startLayout = new FRLayout<CallGraphNode, CallGraphLink>(jungGraph);
        } else { // This should only happen when redrawing an existing graph
        	startLayout = visViewer.getGraphLayout();
        }
        Layout<CallGraphNode, CallGraphLink> endLayout = startLayout;
		
        // Only change the layout if a change is warranted
        if (doLayout || createNewStartLayout) {
        	graphPanel.removeAll();
        	endLayout = getEndLayout(sLayout, jungGraph);
            // The visViewer is a JPanel
	        visViewer =
	        	new VisualizationViewer<CallGraphNode, CallGraphLink>(endLayout);
	        updateVisViewerRenderContext(jungGraph);
	        animateLayoutTransition(startLayout, endLayout);
    		addGraphToGraphPanel();
            visViewer.getRenderContext().getMultiLayerTransformer().setToIdentity();
            mainPanel.validate();
            mainPanel.repaint();
        }
        doLayout = false;
		return endLayout;
	}

	private void addGraphToGraphPanel() {
		Dimension viewportSize = new Dimension(1000, 600);
		JScrollPane scrollPane = new JScrollPane(visViewer);
		scrollPane.setPreferredSize(viewportSize);
		graphPanel.add(scrollPane);
	}

	private void animateLayoutTransition(
			Layout<CallGraphNode, CallGraphLink> startLayout,
			Layout<CallGraphNode, CallGraphLink> endLayout) {
		endLayout.setInitializer(startLayout);
		endLayout.setSize(visViewer.getSize());
		LayoutTransition<CallGraphNode, CallGraphLink> transition =
		    new LayoutTransition<CallGraphNode, CallGraphLink>(
		    		visViewer, startLayout, endLayout);
		Animator animator = new Animator(transition);
		animator.start();
	}


	/**
	 * Creates the specified layout for the graph.
	 * @param sLayout the requested layout
	 * @param jungGraph the graph to lay out
	 * @return the requested Layout
	 */
	private Layout<CallGraphNode, CallGraphLink> getEndLayout(String sLayout,
			Graph<CallGraphNode, CallGraphLink> jungGraph) {
		Layout<CallGraphNode, CallGraphLink> endLayout = null;
		
		// Before we create a DAGLayout, we must ensure the graph has no cycles.
		if (GraphLayoutEnum.DAGLayout.toString().equals(sLayout)) {
			JavaCallGraph dag = GraphCondenser.toDAG(graph);
		    endLayout = getLayout(sLayout, dag.getJungGraph());
		}
		else {
		    endLayout = getLayout(sLayout, jungGraph);
		}
		return endLayout;
	}


	/**
	 * Determines whether the graph to be displayed is a new one.  A class's
	 * call graph can be represented by several different graphs depending on
	 * the graphing options chosen, so a change in an option can result in
	 * a new graph.
	 * @param jungGraph new graph
	 * @return true when a new graph is to be displayed; false otherwise
	 */
	private boolean loadingNewGraph(
			Graph<CallGraphNode, CallGraphLink> jungGraph) {
		boolean createNewStartLayout = true;
        
		if (visViewer != null) {
			Layout<CallGraphNode, CallGraphLink> oldLayout =
				visViewer.getGraphLayout();
			Graph<CallGraphNode, CallGraphLink> oldJungGraph =
				oldLayout.getGraph();
			createNewStartLayout = !jungGraph.equals(oldJungGraph);
		}
		return createNewStartLayout;
	}

}
