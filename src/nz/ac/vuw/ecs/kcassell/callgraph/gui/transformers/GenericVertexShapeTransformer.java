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

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.samples.PluggableRendererDemo;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;

/**
 * Controls the shape, size, and aspect ratio for each vertex.
 * 
 * @author Joshua O'Madadhain
 * @see PluggableRendererDemo.VertexShapeSizeAspect
 */
public class GenericVertexShapeTransformer<V, E> extends
		AbstractVertexShapeTransformer<V> implements Transformer<V, Shape> {

	protected boolean stretch = false;
	protected boolean scale = true;
	protected boolean funny_shapes = true;
	protected Transformer<V, Double> scoreTransformer;
	protected Graph<V, E> graph;

	// protected AffineTransform scaleTransform = new AffineTransform();

	public GenericVertexShapeTransformer(Graph<V, E> graphIn,
			Transformer<V, Double> scoreTransformerIn) {
		this.graph = graphIn;
		this.scoreTransformer = scoreTransformerIn;

		setSizeTransformer(new Transformer<V, Integer>() {

			public Integer transform(V v) {
				if (scale)
					return (int) (scoreTransformer.transform(v) * 30) + 20;
				else
					return 20;

			}
		}); // setSizeTransformer

		setAspectRatioTransformer(new Transformer<V, Float>() {

			public Float transform(V v) {
				if (stretch) {
					return (float) (graph.inDegree(v) + 1)
							/ (graph.outDegree(v) + 1);
				} else {
					return 1.0f;
				}
			}
		}); // setAspectRatioTransformer

	} // constructor

	public void setStretching(boolean stretch) {
		this.stretch = stretch;
	}

	public void setScaling(boolean scale) {
		this.scale = scale;
	}

	public void useFunnyShapes(boolean use) {
		this.funny_shapes = use;
	}

	public Shape transform(V v) {
		Shape shape = factory.getEllipse(v);
		if (funny_shapes) {
			if (graph.degree(v) < 5) {
				int sides = Math.max(graph.degree(v), 3);
				shape = factory.getRegularPolygon(v, sides);
			} else {
				shape = factory.getRegularStar(v, graph.degree(v));
			}
		}
		return shape;
	}
}
