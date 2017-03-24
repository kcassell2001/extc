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

import java.awt.BasicStroke;
import java.awt.Stroke;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;

import org.apache.commons.collections15.Transformer;

/**
 * This class adjusts the width of the line around the boundary of the nodes.
 * @author Keith Cassell
 */
public class NodeStrokeTransformer implements Transformer<CallGraphNode, Stroke> {

	protected static BasicStroke defaultStroke = new BasicStroke();
	protected static BasicStroke boldStroke = null;
	protected static BasicStroke dashedStroke = null;
	
	// initialize the bold stroke to be wider than the default width
	{
		float lineWidth = defaultStroke.getLineWidth();
		float boldWidth = lineWidth * 3;
		boldStroke = new BasicStroke(boldWidth);
		boldStroke = new BasicStroke(boldWidth);
		// see http://docstore.mik.ua/orelly/java-ent/jfc/ch04_05.htm
		dashedStroke = new BasicStroke(boldWidth,
                BasicStroke.CAP_BUTT,    // End cap
                BasicStroke.JOIN_ROUND,    // Join style
                10.0f,                     // Miter limit
                new float[] {2.0f, 4.0f},// Dash pattern
                0.0f);                     // Dash phase
	}
	
	public NodeStrokeTransformer() {
	}

	public Stroke transform(CallGraphNode node) {
		Stroke stroke = defaultStroke;
		
		if (node.isInherited()) {
			stroke = defaultStroke;
		} else if (node.isInner()) {
			stroke = dashedStroke;
		} else {
			stroke = boldStroke;
		}
		return stroke;
	}
}
