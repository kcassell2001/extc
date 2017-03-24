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

package nz.ac.vuw.ecs.kcassell.callgraph.gui.transformers;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.NodeType;
import nz.ac.vuw.ecs.kcassell.callgraph.ScoreType;
import nz.ac.vuw.ecs.kcassell.callgraph.algorithm.HITSScorer;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.samples.PluggableRendererDemo;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;

/**
 * Controls the shape, size, and aspect ratio for each vertex.
 * 
 * @author Keith Cassell
 * @see PluggableRendererDemo.VertexShapeSizeAspect
 */
public class CallGraphNodeShapeTransformer extends
		AbstractVertexShapeTransformer<CallGraphNode> implements
		Transformer<CallGraphNode, Shape> {
	private final class NodeSizeMetricTransformer implements
			Transformer<CallGraphNode, Integer> {
		/**
		 * Returns the size of the node in number of pixels based on the node
		 * score
		 * 
		 * @param node
		 *            the node to draw
		 * @return number of pixels in diameter
		 */
		public Integer transform(CallGraphNode node) {
			int pixels = MIN_DIAMETER;
			if (node.getNodeType() == NodeType.CLUSTER) {
				pixels = MAX_DIAMETER;
			}
			else if (node.getNodeType() == NodeType.METHOD) {
				
				// Alter the scaling based on the metric chosen
				if (ScoreType.AUTHORITY.equals(scoreToScaleOn)) {
					pixels = computeHITSSize(node, scoreToScaleOn);
				}
				else if (ScoreType.HUB.equals(scoreToScaleOn)) {
					pixels = computeHITSSize(node, scoreToScaleOn);
				}
				else if (ScoreType.MCCABE.equals(scoreToScaleOn)) {
					Number score = node.getScore(ScoreType.MCCABE);
					if (score != null) {
						pixels = (int) (10 * Math.log1p(score.doubleValue() + 1.0));
					}
				}
				else if (ScoreType.NESTEDBLOCKDEPTH.equals(scoreToScaleOn)) {
					Number score = node.getScore(ScoreType.MCCABE);
					if (score != null) {
						pixels = 10 * score.intValue();
					}
				}
				else if (ScoreType.INDEGREE.equals(scoreToScaleOn)) {
					pixels = 5 * getNodeInDegree(node);
				}
				else if (ScoreType.OUTDEGREE.equals(scoreToScaleOn)) {
					pixels = 5 * graph.outDegree(node);
				}
			} // if a Method
			else {  // Field
				pixels = FIELD_DIAMETER;
				if (ScoreType.AUTHORITY.equals(scoreToScaleOn)) {
					pixels = computeHITSSize(node, scoreToScaleOn);
				}
				else if (ScoreType.HUB.equals(scoreToScaleOn)) {
					pixels = computeHITSSize(node, scoreToScaleOn);
				}
				else if (ScoreType.INDEGREE.equals(scoreToScaleOn)) {
					pixels = 5 * getNodeInDegree(node);
				}
			}
			pixels = Math.min(MAX_DIAMETER, pixels);
			return Math.max(MIN_DIAMETER, pixels);
		}

		private int computeHITSSize(CallGraphNode node, String scoreType) {
			int pixels = MIN_DIAMETER;
			Number score = node.getScore(scoreType);
			if (score != null) {
				pixels = (int) (MIN_DIAMETER * Math.sqrt(score.doubleValue()/aveHITSScore));
			}
			return pixels;
		}

		private int getNodeInDegree(CallGraphNode node) {
			int inDegree = 0;
			try {
				inDegree = graph.inDegree(node);
			}
			catch (Exception e) {
				System.err.println("CallGraphNodeShapeTransformer$NodeSizeMetricTransformer.getNodeInDegree Exception: " + e);
				/*
				java.lang.NullPointerException
CallGraphNodeShapeTransformer$NodeSizeMetricTransformer.getNodeInDegree(CallGraphNode) line: 97	
CallGraphNodeShapeTransformer$NodeSizeMetricTransformer.transform(CallGraphNode) line: 60	
CallGraphNodeShapeTransformer$NodeSizeMetricTransformer.transform(Object) line: 1	
VertexShapeFactory<V>.getRectangle(V) line: 69	
VertexShapeFactory<V>.getEllipse(V) line: 85	
CallGraphNodeShapeTransformer.transform(CallGraphNode) line: 180	
CallGraphNodeShapeTransformer.transform(Object) line: 1	
BasicEdgeRenderer<V,E>.drawSimpleEdge(RenderContext<V,E>, Layout<V,E>, E) line: 95	
BasicEdgeRenderer<V,E>.paintEdge(RenderContext<V,E>, Layout<V,E>, E) line: 61	
BasicRenderer<V,E>.renderEdge(RenderContext<V,E>, Layout<V,E>, E) line: 79	
BasicRenderer<V,E>.render(RenderContext<V,E>, Layout<V,E>) line: 39	
VisualizationViewer<V,E>(BasicVisualizationServer<V,E>).renderGraph(Graphics2D) line: 367	
						 */
			}
			return inDegree;
		}
	}

	static final int FIELD_DIAMETER = 20;
	static final int MAX_DIAMETER = 45;
	static final int MIN_DIAMETER = 15;

	protected boolean distinguishFieldsMethods = true;
	
	/** The average score for a node as computed by the HITS algorithm. */
	protected double aveHITSScore = 0.15;
	
	/** The score that will affect the size of the nodes. */
	protected String scoreToScaleOn = ScoreType.INDEGREE;
	
	protected boolean shapeOnDegree = true;
	
	protected Transformer<CallGraphNode, Integer> scoreTransformer =
		new NodeSizeMetricTransformer();

	protected Graph<CallGraphNode, CallGraphLink> graph;

	// protected AffineTransform scaleTransform = new AffineTransform();

	public CallGraphNodeShapeTransformer(
			Graph<CallGraphNode, CallGraphLink> graphIn) {
		this.graph = graphIn;

		setSizeTransformer(scoreTransformer);
		setAspectRatioTransformer(new Transformer<CallGraphNode, Float>() {

			public Float transform(CallGraphNode v) {
//				if (stretch) {
//					return (float) (graph.inDegree(v) + 1)
//							/ (graph.outDegree(v) + 1);
//				} else {
					return 1.0f;
//				}
			}
		}); // setAspectRatioTransformer

	} // constructor

	/**
	 * @return the scoreToScaleOn
	 */
	public String getScoreToScaleOn() {
		return scoreToScaleOn;
	}

	/**
	 * @param scoreToScaleOn the scoreToScaleOn to set
	 */
	public void setScoreToScaleOn(String scoreToScaleOn) {
		this.scoreToScaleOn = scoreToScaleOn;
		if (ScoreType.AUTHORITY.equals(scoreToScaleOn)
				|| ScoreType.HUB.equals(scoreToScaleOn)) {
			HITSScorer scorer = new HITSScorer();
			scorer.assignHITSScores(graph);
			aveHITSScore = Math.sqrt(1.0/graph.getVertexCount());
		}
	}

	public void setShapeOnDegree(boolean use) {
		this.shapeOnDegree = use;
	}

	public Shape transform(CallGraphNode node) {
		Shape shape = factory.getEllipse(node);
		int numClusterSides = 3;
		if (distinguishFieldsMethods) {
			if (node.getNodeType() == NodeType.FIELD) {
				shape = factory.getRegularStar(node, 5);
			} else if (node instanceof CallGraphCluster) {
				CallGraphCluster cluster = (CallGraphCluster)node;
				int nodeCount = cluster.getElementCount();
				if (nodeCount == 2) {
					Ellipse2D ellipse = factory.getEllipse(cluster);
					shape = new Ellipse2D.Double(ellipse.getX(), ellipse.getY(),
							                     ellipse.getWidth(), ellipse.getHeight() * 0.6);
				} else {
					shape = factory.getRegularPolygon(node,
							(nodeCount > numClusterSides) ? nodeCount : numClusterSides);
				}
			} else if (node.getNodeType() == NodeType.CLUSTER) {
				shape = factory.getRegularPolygon(node, numClusterSides);
			} else { // METHOD
				shape = factory.getEllipse(node);
//				shape = factory.getRegularPolygon(node, 4);
			}
		} else if (shapeOnDegree) {
			if (graph.degree(node) < 5) {
				int sides = Math.max(graph.degree(node), 3);
				shape = factory.getRegularPolygon(node, sides);
			} else {
				shape = factory.getRegularStar(node, graph.degree(node));
			}
		}
		return shape;
	}
}
