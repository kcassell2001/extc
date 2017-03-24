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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import nz.ac.vuw.ecs.kcassell.cluster.ClusterIfc;
import nz.ac.vuw.ecs.kcassell.cluster.MemberCluster;
import nz.ac.vuw.ecs.kcassell.utils.StringUtils;

/**
 * A CallGraphCluster a CallGraphNode that is composed of one or more CallGraphNodes.
 * @author Keith
 *
 */
public class CallGraphCluster extends CallGraphNode
//implements Comparable<CallGraphCluster>
 implements ClusterIfc<CallGraphNode>
{
    /** The nodes in the cluster, ordered (e.g. highest node betweenness first).
     *  This may be a hierarchical structure, e.g. a subnode may itself be a
     *  cluster.  */
    protected SortedSet<CallGraphNode> nodes = null;
    
    /** The (recursive) count of all subnodes. */
    protected int nodeCount = 0;
    
    protected static CallGraphClusterComparator clusterComparator =
        new CallGraphClusterComparator();
    
//    private static ScoreComparator nodeScoreComparator =
//        new ScoreComparator();
    
    protected static NodeNameComparator nodeNameComparator =
    	new NodeNameComparator();
    
    protected static ClusterSizeComparator clusterSizeComparator =
    	new ClusterSizeComparator();
    
    /** Determines how the nodes are ordered. */
    protected static Comparator<CallGraphNode> comparator = nodeNameComparator;
    

    {
        nodeNameComparator.setAscending(true);
//        nodeScoreComparator.setAscending(false);
    }
    
    /**
     * Creates a new cluster, ordered by score (descending)
     * @param nodes
     */
    public CallGraphCluster(Collection<CallGraphNode> nodes)
    {
		id = CallGraphNodeFactory.id;
		label = "CGNode" + CallGraphNodeFactory.id++;
		nodeType = NodeType.CLUSTER;
		
		if (nodes != null && nodes.size() > 0) {
			ArrayList<CallGraphNode> nodeList =
				new ArrayList<CallGraphNode>(nodes);
			try {
				String name1 = nodeList.get(0).getSimpleName();
				simpleName = name1 + "+";

				for (CallGraphNode subNode : nodes) {
					if (subNode instanceof CallGraphCluster) {
						CallGraphCluster subCluster = (CallGraphCluster) subNode;
						nodeCount += subCluster.nodeCount;
					} else {
						nodeCount++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			simpleName = label;
		}
    	nodeType = NodeType.CLUSTER;
        this.nodes = new TreeSet<CallGraphNode>(comparator);
        this.nodes.addAll(nodes);
    }

    /* (non-Javadoc)
	 * @see nz.ac.vuw.ecs.kcassell.callgraph.ClusterIfc#getElements()
	 */
    public SortedSet<CallGraphNode> getElements()
    {
    	SortedSet<CallGraphNode> elements =
    		new TreeSet<CallGraphNode>(comparator);
    	
    	for (CallGraphNode node : nodes) {
    		if (node instanceof CallGraphCluster) {
    			CallGraphCluster cluster = (CallGraphCluster)node;
    			SortedSet<CallGraphNode> subElements = cluster.getElements();
    			elements.addAll(subElements);
    		} else { // unclustered node
    			elements.add(node);
    		}
    	}
        return elements;
    }
    
    /* (non-Javadoc)
	 * @see nz.ac.vuw.ecs.kcassell.callgraph.ClusterIfc#getElementCount()
	 */
	public int getElementCount() {
		return nodeCount;
	}

	/**
	 * @param comparator the comparator to set
	 */
	public static void setComparator(Comparator<CallGraphNode> comparator) {
		CallGraphCluster.comparator = comparator;
	}

	public static Collection<CallGraphNode> toCallGraphClusters(
            Set<Set<CallGraphNode>> nodeGroupSet)
    {
        SortedSet<CallGraphCluster> clusters =
            new TreeSet<CallGraphCluster>(clusterComparator);
        
        for (Set<CallGraphNode> nodeGroup : nodeGroupSet)
        {
            CallGraphCluster cluster = new CallGraphCluster(nodeGroup);
            clusters.add(cluster);
        }
        ArrayList<CallGraphNode> clusterList = new ArrayList<CallGraphNode>();
        for (CallGraphCluster cluster : clusters) {
        	clusterList.add(cluster);
        }
        return clusterList;
    }

    /**
     * Convert each node (either a simple node or a cluster node) into
	 *	 a MemberCluster
     * @param nodeGroup a the CallGraphNodes to convert
     * @return the values retrieved from the nodes
     */
	public static List<MemberCluster> toMemberClusters(
			Collection<CallGraphNode> nodeGroup)
    {
		ArrayList<MemberCluster> memberClusters =
			new ArrayList<MemberCluster>();
        
		// Convert each node (either a simple node or a cluster node) into
		// a MemberCluster
        for (CallGraphNode node : nodeGroup)
        {
        	// CallGraphClusters shouldn't have a hierarchical structure, so
        	// we assume the child nodes are non-cluster nodes.
        	if (node instanceof CallGraphCluster) {
        		CallGraphCluster graphCluster = (CallGraphCluster)node;
        		SortedSet<CallGraphNode> memberNodes = graphCluster.getElements();
        		
        		if (memberNodes != null) {
            		MemberCluster memCluster = new MemberCluster();
            		memCluster.setClusterName(graphCluster.getSimpleName());

            		for (CallGraphNode memberNode : memberNodes) {
        				memCluster.addElement(memberNode.getLabel());
        			}
        			memberClusters.add(memCluster);
        		}
        	} else { // normal node
        		MemberCluster memCluster = new MemberCluster();
        		String memberName = node.getSimpleName();
				memCluster.setClusterName(memberName);
        		String nodeLabel = node.getLabel();
				memCluster.addElement(nodeLabel);
				//memCluster.addElement(memberName);
    			memberClusters.add(memCluster);
        	}
        }
        return memberClusters;
    }

    /**
     * Convert each MemberCluster into a CallGraphCluster
	 * @param memberClusters the clusters to convert
	 * @param labelsToVertices a mapping of names to CallGraphNodes
     * @return the new CallGraphClusters
     */
	public static List<CallGraphNode> toCallGraphClusters(
			Collection<MemberCluster> memberClusters,
			HashMap<String, CallGraphNode> labelsToVertices) {
		ArrayList<CallGraphNode> clusters = new ArrayList<CallGraphNode>();
//		clusters.clear();
		for (MemberCluster mCluster : memberClusters) {
			ArrayList<CallGraphNode> nodes = new ArrayList<CallGraphNode>();
			Set<String> members = mCluster.getElements();
			for (String handle : members) {
				CallGraphNode node = labelsToVertices.get(handle);
				if (node != null) {
					nodes.add(node);
				}
			}
			CallGraphCluster cgCluster = new CallGraphCluster(nodes);
			clusters.add(cgCluster);
		}
		return clusters;
	}

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(simpleName);
//        buf.append("nodes: {");
//        
//        for (CallGraphNode node : nodes)
//        {
//            boolean isOn = node.getShowToString();
//            node.setShowToString(true);
//            buf.append(node.toString()).append("; ");
//            node.setShowToString(isOn);
//        }
//        buf.delete(buf.length()-2, buf.length()-1);
//        buf.append("}");
        return buf.toString();
    }
    
    /* (non-Javadoc)
	 * @see nz.ac.vuw.ecs.kcassell.callgraph.ClusterIfc#toNestedString()
	 */
    public String toNestedString()
    {
    	StringBuffer buf = new StringBuffer();
    	toNestedString(0, buf);
    	String dendrogram = buf.toString();
    	return dendrogram;
    }
    
    public void toNestedString(int indentLevel, StringBuffer buf)
    {
    	String leadSpaces = StringUtils.SPACES140.substring(0, 2*indentLevel);
        
    	// If this is a top level (visible) node, print its name
    	if (indentLevel == 0) {
    		buf.append(simpleName).append("\n");
    	}
    	ArrayList<CallGraphNode> nodeArray =
    		new ArrayList<CallGraphNode>(nodes);
    	Collections.sort(nodeArray, comparator);
    	
        for (CallGraphNode node : nodeArray)
        {
        	indentLevel++;
        	if (node instanceof CallGraphCluster) {
            	buf.append(leadSpaces);
        		CallGraphCluster cluster = (CallGraphCluster)node;
				String name = cluster.getSimpleName();
				int indexPlus = name.lastIndexOf("+");
				// get the iteration number
				if (indexPlus >= 0) {
					String it = name.substring(indexPlus + 1);
	                buf.append(" |+" + it + "\n");
				}
				else {
	                buf.append(" |+\n");
				}
        		cluster.toNestedString(indentLevel, buf);
        	} else { // regular node
        		buf.append(leadSpaces).append("|-");
        		buf.append(node.getSimpleName()).append("\n");
        	}
        }
    }

	public static List<Integer> getClusterSizes(Collection<CallGraphNode> nodes) {
			ArrayList<Integer> sizes = new ArrayList<Integer>();
	//        StringBuffer buf = new StringBuffer();
	        for (CallGraphNode node : nodes)
	        {
	            int size = 1;
	            
	            if (node instanceof CallGraphCluster) {
	            	CallGraphCluster cluster = (CallGraphCluster)node;
	                size = cluster.getElements().size();
	            } else {
	            	size = 1;
	            }
	//            buf.append(size).append(SIZE_SEPARATOR);
	            sizes.add(size);
	        }
			return sizes;
		}

}
