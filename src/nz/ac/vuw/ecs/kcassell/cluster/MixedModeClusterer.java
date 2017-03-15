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

package nz.ac.vuw.ecs.kcassell.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.ClusterSizeComparator;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;

/**
 * This clusterer combines the activities of other clusterers.  For example,
 * betweenness clustering can be used to form a set of clusters, then 
 * agglomerative clustering can be used to merge some of those.
 * @author kcassell
 *
 */
public class MixedModeClusterer implements ClustererIfc<CallGraphNode> {

	/** The current collection of clusters. */
	private Collection<CallGraphNode> clusters = null;

	/** The final collection of clusters. */
	private Collection<MemberCluster> finalMemberClusters = null;

	/** The graph being manipulated. */
	private JavaCallGraph callGraph = null;
	
	/** A comparator for sorting clusters by size. */
	private static ClusterSizeComparator sizeComparator =
		new ClusterSizeComparator();
	
	private static UtilLogger utilLogger =
		new UtilLogger("MixedModeClusterer");

	{
		sizeComparator.setAscending(false);
	}

	public MixedModeClusterer(JavaCallGraph callGraph) {
		this.callGraph = callGraph;
	}


	public Collection<CallGraphNode> getClusters() {
		return clusters;
	}

	public Collection<MemberCluster> getFinalMemberClusters() {
		return finalMemberClusters;
	}


	public Collection<CallGraphNode> cluster(int iteration) {
		// TODO make incremental
		return cluster();
	}

	/**
	 * First, Betweenness clustering will break up the class into
	 * at least two groups.  Then agglomerative clustering will add
	 * the smaller groups to the two largest groups.
	 */
	public Collection<CallGraphNode> cluster() {
		// Betweenness clustering
		BetweennessClusterer betClusterer =
			new BetweennessClusterer(callGraph);
		// creates the number of new clusters specified by a param
		clusters = betClusterer.cluster();
		String sClusters = buildClustersString();
		utilLogger.info("betweenness clusters for " + callGraph.getName() +
				":\n" + sClusters);
		
		ArrayList<CallGraphNode> nodeClusters =
			new ArrayList<CallGraphNode>(clusters);
		Collections.sort(nodeClusters, sizeComparator);
		
		// Agglomerative clustering
		List<MemberCluster> memberClusters =
			CallGraphCluster.toMemberClusters(nodeClusters);
		
		if (memberClusters.size() > 2) {
			MemberCluster seed1 = memberClusters.get(0);
			MemberCluster seed2 = memberClusters.get(1);
			String classHandle = callGraph.getHandle();

			DisjointClusterer djClusterer =
				new DisjointClusterer(seed1, seed2, memberClusters, classHandle);
			djClusterer.cluster();
			finalMemberClusters = djClusterer.getMemberClusters();
			HashMap<String, CallGraphNode> labelsToVertices =
				callGraph.getLabelsToVertices();

			clusters = CallGraphCluster.toCallGraphClusters(
					finalMemberClusters, labelsToVertices);
			sClusters = buildClustersString();
			utilLogger.info("final clusters for " + callGraph.getName() +
					":\n" + sClusters);
		}
		return clusters;
	}


	/**
	 * @return the clusters as a user-readable string
	 */
	private String buildClustersString() {
		StringBuffer buf = new StringBuffer();
		for (CallGraphNode node: clusters) {
			buf.append(node.toNestedString());
			buf.append("\n");
		}
		return buf.toString();
	}

}
