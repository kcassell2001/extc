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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.ClusterSizeComparator;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.ScoreComparator;
import nz.ac.vuw.ecs.kcassell.callgraph.ScoreType;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class BetweennessClusterer implements ClustererIfc<CallGraphNode> {
	/** The number of clusters currently */
	private int numClusters = 1;

	/** The current collection of clusters. */
	private Collection<CallGraphNode> clusters = null;

	/** The graph being manipulated. */
	private JavaCallGraph callGraph = null;

	/** The edges removed (in order) thus far. */
	private List<CallGraphLink> edgesRemoved = null;

	/** A comparator for sorting clusters by size. */
	private static ClusterSizeComparator sizeComparator =
		new ClusterSizeComparator();
	
	private static UtilLogger utilLogger =
		new UtilLogger("BetweennessCalculator");

	{
		sizeComparator.setAscending(false);
	}

	public BetweennessClusterer(JavaCallGraph callGraph) {
		this.callGraph = callGraph;
	}

	public int getNumClusters() {
		return numClusters;
	}

	public Collection<CallGraphNode> getClusters() {
		return clusters;
	}

	public JavaCallGraph getCallGraph() {
		return callGraph;
	}

	public List<CallGraphLink> getEdgesRemoved() {
		return edgesRemoved;
	}

	public static ClusterSizeComparator getSizeComparator() {
		return sizeComparator;
	}

	/**
	 * Form the number of new clusters specified by the
	 *  user preferences/parameters (or 1 if not specified).
	 * @return the clusters (disconnected parts of the graph)
	 */
	public Collection<CallGraphNode> cluster() {
        Graph<CallGraphNode, CallGraphLink> jungGraph = callGraph.getJungGraph();
        int numEdgesToRemove = 0;
        Collection<CallGraphNode> clusters = cluster(numEdgesToRemove);
        int origClusterCount = clusters.size();
        int lastClusterCount = origClusterCount;
//    	List<Integer> sizes = CallGraphCluster.getClusterSizes(clusters);
//    	Collections.sort(sizes);
		ApplicationParameters parameters =
			ApplicationParameters.getSingleton();
		int numToCreate = parameters.getIntParameter(
				ParameterConstants.NEW_BETWEENNESS_CLUSTERS_KEY, 1);

        while ((lastClusterCount < origClusterCount + numToCreate)
                && (numEdgesToRemove < jungGraph.getEdgeCount()))
        {
            numEdgesToRemove++;
            clusters = cluster(numEdgesToRemove);
            
            // When a new cluster is produced, update the output
            if (clusters.size() > lastClusterCount)
            {
                lastClusterCount = clusters.size();
//            	List<Integer> sizes = CallGraphCluster.getClusterSizes(clusters);
//            	Collections.sort(sizes);
            }
        }
//        List<CallGraphLink> edgesRemoved = clusterer.getEdgesRemoved();
//        String edgesRemovedString =
//            clusterer.edgesRemovedToString(jungGraph, edgesRemoved);
		return clusters;
	}

	/**
	 * Form clusters after removing the specified number of edges
	 * @param graph
	 * @param numEdgesToRemove
	 * @return the new clusters (disconnected parts of the graph)
	 */
	public Collection<CallGraphNode> cluster(int numEdgesToRemove) {
		Graph<CallGraphNode, CallGraphLink> jungGraph = callGraph.getJungGraph();
		EdgeBetweennessClusterer<CallGraphNode, CallGraphLink> clusterer =
			new EdgeBetweennessClusterer<CallGraphNode, CallGraphLink>(
				numEdgesToRemove);

		Set<Set<CallGraphNode>> nodeGroupSet = clusterer.transform(jungGraph);
		clusters = CallGraphCluster.toCallGraphClusters(nodeGroupSet);
		utilLogger.fine("clusters = " + clusters);
		edgesRemoved = clusterer.getEdgesRemoved();
		utilLogger.fine("edgesRemoved = " + edgesRemoved);
		int iCount = clusters.size();

		String edgesRemovedString = edgesRemovedToString(jungGraph, edgesRemoved);

		// New cluster
		if (iCount > numClusters) {
			numClusters = iCount;
			utilLogger.info("New clusters after " + numEdgesToRemove
					+ " edges removed:\n" + clusters);
			utilLogger.info("Edges removed:\n" + edgesRemovedString);
		}

		/*
		 * clusterer.transform is misleading. The edges removed are actually
		 * added back in again, so the graph is restored to its original state.
		 * That is why we re-remove the edges.
		 */
		// TODO make this less kludgy
		recalculateBetweenness(jungGraph, edgesRemoved);
		return clusters;
	}

	public String edgesRemovedToString(
			Graph<CallGraphNode, CallGraphLink> graph,
			List<CallGraphLink> edgesRemoved) {
		StringBuffer buf = new StringBuffer();
		int whenRemoved = 1;
		for (CallGraphLink link : edgesRemoved) {
			Pair<CallGraphNode> endpoints = graph.getEndpoints(link);
			String edgeString = String.format(
					"  %d) %s <--> %s, strength %.1f,\n", whenRemoved++,
					endpoints.getFirst().getLabel(), endpoints.getSecond()
							.getLabel(), link.getWeight());
			buf.append(edgeString);
		}
		return buf.toString();
	}

	/**
	 * Given edges removed from the graph, calculates the new betweenness values
	 * for the nodes and edges.
	 * 
	 * @param graph
	 *            the graph
	 * @param removedEdgesList
	 *            the edges that have been removed from the graph
	 */
	void recalculateBetweenness(Graph<CallGraphNode, CallGraphLink> graph,
			List<CallGraphLink> removedEdgesList) {
		Map<CallGraphLink, Pair<CallGraphNode>> removedEdgesMap = removeEdges(
				graph, removedEdgesList);
		BetweennessCentrality<CallGraphNode, CallGraphLink> ranker = new BetweennessCentrality<CallGraphNode, CallGraphLink>(
				graph, true, true);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();
		Collection<CallGraphLink> edges = graph.getEdges();
		for (CallGraphLink edge : edges) {
			edge.setWeight(ranker.getEdgeRankScore(edge));
		}
		Collection<CallGraphNode> nodes = graph.getVertices();
		for (CallGraphNode node : nodes) {
			node
					.setScore(ScoreType.CENTRALITY, ranker
							.getVertexRankScore(node));
		}
		addEdges(graph, removedEdgesList, removedEdgesMap);
	}

	private void addEdges(Graph<CallGraphNode, CallGraphLink> graph,
			List<CallGraphLink> edgesRemoved,
			Map<CallGraphLink, Pair<CallGraphNode>> removedEdgesMap) {
		for (CallGraphLink edge : edgesRemoved) {
			Pair<CallGraphNode> endpoints = removedEdgesMap.get(edge);
			graph.addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
		}
	}

	private Map<CallGraphLink, Pair<CallGraphNode>> removeEdges(
			Graph<CallGraphNode, CallGraphLink> graph,
			List<CallGraphLink> edgesRemoved) {
		Map<CallGraphLink, Pair<CallGraphNode>> removedEdges = new HashMap<CallGraphLink, Pair<CallGraphNode>>();
		for (CallGraphLink edge : edgesRemoved) {
			Pair<CallGraphNode> removedEdgeEndpoints = graph.getEndpoints(edge);
			removedEdges.put(edge, removedEdgeEndpoints);
			graph.removeEdge(edge);
		}
		return removedEdges;
	}

	public TreeSet<CallGraphNode> calculateHubScores(
			Graph<CallGraphNode, CallGraphLink> jungGraph) {
		ScoreComparator comparator = new ScoreComparator();
		// large scores indicate hubs
		comparator.setAscending(false);
		TreeSet<CallGraphNode> orderedNodes = new TreeSet<CallGraphNode>(
				comparator);
		Collection<CallGraphNode> vertices = jungGraph.getVertices();

		utilLogger.info("Barycentric scores:");
		for (CallGraphNode node : vertices) {
			Double score = jungGraph.getIncidentEdges(node).size() * 1.0;
			utilLogger.info("\t" + node.toString() + ":" + score);
			node.setScore(ScoreType.CENTRALITY, score);
			orderedNodes.add(node);
		}
		return orderedNodes;
	}

	protected TreeSet<CallGraphNode> calculateBarycentricScores(
			Graph<CallGraphNode, CallGraphLink> jungGraph) {
		ScoreComparator comparator = new ScoreComparator();
		// small scores indicate more central nodes
		comparator.setAscending(true);
		TreeSet<CallGraphNode> orderedNodes = new TreeSet<CallGraphNode>(
				comparator);
		ClosenessCentrality<CallGraphNode, CallGraphLink> scorer =
			new ClosenessCentrality<CallGraphNode, CallGraphLink>(jungGraph);
		Collection<CallGraphNode> vertices = jungGraph.getVertices();

		utilLogger.info("Barycentric scores:");
		for (CallGraphNode node : vertices) {
			Double score = scorer.getVertexScore(node);
			utilLogger.info("\t" + node.toString() + ":" + score);
			node.setScore(ScoreType.CENTRALITY, score);
			orderedNodes.add(node);
		}
		return orderedNodes;
	}

//	public Collection<CallGraphNode> cluster(int iteration) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
