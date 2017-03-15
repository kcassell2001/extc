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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;

public class MaxFlowClusterer implements ClustererIfc<CallGraphNode> {

	/** The graph being manipulated. */
	private JavaCallGraph callGraph = null;
	
	CallGraphNode source = null;
	CallGraphNode sink = null;
	
	Transformer<CallGraphLink, Double> capTransformer =
		new Transformer<CallGraphLink, Double>(){
		
		//TODO return the flow for an edge
		public Double transform(CallGraphLink link) {
			return 1.0;
		}
	};

	// This Factory produces new edges for use by the algorithm
	CallGraphLink.CallGraphLinkFactory edgeFactory =
		new CallGraphLink.CallGraphLinkFactory();

	public MaxFlowClusterer(JavaCallGraph callGraph, CallGraphNode source,
	CallGraphNode sink) {
		this.callGraph = callGraph;
		this.source = source;
		this.sink = sink;
	}

	public Collection<CallGraphNode> cluster() {
		// TODO make sure we use the largest connected component for max flow/min cut
		// while saving the disconnected components
		Collection<CallGraphNode> clusters = new ArrayList<CallGraphNode>();
		Map<CallGraphLink, Double> edgeFlowMap = new HashMap<CallGraphLink, Double>();
		Graph<CallGraphNode,CallGraphLink> jungGraph = callGraph.getJungGraph();
		
		if (jungGraph instanceof DirectedGraph<?,?>) {
			DirectedGraph<CallGraphNode, CallGraphLink> directedGraph =
				(DirectedGraph<CallGraphNode, CallGraphLink>)jungGraph;
			//	See the Jung2 Tutorial for usage of EdmondsKarpMaxFlow
			EdmondsKarpMaxFlow<CallGraphNode, CallGraphLink> maxFlowAlgo =
				new EdmondsKarpMaxFlow(directedGraph, source, sink, capTransformer, edgeFlowMap,
						edgeFactory);
			maxFlowAlgo.evaluate();
			Set<CallGraphNode> sinkPartition = maxFlowAlgo.getNodesInSinkPartition();
			CallGraphCluster sinkCluster = new CallGraphCluster(sinkPartition);
			clusters.add(sinkCluster);
			Set<CallGraphNode> sourcePartition = maxFlowAlgo.getNodesInSourcePartition();
			CallGraphCluster sourceCluster = new CallGraphCluster(sourcePartition);
			clusters.add(sourceCluster);
		} // TODO create directed equivalent of undirected graph by having two edges per one
		return clusters;
	}

	public Collection<CallGraphNode> cluster(int iteration) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<CallGraphNode> getClusters() {
		// TODO Auto-generated method stub
		return null;
	}
}
