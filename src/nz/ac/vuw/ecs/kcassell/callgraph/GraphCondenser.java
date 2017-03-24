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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.SortedSet;

import nz.ac.vuw.ecs.kcassell.callgraph.algorithm.CycleCalculator;

import org.apache.commons.collections15.Factory;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class GraphCondenser {

	/**
	 * Creates a condensed graph from the provided directed graph by
	 * replacing clusters with single nodes.
	 * @param jungGraph the graph being examined
	 * @param clusters collections of nodes that will be "condensed" into
	 *   single nodes
	 * @return the condensed graph
	 */
	public static JavaCallGraph toCondensedGraph(
			JavaCallGraph oldGraph,
			Set<CallGraphCluster> clusters) {
		Graph<CallGraphNode, CallGraphLink> jungGraph = oldGraph.getJungGraph();
		Factory<CallGraphNode> vertexFactory = oldGraph.getVertexFactory();
		JavaCallGraph newGraph = new JavaCallGraph();
		newGraph.setName(oldGraph.getName());
		newGraph.setHandle(oldGraph.getHandle());
		newGraph.setDefaultEdgeType(oldGraph.getDefaultEdgeType());
		
		// The key is the original node; the value is the (possibly newly condensed)
		// node
		Hashtable<CallGraphNode, CallGraphNode> nodeTranslator =
			new Hashtable<CallGraphNode, CallGraphNode>();
		// Add the condensed nodes to the node translator
		convertClustersToNodes(vertexFactory, clusters, nodeTranslator);
		
		// Add the uncondensed nodes to the node translator
		for (CallGraphNode node : jungGraph.getVertices()) {
			if (!nodeTranslator.containsKey(node)) {
				nodeTranslator.put(node, node);
			}
		}
		Collection<CallGraphNode> newNodes = nodeTranslator.values();
		
		// Add nodes to the new graph
		for (CallGraphNode newNode : newNodes) {
			newGraph.addNode(newNode);
		}
		createLinks(oldGraph, newGraph, nodeTranslator);
		return newGraph;
	}

	/**
	 * Creates a DAG from the provided directed graph by removing links
	 * from a node to itself and by replacing multi-node cycles with a
	 * single node.
	 * @param jungGraph the graph being examined
	 * @param nodeFactory the node creator
	 * @param linkFactory the link creator
	 * @return the condensed graph
	 */
	public static JavaCallGraph toDAG(JavaCallGraph oldGraph) {
		Graph<CallGraphNode, CallGraphLink> jungGraph = oldGraph.getJungGraph();
		Factory<CallGraphNode> nodeFactory = oldGraph.getVertexFactory();
		Factory<CallGraphLink> linkFactory = oldGraph.getEdgeFactory();
		JavaCallGraph newGraph = new JavaCallGraph();
		newGraph.setName(oldGraph.getName());
		newGraph.setHandle(oldGraph.getHandle());
		newGraph.setDefaultEdgeType(oldGraph.getDefaultEdgeType());
		
		CycleCalculator cycler = new CycleCalculator();
		Set<CallGraphCluster> clusters = cycler.getStronglyConnectedComponents(jungGraph);
		// The key is the original node; the value is the (possibly newly condensed)
		// node in the DAG
		Hashtable<CallGraphNode, CallGraphNode> nodeTranslator =
			new Hashtable<CallGraphNode, CallGraphNode>();
		Set<CallGraphNode> nodes =
			convertClustersToNodes(nodeFactory, clusters, nodeTranslator);
		for (CallGraphNode node : nodes) {
			newGraph.addNode(node);
		}
		createDAGLinks(jungGraph, linkFactory, newGraph.getJungGraph(), nodeTranslator);
		return newGraph;
	}

	private static void createDAGLinks(Graph<CallGraphNode, CallGraphLink> jungGraph,
			Factory<CallGraphLink> linkFactory,
			Graph<CallGraphNode, CallGraphLink> dag,
			Hashtable<CallGraphNode, CallGraphNode> nodeTranslator) {
		Collection<CallGraphNode> origVertices = jungGraph.getVertices();
		for (CallGraphNode origVertex : origVertices) {
			CallGraphNode dagVertex = nodeTranslator.get(origVertex);
			if (dagVertex == null) {
				System.err.println("GraphCondenser.createDAGLinks: unable to locate DAG vertex for " + origVertex);
			}
			else {
				Collection<CallGraphNode> successors = jungGraph.getSuccessors(origVertex);
				
				for (CallGraphNode successor : successors) {
					CallGraphNode dagSuccesor = nodeTranslator.get(successor);
					
					if (!dagVertex.equals(dagSuccesor)	// ensure no self-loop
							&& !dag.isSuccessor(dagVertex, dagSuccesor))  /* no dups  */ {
						CallGraphLink dagLink = linkFactory.create();
						dag.addEdge(dagLink, dagVertex, dagSuccesor, EdgeType.DIRECTED);
					}
				}
			}
		}
	}

	/**
	 * Create links for the newly condensed graph based on the edges that were
	 * present in the original graph.
	 * @param oldGraph the original graph
	 * @param newGraph the new graph, to which edges are being added
	 * @param nodeTranslator matches up nodes in the original graph to those
	 * (possibly condensed) nodes in the new graph.
	 */
	private static void createLinks(JavaCallGraph oldGraph,
			JavaCallGraph newGraph,
			Hashtable<CallGraphNode, CallGraphNode> nodeTranslator) {
		Graph<CallGraphNode, CallGraphLink> oldJungGraph =
			oldGraph.getJungGraph();
		Graph<CallGraphNode, CallGraphLink> newJungGraph =
			newGraph.getJungGraph();
		Collection<CallGraphNode> origVertices = oldJungGraph.getVertices();
		for (CallGraphNode origVertex : origVertices) {
			CallGraphNode newVertex = nodeTranslator.get(origVertex);
			// Handle the case where a vertex may have been removed,
			// e.g. a constructor
			if (newVertex == null) {
				System.err.println("GraphCondenser.createLinks: unable to locate vertex for "
						+ origVertex);
			}
			else { 	// newVertex != null
				Collection<CallGraphNode> successors =
					oldJungGraph.getSuccessors(origVertex);
				EdgeType edgeType = newGraph.getDefaultEdgeType();
				Factory<CallGraphLink> linkFactory = newGraph.getEdgeFactory();

				// Link newVertex to its successors
				for (CallGraphNode succ : successors) {
					CallGraphNode newSucc = nodeTranslator.get(succ);
					CallGraphLink edge = linkFactory.create();
					
					// Don't link a node to itself
					if (!newVertex.equals(newSucc)) {
						newJungGraph.addEdge(edge, newVertex, newSucc, edgeType);
					}
				}	// for
			}	// else newVertex != null
		}	// for
	}

	/**
	 * Create nodes from clusters.  If the cluster contains one node,
	 * that node is added to the collection.  If the cluster contains multiple
	 * nodes, a new node is created that represents the cluster, and
	 * that node is added to the collection.
	 * @param nodeFactory used to create new nodes
	 * @param clusters contain one or more nodes
	 * @param nodeTranslator keeps track of node correspondences -
	 *   the key is the original node; the value is the (possibly newly condensed)
	 *   node in the DAG
	 */
	private static Set<CallGraphNode> convertClustersToNodes(Factory<CallGraphNode> nodeFactory,
			Set<CallGraphCluster> clusters,
			Hashtable<CallGraphNode, CallGraphNode> nodeTranslator) {
		HashSet<CallGraphNode> newNodes = new HashSet<CallGraphNode>();
		for (CallGraphCluster cluster : clusters) {
			SortedSet<CallGraphNode> clusterNodes = cluster.getElements();
			int clusterSize = clusterNodes.size();
			if (clusterSize == 1) {
				CallGraphNode node = clusterNodes.first();
				newNodes.add(node);
				nodeTranslator.put(node, node);
			}
			else if (clusterSize > 1) {
				CallGraphNode combinedNode =
					createNodeForCluster(cluster, nodeFactory);
				newNodes.add(combinedNode);
				for (CallGraphNode origNode : clusterNodes) {
					nodeTranslator.put(origNode, combinedNode);
				}
			}
		}
		return newNodes;
	}

	private static CallGraphNode createNodeForCluster(CallGraphCluster cluster,
			Factory<CallGraphNode> nodeFactory) {
		SortedSet<CallGraphNode> clusterNodes = cluster.getElements();
		int clusterSize = clusterNodes.size();
		CallGraphNode firstNode = clusterNodes.first();
		String firstLabel = firstNode.getLabel();
		String firstSimple = firstNode.getSimpleName();
		CallGraphNode compositeNode = nodeFactory.create();
		compositeNode.setNodeType(NodeType.CLUSTER);
		compositeNode.setUserData(cluster);
		compositeNode.setLabel(firstLabel + "And" + (clusterSize - 1));
		compositeNode.setSimpleName(firstSimple + "And" + (clusterSize - 1));
		compositeNode.setShowToString(firstNode.getShowToString());
		return compositeNode;
	}

	/**
	 * Combines all method nodes that are imposed by an interface or
	 * a superclass into a single cluster node.
	 * @param graph2 
	 * @param type the class whose call graph we're manipulating
	 * @return the graph with the new cluster node and without the nodes
	 * that went into it.
	 * @throws JavaModelException
	 */
	public static JavaCallGraph condenseRequiredMethods(JavaCallGraph graph,
			IType type,
			boolean includeObjectMethods)
			throws JavaModelException {
		HashSet<CallGraphCluster> clusters =
			new HashSet<CallGraphCluster>();
		CallGraphCluster requiredMethods =
			graph.getRequiredMethods(type, includeObjectMethods);
		if (requiredMethods != null) {
			clusters.add(requiredMethods);
		}
		JavaCallGraph condensedGraph = toCondensedGraph(graph, clusters);
		return condensedGraph;
	}
	
}
