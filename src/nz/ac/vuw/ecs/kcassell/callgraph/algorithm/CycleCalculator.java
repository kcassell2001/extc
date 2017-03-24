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

package nz.ac.vuw.ecs.kcassell.callgraph.algorithm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import net.sourceforge.metrics.core.sources.TypeMetrics;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.persistence.MetricDatabaseLocator;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;

import org.eclipse.jdt.core.IType;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class CycleCalculator {

	/** A key to keep track of the order in which this node has
	 * been visited.	 */
	public static final String TARJAN_INDEX = "TARJAN";
	
	/** A key to keep track of the earliest-visited node to which
	 * this node has a return path.  */
	public static final String TARJAN_LOW_LINK = "TARJAN_LOW";
	
	/** DFS node number counter */
	protected int index = 0;
	
	/** The stack of nodes that have already been visited, but not yet
	 * assigned to a group of strongly connected components (scc). 	 */
	protected Stack<CallGraphNode> nodeStack = new Stack<CallGraphNode>();

	/**
	 * Gets the strongly connected components of a graph using Tarjan's
	 * algorithm
	 * @param jungGraph the graph being examined
	 * @return the strongly connected components
	 */
	public Set<CallGraphCluster> getStronglyConnectedComponents(
			Graph<CallGraphNode, CallGraphLink> jungGraph) {
		Set<CallGraphCluster> clusters = new HashSet<CallGraphCluster>();
		Collection<CallGraphNode> vertices = jungGraph.getVertices();

		// Reset whatever TARJAN scores may have existed previously
		for (CallGraphNode v : vertices) {
			v.setScore(TARJAN_INDEX, null);
		}

		// Start a depth-first search at each unvisited node
		for (CallGraphNode v : vertices) {
			Number vIndex = v.getScore(TARJAN_INDEX);
			
			// Examine the successors of the node if it hasn't been seen before
			if (vIndex == null) {
				getGroup(jungGraph, v, clusters);
			}
		}
		return clusters;
	}
	
	/**
	 * Returns true if the supplied graph contains cycles of any
	 * length (including a node with a link to itself).
	 * @param graph the graph being examined
	 * @return true if the graph contains cycles; false otherwise
	 */
	public boolean containsCycles(
			Graph<CallGraphNode, CallGraphLink> graph) {
		boolean isCyclic = false;
		Collection<CallGraphNode> vertices = graph.getVertices();
		
		// Start a depth-first search as each unvisited node
		for (CallGraphNode v : vertices) {
			Number vIndex = v.getScore(TARJAN_INDEX);
			
			// Test for one node cycle - edge to self
			Collection<CallGraphNode> successors = graph.getSuccessors(v);
			if (successors.contains(vIndex)) {
				isCyclic = true;
				break;
			}
			
			// Examine the successors of the node if it hasn't been seen before
			if (vIndex == null) {
				int groupSize = getGroup(graph, v, new HashSet<CallGraphCluster>());
				// Test for multi-node cycles
				if (groupSize > 1) {
					isCyclic = true;
					break;
				}
			}
		}
		return isCyclic;
	}
	
	/**
	 * Returns true if the supplied graph contains cycles
	 * @param graph the graph being examined
	 * @return the strongly connected components
	 */
	public boolean containsMultinodeCycles(
			Graph<CallGraphNode, CallGraphLink> graph) {
		boolean hasCycles = false;
		Collection<CallGraphNode> vertices = graph.getVertices();
		
		// Start a depth-first search as each unvisited node
		for (CallGraphNode v : vertices) {
			Number vIndex = v.getScore(TARJAN_INDEX);
			
			// Examine the successors of the node if it hasn't been seen before
			if (vIndex == null) {
				int groupSize = getGroup(graph, v, new HashSet<CallGraphCluster>());
				if (groupSize > 1) {
					hasCycles = true;
					break;
				}
			}
		}
		return hasCycles;
	}
	
	/**
	 * Gets the group of nodes that is mutually reachable from the specified one
	 * @param graph the graph being examined
	 * @param startNode the node whose mutually reachable nodes are desired
	 * @param clusters an accumulator for the collections of mutually reachable nodes
	 * @return the size of the newly added group of strongly
	 *  connected components
	 */
	private int getGroup(Graph<CallGraphNode, CallGraphLink> graph,
			CallGraphNode startNode,
			Set<CallGraphCluster> clusters) {
		startNode.setScore(TARJAN_INDEX, index); // Set the depth index
		startNode.setScore(TARJAN_LOW_LINK, index);
		index++;
		nodeStack.push(startNode);
		processSuccessors(graph, startNode, clusters);

		int groupSize = 0;
		Number vertexLowLink = startNode.getScore(TARJAN_LOW_LINK);
		Number vertexIndex = startNode.getScore(TARJAN_INDEX);

		// The vertex is the root of a new set of strongly connected components
		if (vertexIndex.intValue() == vertexLowLink.intValue()) {
			groupSize = addSCCToClusters(startNode, clusters);
		}
		return groupSize;
	}


	/**
	 * This method creates a new cluster and adds it to the collection of clusters.
	 * @param startNode
	 * @param clusters
	 * @return the size of the newly added group of strongly
	 *  connected components
	 */
	private int addSCCToClusters(CallGraphNode startNode, Set<CallGraphCluster> clusters) {
		Set<CallGraphNode> scc = new HashSet<CallGraphNode>();
		CallGraphNode top = null;
		int groupSize = 0;
		
		// Pop the stack of SCC elements until the start node is reached.
		// Each popped element is added to the SCC.
		do {
			top = nodeStack.pop();
			scc.add(top);
			groupSize++;
		} while (!startNode.equals(top));
		CallGraphCluster cluster = new CallGraphCluster(scc);
		clusters.add(cluster);
		return groupSize;
	}

	private void processSuccessors(Graph<CallGraphNode, CallGraphLink> graph,
			CallGraphNode startNode, Set<CallGraphCluster> clusters) {
		Collection<CallGraphNode> successors = graph.getSuccessors(startNode);
		for (CallGraphNode successor : successors) {
			Number successorIndex = successor.getScore(TARJAN_INDEX);

			// If successor hasn't been visited, recurse
			if (successorIndex == null) {
				processNewSuccessor(graph, startNode, clusters, successor);
			}
			// The successor has already been seen
			else if (nodeStack.contains(successor)) {
				processSeenSuccessor(startNode, successorIndex);
			}
		}
	}

	private void processSeenSuccessor(CallGraphNode startNode,
			Number successorIndex) {
		Number lowLink = startNode.getScore(TARJAN_LOW_LINK);
		if (successorIndex.intValue() < lowLink.intValue()) {
			startNode.setScore(TARJAN_LOW_LINK, successorIndex);
		}
	}

	private void processNewSuccessor(Graph<CallGraphNode, CallGraphLink> graph,
			CallGraphNode startNode, Set<CallGraphCluster> clusters,
			CallGraphNode successor) {
		getGroup(graph, successor, clusters);
		Number lowLink = startNode.getScore(TARJAN_LOW_LINK);
		Number successorLowLink = successor.getScore(TARJAN_LOW_LINK);
		if (successorLowLink.intValue() < lowLink.intValue()) {
			startNode.setScore(TARJAN_LOW_LINK, successorLowLink);
		}
	}
	
    /**
     * Given a SQL query, check the classes in the database that satisfy the query
     * to see whether they contain recursive cycles involving more than one method.
     * @param sql the SQL query
     * @param fileName the file to hold the results
     */
	public static void checkClassesForCycles(final String sql, String fileName) {
		final MetricDatabaseLocator locator = new MetricDatabaseLocator();
		System.out.println("loadingProblemClasses from database...");
		List<TypeMetrics> classes = new ArrayList<TypeMetrics>();
		try {
			locator.setSqlQuery(sql);
			classes = locator.findProblemClasses();
		} catch (Exception e) {
			String msg = "Unable to read classes from database: "
					+ e.getMessage();
			System.err.println(msg);
		}
		try {
			Writer fileWriter = new FileWriter(fileName);
			for (TypeMetrics typeMetric : classes) {
				IType iType = EclipseUtils.getTypeFromHandle(typeMetric.getHandle());
				if (iType != null) {
					try {
						String handle = typeMetric.getHandle();
						JavaCallGraph callGraph =
							new JavaCallGraph(handle, EdgeType.DIRECTED);
						CycleCalculator calc = new CycleCalculator();
						boolean hasCycles =
							calc.containsMultinodeCycles(callGraph.getJungGraph());
						fileWriter.write(callGraph.getName() +
								" containsMultinodeCycles = " + hasCycles + "\n");
					} catch (Exception e) {
						System.err.println(e);
					}
				}
			}
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
