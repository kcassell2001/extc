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

package nz.ac.vuw.ecs.kcassell.callgraph.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.apache.commons.collections15.Factory;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.jung.graph.Graph;

public class CallGraphReaderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testReadPajekNetGraphString() {
        JavaCallGraph callGraph = null;
        Factory<CallGraphNode> vertexFactory = new CallGraphNode.CallGraphNodeFactory();
        Factory<CallGraphLink> edgeFactory = new CallGraphLink.CallGraphLinkFactory();
        CallGraphReader graphReader = new CallGraphReader(vertexFactory,
                edgeFactory);
        try {
        	String file = RefactoringConstants.DATA_DIR + "SmallTests/kite.net";
			callGraph = graphReader.readPajekNetGraph(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
        assertNotNull(callGraph);
        Graph<CallGraphNode,CallGraphLink> jungGraph = callGraph.getJungGraph();
        Collection<CallGraphNode> vertices = jungGraph.getVertices();
        assertEquals(5, vertices.size());
        assertTrue(vertices.toString().indexOf("A1") > -1);
        assertTrue(vertices.toString().indexOf("E5") > -1);
        assertEquals(7, jungGraph.getEdgeCount());
	}

	@Test
	public void testReadGraphMLGraph() {
        JavaCallGraph callGraph = null;
        Factory<CallGraphNode> vertexFactory = new CallGraphNode.CallGraphNodeFactory();
        Factory<CallGraphLink> edgeFactory = new CallGraphLink.CallGraphLinkFactory();
        CallGraphReader graphReader = new CallGraphReader(vertexFactory,
                edgeFactory);
        try {
        	String file = RefactoringConstants.DATA_DIR + "SmallTests/graphmlColors.xml";
			callGraph = graphReader.readGraphMLGraph(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
        assertNotNull(callGraph);
        Graph<CallGraphNode,CallGraphLink> jungGraph = callGraph.getJungGraph();
        Collection<CallGraphNode> vertices = jungGraph.getVertices();
        assertEquals(6, vertices.size());
        assertTrue(vertices.toString().indexOf("A1") == -1);
        assertTrue(vertices.toString().indexOf("green") > -1);
        assertTrue(vertices.toString().indexOf("turquoise") > -1);
        assertEquals(7, jungGraph.getEdgeCount());
	}

}
