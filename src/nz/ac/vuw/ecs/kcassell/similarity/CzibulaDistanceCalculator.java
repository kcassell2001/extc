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

import java.util.Collection;
import java.util.HashSet;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.NodeType;

/**
 * This distance calculator is based on the one described in
 * "Hierarchical Clustering for Software Systems Restructuring" by
 * Czibula and Serban.
 * @author Keith
 *
 */
public class CzibulaDistanceCalculator
extends JaccardCalculator
implements DistanceCalculatorIfc<String>
{
    /** The graph showing the static interrelationships of methods and
     * attributes.  */
    protected JavaCallGraph javaCallGraph = null;

    /**
     * @param javaCallGraph
     */
    public CzibulaDistanceCalculator(JavaCallGraph javaCallGraph)
    {
        super();
        this.javaCallGraph = javaCallGraph;
    }

    /**
     * This provides the property set for a method or attribute
     * represented as a CallGraphNode.
     * @param node the node representing a method or attribute
     * @return The property set, p(e), represents a set of
     * relevant properties of e, defined as:
     * o If e is an attribute, then p(e) consists of: the attribute
     *   itself, the application class where the attribute is defined,
     *   and all methods from S that access the attribute.
     * o If e is a method, then p(e) consists of: the method itself,
     *   the application class where the method is defined, and
     *   all attributes from S accessed by the method.
     */
    public static HashSet<String> getProperties(
            CallGraphNode node,
            JavaCallGraph graph)
    {
        HashSet<String> properties = new HashSet<String>();
        properties.add("DefiningClass");    // surrogate for the containing class
        properties.add(node.getLabel());    // this element
        Collection<CallGraphNode> neighbors = graph.getNeighbors(node);
        
        // For a field, add all the methods that access this field
        if (node.getNodeType() == NodeType.FIELD)
        {
            for (CallGraphNode neighbor : neighbors)
            {
                properties.add(neighbor.getLabel());
            }
        }
        // For a method, add all the fields that this method accesses
        else
        {
            for (CallGraphNode neighbor : neighbors)
            {
                if (neighbor.getNodeType() == NodeType.FIELD)
                {
                    properties.add(neighbor.getLabel());
                }
            }
        }
        return properties;
    }
    

    /* (non-Javadoc)
     * @see nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorIfc#calculateDistance(java.lang.String, java.lang.String)
     */
    public Double calculateDistance(String id1, String id2)
    {
        Double distance = null;
        CallGraphNode node1 = javaCallGraph.getNode(id1);
        CallGraphNode node2 = javaCallGraph.getNode(id2);

        if (node1 == null) {
        	System.err.println("CzibulaDistanceCalculator.calculateDistance: node "
        			+ id1 + " not found.");
        } else if (node2 == null) {
        	System.err.println("CzibulaDistanceCalculator.calculateDistance: node "
        			+ id2 + " not found.");
        } else {
        	distance = calculateDistance(node1, node2);
        }
        return distance;
    }

    public Double calculateDistance(
            CallGraphNode node1,
            CallGraphNode node2)
    {
        Double distance = null;
        HashSet<String> properties1 = getProperties(node1, javaCallGraph);
        HashSet<String> properties2 = getProperties(node2, javaCallGraph);
        distance = calculateDistance(properties1, properties2);
        return distance;
    }

	public DistanceCalculatorEnum getType() {
		return DistanceCalculatorEnum.Czibula;
	}

}
