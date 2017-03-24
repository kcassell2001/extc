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

/**
 * This is the JDeodorant distance calculator as described in their 2011 paper.
 * @see "JDeodorant: Identification and Application of Extract
 *   Class Refactorings", Marios Foakaefs, et al., 2011.
 * @author Keith
 *
 */
public class JDeodorantDistanceCalculator
extends JaccardCalculator
implements DistanceCalculatorIfc<String>
{
    /** The graph showing the static interrelationships of methods and
     * attributes.  */
    protected JavaCallGraph javaCallGraph = null;

    /**
     * @param javaCallGraph
     */
    public JDeodorantDistanceCalculator(JavaCallGraph javaCallGraph)
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
     * for a method:
     *  o all the members of the class that use or are used by the method
     * for an attribute:
     *  o all the members of the class that use or are used by the attribute.
     */
    public static HashSet<String> getProperties(
            CallGraphNode node,
            JavaCallGraph graph)
    {
		HashSet<String> properties = new HashSet<String>();
		Collection<CallGraphNode> neighbors = graph.getNeighbors(node);
		for (CallGraphNode neighbor : neighbors) {
			properties.add(neighbor.getLabel());
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
        	System.err.println("SimonDistanceCalculator.calculateDistance: node "
        			+ id1 + " not found.");
        } else if (node2 == null) {
        	System.err.println("SimonDistanceCalculator.calculateDistance: node "
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
		return DistanceCalculatorEnum.JDeodorant;
	}

}
