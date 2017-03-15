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

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class ClusterSizeComparatorTest {

	ClusterSizeComparator comparator = new ClusterSizeComparator();

	@Before
	public void setUp() throws Exception {
		comparator.setAscending(true);
	}

	@Test
	public void testCompareNodes() {
        CallGraphNode node1 = new CallGraphNode();
        node1.setSimpleName("n1");
        CallGraphNode node2 = new CallGraphNode();
        node2.setSimpleName("n2");
        CallGraphNode node3 = new CallGraphNode();
        node3.setSimpleName("n1");
        
        assertTrue(comparator.compare(node1, node1) == 0);
        assertTrue(comparator.compare(node1, node2) < 0);
        assertTrue(comparator.compare(node1, node3) < 0); // by node id
        assertTrue(comparator.compare(node2, node3) > 0);

        comparator.setAscending(false);
        assertTrue(comparator.compare(node1, node2) > 0);
        assertTrue(comparator.compare(node1, node3) > 0); // by node id
        assertTrue(comparator.compare(node2, node3) < 0);
	}

	@Test
	public void testCompareNodesAndClusters() {
        CallGraphNode node1 = new CallGraphNode();
        node1.setSimpleName("a1");
        CallGraphNode node2 = new CallGraphNode();
        node2.setSimpleName("n2");
        ArrayList<CallGraphNode> nodes1 = new ArrayList<CallGraphNode>();
        nodes1.add(node1);
		CallGraphCluster cluster1 = new CallGraphCluster(nodes1 );
		cluster1.setSimpleName("cluster1");
        ArrayList<CallGraphNode> nodes2 = new ArrayList<CallGraphNode>();
        nodes2.add(node1);
        nodes2.add(node2);
		CallGraphCluster cluster2 = new CallGraphCluster(nodes2 );
		cluster2.setSimpleName("zcluster2");
        
        assertTrue(comparator.compare(cluster1, cluster1) == 0);
        assertTrue(comparator.compare(cluster1, node1) > 0); // by name
        assertTrue(comparator.compare(cluster1, node2) < 0); // by name
        assertTrue(comparator.compare(node2, cluster1) > 0); // by name
        assertTrue(comparator.compare(cluster2, node1) > 0); // by count
        assertTrue(comparator.compare(node1, cluster2) < 0); // by count
        assertTrue(comparator.compare(cluster2, node2) > 0); // by count
        assertTrue(comparator.compare(cluster1, cluster2) < 0); // by count
        assertTrue(comparator.compare(cluster2, cluster1) > 0); // by count

        comparator.setAscending(false);
        assertTrue(comparator.compare(cluster1, cluster1) == 0);
        assertTrue(comparator.compare(cluster1, node1) < 0); // by name
        assertTrue(comparator.compare(cluster1, node2) > 0); // by name
        assertTrue(comparator.compare(node2, cluster1) < 0); // by name
        assertTrue(comparator.compare(cluster2, node1) < 0); // by count
        assertTrue(comparator.compare(node1, cluster2) > 0); // by count
        assertTrue(comparator.compare(cluster2, node2) < 0); // by count
        assertTrue(comparator.compare(cluster1, cluster2) > 0); // by count
        assertTrue(comparator.compare(cluster2, cluster1) < 0); // by count
	}

	@Test
	public void testCompareNames() {
        CallGraphNode node1 = new CallGraphNode();
        node1.setSimpleName("n1");
        CallGraphNode node2 = new CallGraphNode();
        node2.setSimpleName("n2");
        CallGraphNode node3 = new CallGraphNode();
        node3.setSimpleName("n1");
        
        assertTrue(comparator.compareNames(node1, node1) == 0);
        assertTrue(comparator.compareNames(node1, node2) < 0);
        assertTrue(comparator.compareNames(node1, node3) < 0); // by node id
        assertTrue(comparator.compareNames(node2, node3) > 0);
	}

}
