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

import java.util.SortedSet;
import java.util.Vector;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;


public class CallGraphClusterTest extends TestCase {

	@Before
    public void setUp()
    {
    	ScoreComparator comparator = new ScoreComparator(); // lowest first
    	comparator.setAscending(false); // highest first
    	comparator.setScoreBeingCompared(ScoreType.BASIC);
		CallGraphCluster.setComparator(comparator);
    }


    @Test
    public void testCallGraphCluster()
    {
        CallGraphNode node1 = new CallGraphNode();
        node1.setId(1);
        node1.setScore(ScoreType.BASIC, 1.0);
        CallGraphNode node2 = new CallGraphNode();
        node2.setId(2);
        node2.setScore(ScoreType.BASIC, 2.0);
        CallGraphNode node3 = new CallGraphNode();
        node3.setId(3);
        node3.setScore(ScoreType.BASIC, 3.0);
        Vector<CallGraphNode> nodeVec = new Vector<CallGraphNode>();
        nodeVec.add(node2);
        nodeVec.add(node3);
        nodeVec.add(node1);
        CallGraphCluster cluster1 = new CallGraphCluster(nodeVec);
        SortedSet<CallGraphNode> nodes = cluster1.getElements();
        assertEquals(node3, nodes.first());
        assertEquals(node1, nodes.last());
    }

    @Test
    public void testToString()
    {
        CallGraphNode node1 = new CallGraphNode();
        node1.setId(1);
        node1.setLabel("node1");
        node1.setScore(ScoreType.BASIC, 1.0);
        CallGraphNode node2 = new CallGraphNode();
        node2.setId(2);
        node2.setLabel("node2");
        node2.setScore(ScoreType.BASIC, 2.0);
        CallGraphNode node3 = new CallGraphNode();
        node3.setId(3);
        node3.setLabel("node3");
        node3.setScore(ScoreType.BASIC, 3.0);
        Vector<CallGraphNode> nodeVec = new Vector<CallGraphNode>();
        nodeVec.add(node2);
        nodeVec.add(node3);
        nodeVec.add(node1);
        CallGraphCluster cluster1 = new CallGraphCluster(nodeVec);
        SortedSet<CallGraphNode> nodes = cluster1.getElements();
        String result = nodes.toString();
        int indexNode1 = result.indexOf("node1");
        int indexNode2 = result.indexOf("node2");
        int indexNode3 = result.indexOf("node3");
        assertTrue(indexNode1 > indexNode2);
        assertTrue(indexNode2 > indexNode3);
    }

}
