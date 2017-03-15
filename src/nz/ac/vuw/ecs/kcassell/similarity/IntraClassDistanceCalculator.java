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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.NodeNameComparator;
import nz.ac.vuw.ecs.kcassell.callgraph.ScoreType;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;

/**
 * This class is used to calculate the distance between nodes, where the
 * distance equals the shortest undirected path between the two nodes with an
 * added fractional distance based on the number of incident edges on the two
 * nodes.  NOTE: this will not cluster all nodes for a disconnected graph.
 * 
 * Maybe change to:
 * Interface distances = 0
 * Calling distances as above
 * "Semantic distances" based on identifiers
 * 
 * @author kcassell
 * 
 */
public class IntraClassDistanceCalculator
implements Distance<CallGraphNode>, DistanceCalculatorIfc<String> {
	
	protected UnweightedShortestPath<CallGraphNode, CallGraphLink>
	shortestPathCalculator = null;
	
	/**
	 * Provides distances between nodes. The key is the source node; the value
	 * is a hash map of distances. For the distance hash map, the key is the
	 * destination node, and the value is the distance from the source to the
	 * destination.	 */
	protected Map<CallGraphNode, Map<CallGraphNode, Number>> distanceMap =
		new HashMap<CallGraphNode, Map<CallGraphNode,Number>>();

	//	protected Map<CallGraphNode, Map<CallGraphNode, CallGraphLink>>
//	incomingEdgeMap = null;
	
	JavaCallGraph undirectedGraph = null;


	/**
	 * Constructs the calculator of node distances.  It calculates all distances
	 *  between nodes and stores them.
	 * @param callGraph
	 */
	public IntraClassDistanceCalculator(JavaCallGraph callGraph) {
		JavaCallGraph undirectedGraph =
			JavaCallGraph.toUndirectedGraph(callGraph);
		this.undirectedGraph = undirectedGraph;
		Graph<CallGraphNode,CallGraphLink> jungGraph =
			undirectedGraph.getJungGraph();
		shortestPathCalculator =
			new UnweightedShortestPath<CallGraphNode, CallGraphLink>(jungGraph);
		
		List<CallGraphNode> nodes = undirectedGraph.getNodes();
		
		for (CallGraphNode node1 : nodes) {
			HashMap<CallGraphNode, Number> distances =
				new HashMap<CallGraphNode, Number>();
			Collection<CallGraphLink> edges1 =
				jungGraph.getIncidentEdges(node1);
			double edgeCount1 = edges1.size();

			for (CallGraphNode node2 : nodes) {
				Number distance =
					calculateDistance(jungGraph, node1, node2, edgeCount1);
				distances.put(node2, distance);
				CallGraphLink link = jungGraph.findEdge(node1, node2);
				if (link != null) {
					link.setWeight(ScoreType.BASIC, distance);
				}
			}
			distanceMap.put(node1, distances);
		}

	}

	/**
	 * Returns the distance between the provided nodes. The distance equals the
	 * shortest undirected path between the two nodes with an added fractional distance
	 * based on the number of incident edges on the two nodes
	 */
	public Number getDistance(CallGraphNode node1, CallGraphNode node2) {
		Double distance = Double.MAX_VALUE;
		Map<CallGraphNode, Number> node1Distances = distanceMap.get(node1);
		
		if (node1Distances == null) {
			node1Distances = new HashMap<CallGraphNode, Number>();
			distanceMap.put(node1, node1Distances);
		}
		
		Number distanceN = node1Distances.get(node2);
		if (distanceN != null) {
			distance = distanceN.doubleValue();
		} else {
			Graph<CallGraphNode,CallGraphLink> jungGraph =
				undirectedGraph.getJungGraph();
			Collection<CallGraphLink> incidentEdges = jungGraph.getIncidentEdges(node1);
			int edgeCount1 = incidentEdges.size();
			distanceN = calculateDistance(jungGraph, node1, node2, edgeCount1);
			distance = distanceN.doubleValue();
			node1Distances.put(node2, distance);
			CallGraphLink link = jungGraph.findEdge(node1, node2);
			if (link != null) {
				link.setWeight(ScoreType.BASIC, distance);
			}
		}
		return distance;
	}

	/**
	 * Returns the distance between the provided nodes. The distance equals the
	 * shortest undirected path between the two nodes with an added fractional distance
	 * based on the number of incident edges on the two nodes
	 */
	protected Number calculateDistance(Graph<CallGraphNode,CallGraphLink> jungGraph,
			CallGraphNode node1, CallGraphNode node2, double edgeCount1) {
		Double distance = Double.MAX_VALUE;
		
		if (node1.equals(node2)) {
			distance = 0.0;
		}
		else {
			Number pathDistance = shortestPathCalculator.getDistance(node1,	node2);

			if (pathDistance != null) {
				// TODO optimize - only collect edges once
				Collection<CallGraphLink> edges2 = jungGraph.getIncidentEdges(node2);
				double edgeCount2 = edges2.size();

				// Commonality will be (0, 1].  Nodes with few edges to other nodes will
				// be judged more tightly associated ("Common") and have a higher score.
				Double commonality = 0.0;
				
				if (edgeCount1 != 0 || edgeCount2 != 0) {
					commonality = 2.0 / (edgeCount1 + edgeCount2);
				}
				distance = pathDistance.doubleValue() + (1 - commonality);
			}
		}
		return distance;
	}

	/**
	 * Returns the distances from the specified node to the other nodes in the graph.
	 * @param node1 the source node
	 * @return the key is the destination node, and
	 *    the value is the distance from the source to the destination.
	 * @see edu.uci.ics.jung.algorithms.shortestpath.Distance#getDistanceMap(java.lang.Object)
	 */
	public Map<CallGraphNode, Number> getDistanceMap(CallGraphNode node1) {
		Map<CallGraphNode, Number> distances = distanceMap.get(node1);
		return distances;
	}

	@Override
	/**
	 * @return a human-readable form of the distance matrix
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer(getClass().getSimpleName() + "@"
				+ hashCode() + "\n");
		Set<CallGraphNode> nodes = distanceMap.keySet();
		List<CallGraphNode> nodeList = new ArrayList<CallGraphNode>(nodes);
		Collections.sort(nodeList, new NodeNameComparator());
		int numNodes = nodeList.size();
		
		//Print column headers
		buf.append("           ");
		for (int i = 0; i < numNodes; i++) {
			buf.append(" ");
			CallGraphNode node1 = nodeList.get(i);
			String name = node1.getSimpleName();
			name = String.format("%10.10s", name);
			buf.append(name);
		}
		buf.append("\n");
		
		// print rows
		for (int i = 0; i < numNodes; i++) {
			buf.append(" ");
			CallGraphNode node1 = nodeList.get(i);
			String name = node1.getSimpleName();
			name = String.format("%-10.10s", name);
			buf.append(name);
			for (int j = 0; j < i+1; j++) {
				CallGraphNode node2 = nodeList.get(j);
				Number distanceN = getDistance(node1, node2);
				String distance = "         x";
				if (!distanceN.equals(Double.MAX_VALUE)) {
					distance = String.format("% 10.2f", distanceN);
				}
				buf.append(" ").append(distance);
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	public Number calculateDistance(String id1, String id2) {
		Number distance = RefactoringConstants.UNKNOWN_DISTANCE;
		
		if (undirectedGraph != null) {
			CallGraphNode node1 = undirectedGraph.getNode(id1);
			CallGraphNode node2 = undirectedGraph.getNode(id2);
			if ((node1 != null) && (node2 != null)) {
				distance = getDistance(node1, node2);
			} else {
				System.err.println("Unexpected null: " + id1 + ": " + node1 +
						", " + id2 + ": " + node1);
			}
		}
		return distance;
	}

	public DistanceCalculatorEnum getType() {
		return DistanceCalculatorEnum.IntraClass;
	}

}
