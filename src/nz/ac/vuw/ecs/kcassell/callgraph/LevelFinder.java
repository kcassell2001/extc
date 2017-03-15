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

import java.util.HashSet;
import java.util.Set;

import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;

import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * @author kcassell
 * 
 */
public class LevelFinder {

	private static final UtilLogger logger =
    	new UtilLogger("LevelFinder");

	/**
	 * @param args
	 */
	// public static void main(String[] args)
	// {
	// LevelFinder levelFinder = new LevelFinder();
	// // For some reason, this seems to work for undirected graphs
	// // but not directed ones.
	// String graphFile = ThesisConstants.WORKSPACE_ROOT
	// + "datasets/basetests/personCarDirect.net";
	//
	// for (int i = 0; i < args.length; i++)
	// {
	// if ("-graphFile".equals(args[i]) && (args.length > i + 1))
	// {
	// graphFile = args[i + 1];
	// }
	// }
	//        
	// // Read the graph
	// Factory<CallGraphNode> vertexFactory = new
	// CallGraphNode.CallGraphNodeFactory();
	// Factory<CallGraphLink> edgeFactory = new
	// CallGraphLink.CallGraphLinkFactory();
	// GraphReader<CallGraphNode, CallGraphLink> graphReader = new
	// GraphReader<CallGraphNode, CallGraphLink>(
	// vertexFactory, edgeFactory);
	// Graph<CallGraphNode, CallGraphLink> graph = null;
	//        
	// try
	// {
	// graph = graphReader.readPajekNetGraph(graphFile);
	// levelFinder.logger.finest("After readGraph");
	// }
	// catch (IOException e)
	// {
	// levelFinder.logger.severe("Error in loading graph: " + e);
	// e.printStackTrace();
	// }
	//        
	// // Relabel the nodes descriptively
	// Collection<CallGraphNode> vertices = graph.getVertices();
	// for (CallGraphNode vertex : vertices)
	// {
	// vertex.setLabel(graphReader.getVertexLabel(vertex));
	// }
	// JavaCallGraph.setNodeTypes(vertices);
	// System.out.println("Graph = " + graph);
	//
	// Graph<CallGraphNode, CallGraphLink> dag =
	// levelFinder.createDAG(graph);
	// System.out.println("DAG = " + dag);
	// }

	/**
	 * Creates a directed acyclic graph where the 0 level nodes are the
	 * attributes. The level of a method nodes is the smallest number of method
	 * calls required to access any attribute. Therefore, any method that
	 * accesses an attribute directly has a level of 1.
	 * 
	 * Note that there is a loss of important information from the original
	 * input graph. There are no edges between nodes of the same level, so a
	 * connected graph can be transformed into a disconnected one.
	 * 
	 * @param inputGraph
	 *            a graph containing methods and attributes
	 * @return a directed acyclic graph
	 */
	public Graph<CallGraphNode, CallGraphLink> createDAG(
			Graph<CallGraphNode, CallGraphLink> inputGraph) {
		Graph<CallGraphNode, CallGraphLink> dag = createGraph(inputGraph,
				EdgeType.DIRECTED);
		return dag;
	}

	public Graph<CallGraphNode, CallGraphLink> createGraph(
			Graph<CallGraphNode, CallGraphLink> inputGraph, EdgeType edgeType) {
		Graph<CallGraphNode, CallGraphLink> dag = null;
		HashSet<CallGraphNode> fields = extractFields(inputGraph);

		if (inputGraph != null) {
			dag = new SparseGraph<CallGraphNode, CallGraphLink>();
			BFSDistanceLabeler<CallGraphNode, CallGraphLink> distancer = new BFSDistanceLabeler<CallGraphNode, CallGraphLink>();
			distancer.labelDistances(inputGraph, fields);
			for (CallGraphNode node : distancer.getVerticesInOrderVisited()) {
				dag.addVertex(node);
				Set<CallGraphNode> predecessors = distancer
						.getPredecessors(node);
				logger.fine("Predecessors of " + node.getLabel() + ": "
						+ predecessors);

				for (CallGraphNode pred : predecessors) {
					dag.addEdge(new CallGraphLink(), node, pred, edgeType);
				} // for predecessors
			} // nodes in the graph
		} // if
		return dag;
	}

	protected HashSet<CallGraphNode> extractFields(
			Graph<CallGraphNode, CallGraphLink> inputGraph) {
		HashSet<CallGraphNode> fields = new HashSet<CallGraphNode>();
		for (CallGraphNode node : inputGraph.getVertices()) {
			if (node.getNodeType() == NodeType.FIELD) {
				fields.add(node);
			}
		}
		System.out.println("fields = " + fields);
		return fields;
	}

}
