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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.cluster.ClustererIfc;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class SpanningForestApplet extends JApplet
implements ClusterUIConstants, ParameterConstants
{
	private static final long serialVersionUID = 7065366231144981930L;

	public static final String CALCULATOR_COMBO = "DistanceCombo";

	protected static final String FILTER_COMMAND = "filter";
	
    protected static final UtilLogger logger =
    	new UtilLogger("ClusteringGraphApplet");
	
	/** The view of which this applet is a part. */
	protected SpanningForestView view = null;

	/** The clusterer that works on the graph. */
	protected ClustererIfc<String> clusterer = null;
	
	/** The graph to display. */
	protected JavaCallGraph graph = null;
	
	protected ApplicationParameters parameters = null;

	protected VisualizationViewer<String, CallGraphLink> visualizer;
	
	protected JPanel sliderPanel = null;
	
	protected JSlider iterationSlider = null;
	
	protected String sliderLabelBase = "Iteration: ";

	/** A text area in another component to which this writes. */
	//TODO avoid aliasing problems
	protected JTextArea clustersTextArea = null;


	public SpanningForestApplet() throws HeadlessException {
		super();
		parameters = ApplicationParameters.getSingleton();
	}

	public void start() {
	}

	/**
	 * @param clustersTextArea the clustersTextArea to set
	 */
	public void setClustersTextArea(JTextArea clustersTextArea) {
		this.clustersTextArea = clustersTextArea;
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
//        int sliderLimit = jungGraph.getVertexCount();
//		configureIterationSlider(sliderLimit);
		sliderPanel.setOpaque(true);
		sliderPanel.add(iterationSlider);
		iterationSlider.setValue(0);
		String eastSize = sliderLabelBase + iterationSlider.getValue();
		TitledBorder sliderBorder = BorderFactory.createTitledBorder(eastSize);
		sliderPanel.setBorder(sliderBorder);
		return sliderBorder;
	}

	public void setUpView(final Forest<String,CallGraphLink> forest)
	throws IOException {
		Container content = getContentPane();
		content.removeAll();
		clustersTextArea.setText("");
		
		if (forest != null) {
			VisualizationViewer<String, CallGraphLink> forestViewer =
				setUpForestVisualizerAndLayout(forest);
			// recordPositions(jungGraph);
	
			content.add(new GraphZoomScrollPane(forestViewer));
		}
//		JPanel southPanel = setUpSouthPanel();
//		content.add(southPanel, BorderLayout.SOUTH);
//		displayGraphClusterText();
		content.validate();
		content.repaint();
	}

	protected VisualizationViewer<String, CallGraphLink> setUpForestVisualizerAndLayout(
			final Forest<String, CallGraphLink> forest) {
		Layout<String, CallGraphLink> forestLayout = new TreeLayout<String, CallGraphLink>(
				forest);
		VisualizationViewer<String, CallGraphLink> forestViewer = new VisualizationViewer<String, CallGraphLink>(
				forestLayout);

		RenderContext<String, CallGraphLink> renderContext = forestViewer
				.getRenderContext();
		renderContext.setVertexLabelTransformer(new ToStringLabeller<String>());
		Renderer<String, CallGraphLink> renderer = forestViewer.getRenderer();
		BasicVertexLabelRenderer<String, CallGraphLink> labelRenderer = new BasicVertexLabelRenderer<String, CallGraphLink>();
		labelRenderer.setPosition(Renderer.VertexLabel.Position.S);
		renderer.setVertexLabelRenderer(labelRenderer);

		ToStringLabeller<CallGraphLink> toStringLabeller = new ToStringLabeller<CallGraphLink>();
		renderContext.setEdgeLabelTransformer(toStringLabeller);
		int iColor = parameters.getIntParameter(CONNECTED_EDGE_COLOR_KEY, 0);
		Color ceColor = new Color(iColor);
		DefaultEdgeLabelRenderer edgeLabeler = new DefaultEdgeLabelRenderer(
				ceColor);
		edgeLabeler.setRotateEdgeLabels(false);
		renderContext.setEdgeLabelRenderer(edgeLabeler);

		// setUpMouseMode();
		DefaultModalGraphMouse<?, ?> graphMouse = new DefaultModalGraphMouse<Object, Object>();
		graphMouse.setMode(Mode.PICKING);
		forestViewer.setGraphMouse(graphMouse);
		return forestViewer;
	}

    protected void setUpMouseMode()
    {
        DefaultModalGraphMouse<?, ?> graphMouse =
            new DefaultModalGraphMouse<Object, Object>();
        graphMouse.setMode(Mode.PICKING);
        visualizer.setGraphMouse(graphMouse);
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
	public void setClusterer(ClustererIfc<String> clusterer2) {
		this.clusterer = clusterer2;
	}

	/**
	 * @return the visualizer
	 */
	public VisualizationViewer<String, CallGraphLink> getVisualizer() {
		return visualizer;
	}

	/**
	 * @return the iterationSlider
	 */
	public JSlider getIterationSlider() {
		return iterationSlider;
	}

	public void setView(SpanningForestView clusteringView) {
		view = clusteringView;
	}


}