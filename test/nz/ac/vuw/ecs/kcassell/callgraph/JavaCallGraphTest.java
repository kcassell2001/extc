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

package nz.ac.vuw.ecs.kcassell.callgraph;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class JavaCallGraphTest extends TestCase
{
    JavaCallGraph callGraph = null;
    CallGraphNode node1 = null;
    CallGraphNode node2 = null;
    CallGraphLink link12 = null;

	@Before
    public void setUp()
    {
        callGraph = new JavaCallGraph();
    }

    @Test
    public void testGetVertexLabel()
    {
        callGraph = new JavaCallGraph();
    	String vertexLabel = callGraph.getVertexLabel(node1);
        assertNull(vertexLabel);
        node1 = callGraph.createNode("lab1");
        vertexLabel = callGraph.getVertexLabel(node1);
        assertEquals("lab1", vertexLabel);
    }

    @Test
    public void testGetNode()
    {
    	String vertexLabel = callGraph.getVertexLabel(node1);
        assertNull(vertexLabel);
        node1 = callGraph.createNode("lab1");
        CallGraphNode node = callGraph.getNode("lab1");
        assertEquals(node, node1);
    }

    @Test
    public void testAddNode()
    {
        node1 = new CallGraphNode();
        node1.setLabel("testAddNode1");
        callGraph.addNode(node1);
        List<CallGraphNode> nodes = callGraph.getNodes();
        assertEquals(1, nodes.size());
        CallGraphNode node1a = nodes.get(0);
        assertEquals(node1, node1a);
        assertEquals("testAddNode1", node1a.getLabel());

        node2 = new CallGraphNode();
        node2.setLabel("testAddNode2");
        callGraph.addNode(node2);
        nodes = callGraph.getNodes();
        assertEquals(2, nodes.size());
    }

    @Test
    public void testCreateLink()
    {
        testAddNode();
        link12 = callGraph.createLink(node1, node2);
        List<CallGraphLink> links1 = callGraph.getLinks(node1);
        assertEquals(1, links1.size());
        CallGraphLink link1a = links1.get(0);
        assertEquals(link12, link1a);

        List<CallGraphLink> links2 = callGraph.getLinks(node2);
        assertEquals(1, links2.size());
        CallGraphLink link2a = links2.get(0);
        assertEquals(link12, link2a);

        callGraph.createLink(node1, node2);
        links1 = callGraph.getLinks(node1);
        assertEquals(2, links1.size());
        links2 = callGraph.getLinks(node2);
        assertEquals(2, links2.size());
}

    @Test
    public void testGetNeighbors()
    {
        testAddNode();
        Collection<CallGraphNode> neighbors1 = callGraph.getNeighbors(node1);
        assertEquals(0, neighbors1.size());
        Collection<CallGraphNode> neighbors2 = callGraph.getNeighbors(node2);
        assertEquals(0, neighbors2.size());

        link12 = callGraph.createLink(node1, node2);
        neighbors1 = callGraph.getNeighbors(node1);
        assertEquals(1, neighbors1.size());
        neighbors2 = callGraph.getNeighbors(node2);
        assertEquals(1, neighbors2.size());

        CallGraphNode node3 = callGraph.createNode();
        callGraph.createLink(node2, node3);
        neighbors1 = callGraph.getNeighbors(node1);
        assertEquals(1, neighbors1.size());
        neighbors2 = callGraph.getNeighbors(node2);
        assertEquals(2, neighbors2.size());
        Collection<CallGraphNode> neighbors3 = callGraph.getNeighbors(node3);
        assertEquals(1, neighbors3.size());
    }


//    private static Graph<CallGraphNode, CallGraphLink> extractMinimalSpanningForest(LevelFinder levelFinder,
//            Graph<CallGraphNode, CallGraphLink> graph)
//    {
//        final Graph<CallGraphNode, CallGraphLink> graphF = graph;
//        Transformer<CallGraphLink, Double> edgeWeightTransformer =
//            new DegreeTransformer(graphF);
//        Graph<CallGraphNode, CallGraphLink> spanningForest =
//            JavaCallGraph.extractMinimalSpanningForest(graph, edgeWeightTransformer);
//        return spanningForest;
//    }

}
