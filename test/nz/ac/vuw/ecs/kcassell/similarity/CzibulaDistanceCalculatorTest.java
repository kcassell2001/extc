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

package nz.ac.vuw.ecs.kcassell.similarity;

import java.util.HashSet;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.NodeType;
import nz.ac.vuw.ecs.kcassell.similarity.CzibulaDistanceCalculator;

import org.junit.Before;
import org.junit.Test;

public class CzibulaDistanceCalculatorTest extends TestCase
{
    private JavaCallGraph graph = null;
    private CallGraphNode nodeA1 = null;
    private CallGraphNode nodeA2 = null;
    private CallGraphNode nodeA3 = null;
    private CallGraphNode nodeM1 = null;
    private CallGraphNode nodeM2 = null;
    private CallGraphNode nodeM3 = null;
    private CallGraphNode nodeM4 = null;

	@Before
    public void setUp()
    {
        // Manually create a graph
        graph = new JavaCallGraph();
        nodeA1 = graph.createNode("A1");
        nodeA1.setNodeType(NodeType.FIELD);
        nodeA2 = graph.createNode("A2");
        nodeA2.setNodeType(NodeType.FIELD);
        nodeA3 = graph.createNode("A3");
        nodeA3.setNodeType(NodeType.FIELD);
        nodeM1 = graph.createNode("M1");
        nodeM1.setNodeType(NodeType.METHOD);
        nodeM2 = graph.createNode("M2");
        nodeM2.setNodeType(NodeType.METHOD);
        nodeM3 = graph.createNode("M3");
        nodeM3.setNodeType(NodeType.METHOD);
        nodeM4 = graph.createNode("M4");
        nodeM4.setNodeType(NodeType.METHOD);
        
        graph.createLink(nodeM1, nodeA1);
        graph.createLink(nodeM2, nodeA2);
        graph.createLink(nodeM3, nodeA2);
        graph.createLink(nodeM3, nodeA3);
        graph.createLink(nodeM4, nodeA1);
        graph.createLink(nodeM4, nodeA2);
        graph.createLink(nodeM4, nodeA3);
    }
    

    @Test
    public void testCalculateDistance()
    {
        CzibulaDistanceCalculator calculator =
            new CzibulaDistanceCalculator(graph);
        HashSet<String> propA1 = CzibulaDistanceCalculator.getProperties(nodeA1, graph);
        assertEquals(4, propA1.size()); // A1, M1, M4, C

        HashSet<String> propA2 = CzibulaDistanceCalculator.getProperties(nodeA2, graph);
        assertEquals(5, propA2.size()); // A2, M2, M3, M4, C

        HashSet<String> propM1 = CzibulaDistanceCalculator.getProperties(nodeM1, graph);
        assertEquals(3, propM1.size()); // A1, M1, C
        assertTrue(propM1.contains("A1"));
        assertTrue(propM1.contains("M1"));

        HashSet<String> propM4 = CzibulaDistanceCalculator.getProperties(nodeM4, graph);
        assertEquals(5, propM4.size()); // A1, A2, A3, M4, C
        assertTrue(propM4.contains("A1"));
        assertTrue(propM4.contains("M4"));
        
        Double dist = calculator.calculateDistance(nodeM1, nodeA1);
        assertEquals(0.25, dist);
        dist = calculator.calculateDistance(nodeM4, nodeA2);
        assertEquals((1.0 - 3.0/7.0), dist);
    }

    @Test
    public void testGetProperties()
    {
        HashSet<String> propA1 = CzibulaDistanceCalculator.getProperties(nodeA1, graph);
        assertEquals(4, propA1.size()); // A1, M1, M4, C
        assertTrue(propA1.contains("A1"));
        assertTrue(propA1.contains("M1"));
        assertTrue(propA1.contains("M4"));

        HashSet<String> propA2 = CzibulaDistanceCalculator.getProperties(nodeA2, graph);
        assertEquals(5, propA2.size()); // A2, M2, M3, M4, C
        assertTrue(propA2.contains("A2"));
        assertTrue(propA2.contains("M2"));
        assertTrue(propA2.contains("M3"));
        assertTrue(propA2.contains("M4"));

        HashSet<String> propA3 = CzibulaDistanceCalculator.getProperties(nodeA3, graph);
        assertEquals(4, propA3.size()); // A3, M3, M4, C
        assertTrue(propA3.contains("A3"));
        assertTrue(propA3.contains("M3"));
        assertTrue(propA3.contains("M4"));

        HashSet<String> propM1 = CzibulaDistanceCalculator.getProperties(nodeM1, graph);
        assertEquals(3, propM1.size()); // A1, M1, C
        assertTrue(propM1.contains("A1"));
        assertTrue(propM1.contains("M1"));

        HashSet<String> propM2 = CzibulaDistanceCalculator.getProperties(nodeM2, graph);
        //System.out.println("propM2 = " + propM2);
        assertEquals(3, propM2.size()); // A2, M2, C
        assertTrue(propM2.contains("A2"));
        assertTrue(propM2.contains("M2"));

        HashSet<String> propM3 = CzibulaDistanceCalculator.getProperties(nodeM3, graph);
        //System.out.println("propM3 = " + propM3);
        assertEquals(4, propM3.size()); // A2, A3, M3, C
        assertTrue(propM3.contains("A2"));
        assertTrue(propM3.contains("A3"));
        assertTrue(propM3.contains("M3"));

        HashSet<String> propM4 = CzibulaDistanceCalculator.getProperties(nodeM4, graph);
        //System.out.println("propM4 = " + propM4);
        assertEquals(5, propM4.size()); // A1, A2, A3, M4, C
        assertTrue(propM4.contains("A1"));
        assertTrue(propM4.contains("A2"));
        assertTrue(propM4.contains("A3"));
        assertTrue(propM4.contains("M4"));
    }

}
