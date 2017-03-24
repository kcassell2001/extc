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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.NodeNameComparator;
import nz.ac.vuw.ecs.kcassell.callgraph.gui.transformers.CallGraphNodeShapeTransformer;
import nz.ac.vuw.ecs.kcassell.callgraph.gui.transformers.NodeTypeColorer;
import nz.ac.vuw.ecs.kcassell.cluster.ClusterIfc;
import nz.ac.vuw.ecs.kcassell.cluster.ClusterTextFormatEnum;
import nz.ac.vuw.ecs.kcassell.cluster.ClustererIfc;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public abstract class ClusteringGraphApplet extends JApplet
implements ClusterUIConstants, ParameterConstants
{
	private static final long serialVersionUID = 7065366231144981930L;

	/** The view of which this applet is a part. */
	protected ClusteringView view = null;

	protected static final String FILTER_COMMAND = "filter";
	
    protected static NodeNameComparator nodeNameComparator =
    	new NodeNameComparator();

    protected static final UtilLogger logger =
    	new UtilLogger("ClusteringGraphApplet");
	
	protected Paint CONNECTED_EDGE_COLOR = Color.BLACK;
	protected Paint DELETED_EDGE_COLOR = Color.GRAY;
	protected Paint PICKED_COLOR = Color.CYAN;
	protected Paint UNPICKED_COLOR = Color.BLACK;

	/** The clusterer that works on the graph. */
	protected ClustererIfc<CallGraphNode> clusterer = null;
	
	/** The graph to display. */
	protected JavaCallGraph graph = null;
	
	protected ApplicationParameters parameters = null;

	protected VisualizationViewer<CallGraphNode, CallGraphLink> visualizer;
	
	protected JPanel sliderPanel = null;
	
	protected JSlider iterationSlider = null;
	
	protected Object[] membersToIgnore = null;

	protected String sliderLabelBase = "Iteration: ";

	/** A text area in another component to which this writes. */
	//TODO avoid aliasing problems
	protected JTextArea clustersTextArea = null;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<CallGraphNode, Paint> vertexPaints =
		LazyMap.<CallGraphNode, Paint> decorate(
	            new HashMap<CallGraphNode, Paint>(),
	            new ConstantTransformer(Color.white));
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<CallGraphLink, Paint> edgePaints =
		LazyMap.<CallGraphLink, Paint> decorate(
	            new HashMap<CallGraphLink, Paint>(),
	            new ConstantTransformer(Color.blue));


	public ClusteringGraphApplet() throws HeadlessException {
		super();
		parameters = ApplicationParameters.getSingleton();
		int iColor =
			parameters.getIntParameter(CONNECTED_EDGE_COLOR_KEY, BLACK_RGB);
		CONNECTED_EDGE_COLOR = new Color(iColor);
		iColor =
			parameters.getIntParameter(DELETED_EDGE_COLOR_KEY, GREY_RGB);
		DELETED_EDGE_COLOR = new Color(iColor);
		iColor = parameters.getIntParameter(PICKED_COLOR_KEY, CYAN_RGB);
		PICKED_COLOR = new Color(iColor);
		iColor = parameters.getIntParameter(UNPICKED_COLOR_KEY, BLACK_RGB);
		UNPICKED_COLOR = new Color(iColor);
	}

	public void start() {
	}

	/**
	 * @param clustersTextArea the clustersTextArea to set
	 */
	public void setClustersTextArea(JTextArea clustersTextArea) {
		this.clustersTextArea = clustersTextArea;
	}

	protected void setRenderContextTransformers() {
	    RenderContext<CallGraphNode, CallGraphLink> renderContext =
	        visualizer.getRenderContext();
	    setRenderContextVertexTransformers(renderContext);
	    setRenderContextEdgeTransformers(renderContext);
	}

	protected void setRenderContextEdgeTransformers(RenderContext<CallGraphNode,
			CallGraphLink> renderContext) {
	    setEdgeLabeler(renderContext);
	    renderContext.setEdgeDrawPaintTransformer(MapTransformer
	            .<CallGraphLink, Paint> getInstance(edgePaints));
	    renderContext
	            .setEdgeStrokeTransformer(new Transformer<CallGraphLink, Stroke>()
	            {
	                protected final Stroke THIN = new BasicStroke(1);
	                protected final Stroke THICK = new BasicStroke(2);
	
	                public Stroke transform(CallGraphLink e)
	                {
	                    Paint c = edgePaints.get(e);
	                    if (c == Color.LIGHT_GRAY)
	                        return THIN;
	                    else
	                        return THICK;
	                }
	            });
	}

	protected void setRenderContextVertexTransformers(RenderContext<CallGraphNode, CallGraphLink> renderContext) {
	    setVertexLabeler(renderContext);
	    renderContext.setVertexFillPaintTransformer(
	            new NodeTypeColorer());
//	    		MapTransformer.<CallGraphNode, Paint> getInstance(vertexPaints));
	    setVertexDrawPaintTransformer(renderContext);
	    setVertexShapeTransformer(renderContext);
	}

	protected void setVertexDrawPaintTransformer(RenderContext<CallGraphNode, CallGraphLink> renderContext) {
	    renderContext.setVertexDrawPaintTransformer(new Transformer<CallGraphNode, Paint>()
	            {
	                public Paint transform(CallGraphNode node)
	                {
	                    PickedState<CallGraphNode> pickedState = visualizer
	                            .getPickedVertexState();
	                    if (pickedState.isPicked(node))
	                    {
	                        return PICKED_COLOR;
	                    }
	                    else
	                    {
	                        return UNPICKED_COLOR;
	                    }
	                }
	            });
	}

	protected void setVertexShapeTransformer(RenderContext<CallGraphNode, CallGraphLink> renderContext) {
	    CallGraphNodeShapeTransformer vertexShapeTransformer =
	        new CallGraphNodeShapeTransformer(graph.getJungGraph());
	    renderContext.setVertexShapeTransformer(vertexShapeTransformer);
	}

	protected void setVertexLabeler(RenderContext<CallGraphNode, CallGraphLink> renderContext) {
	    renderContext.setVertexLabelTransformer(new ToStringLabeller<CallGraphNode>());
	    Renderer<CallGraphNode, CallGraphLink> renderer =
	        visualizer.getRenderer();
	    BasicVertexLabelRenderer<CallGraphNode, CallGraphLink> labelRenderer =
	        new BasicVertexLabelRenderer<CallGraphNode, CallGraphLink>();
	    labelRenderer.setPosition(Renderer.VertexLabel.Position.CNTR);
	    renderer.setVertexLabelRenderer(labelRenderer);
	}

	protected void setEdgeLabeler(
			RenderContext<CallGraphNode, CallGraphLink> renderContext) {
	    ToStringLabeller<CallGraphLink> toStringLabeller =
	    	new ToStringLabeller<CallGraphLink>();
		renderContext.setEdgeLabelTransformer(toStringLabeller);
	    Renderer<CallGraphNode, CallGraphLink> renderer =
	        visualizer.getRenderer();
	    BasicEdgeLabelRenderer<CallGraphNode, CallGraphLink> labelRenderer =
	        new BasicEdgeLabelRenderer<CallGraphNode, CallGraphLink>();
	    //labelRenderer.setPosition(Renderer.EdgeLabel.Position.CNTR);
	    renderer.setEdgeLabelRenderer(labelRenderer);
	}

    protected JSlider configureIterationSlider(int sliderLimit)
    {
    	iterationSlider = new JSlider(JSlider.HORIZONTAL);
        iterationSlider.setBackground(Color.WHITE);
        iterationSlider.setPreferredSize(new Dimension(210, 50));
        iterationSlider.setPaintTicks(true);
		iterationSlider.setMaximum(sliderLimit);
        iterationSlider.setMinimum(0);
        iterationSlider.setValue(0);
        iterationSlider.setMajorTickSpacing(sliderLimit / 5);
        iterationSlider.setPaintLabels(true);
        iterationSlider.setPaintTicks(true);
        return iterationSlider;
    }
    
	protected TitledBorder setUpSliderPanel() {
		sliderPanel = new JPanel();
        sliderPanel.setPreferredSize(new Dimension(210, 50));
        int sliderLimit = 50;
        
        if (graph != null) {
        	final Graph<CallGraphNode, CallGraphLink> jungGraph =
        		graph.getJungGraph();
            sliderLimit = jungGraph.getVertexCount();
        }
		configureIterationSlider(sliderLimit);
		sliderPanel.setOpaque(true);
		sliderPanel.add(iterationSlider);
		iterationSlider.setValue(0);
		String eastSize = sliderLabelBase + iterationSlider.getValue();
		TitledBorder sliderBorder = BorderFactory.createTitledBorder(eastSize);
		sliderPanel.setBorder(sliderBorder);
		return sliderBorder;
	}

	public abstract void setUpView(final JavaCallGraph callGraph) throws IOException;

    protected void setUpMouseMode()
    {
    	if (visualizer != null) {
    		DefaultModalGraphMouse<?, ?> graphMouse =
    			new DefaultModalGraphMouse<Object, Object>();
    		graphMouse.setMode(Mode.PICKING);
    		visualizer.setGraphMouse(graphMouse);
    	}
    }

	protected void colorCluster(ClusterIfc<CallGraphNode> cluster, Color color) {
		Set<CallGraphNode> vertices = cluster.getElements();

		for (CallGraphNode vertex : vertices) {
			vertexPaints.put(vertex, color);
		}
	}

	/**
	 * @return the graph
	 */
	public JavaCallGraph getGraph() {
		return graph;
	}

	/**
	 * @return the clusterer
	 */
	public ClustererIfc<?> getClusterer() {
		return clusterer;
	}

	/**
	 * @param clusterer2 the clusterer to set
	 */
	public void setClusterer(ClustererIfc<CallGraphNode> clusterer2) {
		this.clusterer = clusterer2;
	}

	/**
	 * @return the visualizer
	 */
	public VisualizationViewer<CallGraphNode, CallGraphLink> getVisualizer() {
		return visualizer;
	}

	/**
	 * @return the iterationSlider
	 */
	public JSlider getIterationSlider() {
		return iterationSlider;
	}

	protected String groupsToString(StringBuffer buf,
			Collection<CallGraphNode> clusters) {
		int groupNumber = 1;
		String clusterFormat =
			 parameters.getParameter(
							ParameterConstants.CLUSTER_TEXT_FORMAT_KEY,
							ClusterTextFormatEnum.NEWICK.toString());
		ClusterTextFormatEnum textFormatEnum =
			ClusterTextFormatEnum.valueOf(clusterFormat);
		
		for (CallGraphNode node : clusters) {
			buf.append("Group ").append(groupNumber).append(": ");
			buf.append(node.getSimpleName()).append(":\n");
			if (node instanceof CallGraphCluster) {
				CallGraphCluster cluster = (CallGraphCluster)node;
				
				if (ClusterTextFormatEnum.FLAT.equals(textFormatEnum)) {
					clusterToFlatGroupString(buf, cluster);
				} else {
					cluster.toNestedString(1, buf);
				}
			} else { // Regular CallGraphNode
				buf.append("  ").append(node.getSimpleName()).append("\n");
			}
			groupNumber++;
		}
		String clustersString = buf.toString();
		return clustersString;
	}

	protected void clusterToFlatGroupString(StringBuffer buf,
			CallGraphCluster cluster) {
		SortedSet<CallGraphNode> subnodes = cluster.getElements();
		ArrayList<CallGraphNode> subNodeList =
			new ArrayList<CallGraphNode>(subnodes);
		Collections.sort(subNodeList, nodeNameComparator);
		
		for (CallGraphNode subnode : subNodeList) {
			buf.append("  ").append(subnode.getSimpleName()).append("\n");
		}
	}

	public void setView(ClusteringView clusteringView) {
		view = clusteringView;
	}


}