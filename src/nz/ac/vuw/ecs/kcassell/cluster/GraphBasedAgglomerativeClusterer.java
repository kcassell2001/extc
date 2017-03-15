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
import java.util.Stack;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.graph.Graph;

public class GraphBasedAgglomerativeClusterer
implements ClustererIfc<CallGraphNode> {
	
	/** Calculates distances between nodes. */
	protected Distance<CallGraphNode> distanceCalculator = null;
	
	/** The stack keeps track of the change in the graph during clustering. */
	// TODO replace with something more efficient
	protected Stack<JavaCallGraph> graphStack = new Stack<JavaCallGraph>();
	
	/** Keeps track of how many clustering steps have occurred. */ 
	protected int previousIteration = 0;

	public GraphBasedAgglomerativeClusterer(JavaCallGraph callGraph,
			Distance<CallGraphNode> distance) {
//		this.callGraph = callGraph;
		graphStack.push(callGraph);
		this.distanceCalculator = distance;
	}

	/**
	 * @return the clusters
	 */
	public Collection<CallGraphNode> getClusters() {
		JavaCallGraph javaCallGraph = graphStack.elementAt(previousIteration);
		Graph<CallGraphNode,CallGraphLink> jungGraph =
			javaCallGraph.getJungGraph();
		Collection<CallGraphNode> vertices = jungGraph.getVertices();
		return vertices;
	}

    /**
     * Form clusters by combining nodes.  Two nodes should be combined
     * for each iteration.  The total number of cluster steps that should be
     * performed is determined by a user preference/parameter.  By default,
     * everything will be put into a single cluster.
     * @return a collection of all clusters (some will be single nodes)
     */
	public Collection<CallGraphNode> cluster() {
		Collection<CallGraphNode> clusters = null;
		int stackSize = graphStack.size();
		
		if (stackSize > 0) {
			JavaCallGraph javaCallGraph = graphStack.peek();
			Graph<CallGraphNode,CallGraphLink> jungGraph =
				javaCallGraph.getJungGraph();
			int vertexCount = jungGraph.getVertexCount();
			ApplicationParameters params =
				ApplicationParameters.getSingleton();
			int iterations = params.getIntParameter(
					ParameterConstants.AGGLOMERATION_CLUSTERS_KEY,
					vertexCount - 1);
			clusters = cluster(iterations);
		}
		return clusters;
	}

    /**
     * Form clusters by combining nodes.  Two nodes should be combined
     * for each iteration.
     * @param iteration the total number of cluster steps that should be
     * performed
     * @return a collection of all clusters (some will be single nodes)
     */
	public Collection<CallGraphNode> cluster(int iteration) {
		// clusters = new HashSet<CallGraphNode>();
		Collection<CallGraphNode> clusters = null;
		int stackSize = graphStack.size();
		int numIterations = iteration + 1 - stackSize;
		previousIteration = stackSize - 1;

		for (int i = 0; i < numIterations && continueClustering(); i++) {
			clusterOnce();
			previousIteration++;
		}	// for
		JavaCallGraph javaCallGraph = graphStack.elementAt(iteration);
		Graph<CallGraphNode, CallGraphLink> jungGraph =
			javaCallGraph.getJungGraph();
		clusters = jungGraph.getVertices();
		return clusters;
	}

	/**
	 * Add a new level of clustering
	 */
	protected void clusterOnce() {
		JavaCallGraph javaCallGraph = graphStack.peek();
		JavaCallGraph newCallGraph = null;
		try {
			newCallGraph = (JavaCallGraph)javaCallGraph.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Graph<CallGraphNode, CallGraphLink> graph =
			newCallGraph.getJungGraph();
		ArrayList<CallGraphLink> edgeList = getEdgesShortestFirst(graph);
		//TODO remove self-links
		if (0 < edgeList.size()) {
			CallGraphLink link = edgeList.remove(0);
			if (link != null) {
				Collection<CallGraphNode> nearNodes =
					graph.getIncidentVertices(link);
				CallGraphCluster cluster = mergeNodes(newCallGraph, nearNodes);
				// Make the name show the iteration the cluster was made
				// Parse the old name to remove the old number - avoid "+26+27"
				String name = cluster.getSimpleName();
				int indexPlus = name.indexOf("+");
				if (indexPlus >= 0) {
					name = name.substring(0, indexPlus + 1); // include the +
				}
				name += previousIteration + 1;
				cluster.setSimpleName(name);
				computeDistances(graph, cluster);
			}	// if link exists
		}
		graphStack.push(newCallGraph);
	}

	protected ArrayList<CallGraphLink> getEdgesShortestFirst(
			Graph<CallGraphNode, CallGraphLink> graph) {
		Collection<CallGraphLink> edges = graph.getEdges();
		ArrayList<CallGraphLink> edgeList =
			new ArrayList<CallGraphLink>(edges);
		Collections.sort(edgeList);
		return edgeList;
	}

	protected CallGraphCluster mergeNodes(JavaCallGraph callGraph,
			Collection<CallGraphNode> nearNodes) {
		CallGraphCluster cluster = null;
		int numNodes = nearNodes.size();
		if (numNodes != 2) {
			System.err.println("AgglomerativeClusterer.mergeNodes: "
					+ " link has " + numNodes + " endpoints");
		}
		else {
			cluster = formCluster(callGraph, nearNodes);
		}
		return cluster;
	}

	/**
	 * Reconnect the neighbors of node2 to node1
	 * @param cluster the cluster being relinked.  It's composed of two
	 *   subnodes
	 * @param jungGraph
	 */
	protected CallGraphCluster formCluster(JavaCallGraph callGraph,
			Collection<CallGraphNode> nearNodes) {
		CallGraphCluster cluster = new CallGraphCluster(nearNodes);
		Graph<CallGraphNode,CallGraphLink> jungGraph =
			callGraph.getJungGraph();
		ArrayList<CallGraphNode> subNodes =
			new ArrayList<CallGraphNode>(nearNodes); //cluster.getElements();
		CallGraphNode node1 = subNodes.get(0);
		CallGraphNode node2 = subNodes.get(1);
		Collection<CallGraphNode> node1Neighbors =
			jungGraph.getNeighbors(node1);
		Collection<CallGraphNode> node2Neighbors =
			jungGraph.getNeighbors(node2);
		
		if (node1Neighbors != null) {
			node1Neighbors.remove(node2);
			// Find the neighbors of node2 that aren't linked to node1
			if (node2Neighbors != null) {
				node2Neighbors.remove(node1);
				for (CallGraphNode neighbor1 : node1Neighbors) {
					node2Neighbors.remove(neighbor1);
				}
			}
		}
		
		reconnectLinksToCluster(callGraph, cluster, node1, node2Neighbors);
		reconnectLinksToCluster(callGraph, cluster, node2, node1Neighbors);

		jungGraph.removeVertex(node1);
		jungGraph.removeVertex(node2);
		jungGraph.addVertex(cluster);
		return cluster;
	}

	protected void reconnectLinksToCluster(JavaCallGraph callGraph,
			CallGraphCluster cluster, CallGraphNode node1,
			Collection<CallGraphNode> node2Neighbors) {
		if (node2Neighbors != null) {
			node2Neighbors.remove(node1);
			// Reconnect the neighbors of node2 to cluster
			for (CallGraphNode neighbor2 : node2Neighbors) {
				callGraph.createLink(cluster, neighbor2);
			}
		}
	}

	protected void computeDistances(Graph<CallGraphNode, CallGraphLink> graph,
			CallGraphCluster cluster) {
		// TODO see edu.uci.ics.jung.algorithms.shortestpath.Distance<V>,
		//   edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics
		//   edu.uci.ics.jung.algorithms.scoring.DistanceCentralityScorer<V,E>
		//   edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils
		Collection<CallGraphNode> neighbors = graph.getNeighbors(cluster);
		
		if (neighbors != null) {
			for (CallGraphNode neighbor : neighbors) {
				distanceCalculator.getDistance(cluster, neighbor);
			}
		}
	}

	protected boolean continueClustering() {
		// TODO write cohesion-based stopping criterion;
		// ultimately have a user-supplied command object
		return true;
	}
}
