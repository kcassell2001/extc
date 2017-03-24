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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.eclipse.jdt.core.Flags;

import edu.uci.ics.jung.io.GraphMLMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;

public class CallGraphNode {
	public static final Number UNKNOWN_SCORE = Double.NaN;
	// public static final String GRAPHML_ATTRIBUTE = "attribute";
	public static final String GRAPHML_ACCESS = "access";
	public static final String GRAPHML_LABEL = "label";
	public static final String GRAPHML_MEMBER_TYPE = "memberType";
	public static final String GRAPHML_SCORE = "score";

	/** Keeps track of the number of nodes seen thus far. */
	protected static int generatedId = 0;
	
	/** The unique identifier of the node. */
	protected int id = generatedId++;

	/** The type of the element in the call graph, e.g. "method" */
	protected NodeType nodeType = NodeType.UNKNOWN;
	
	/** Indicates whether the member is inherited from a superclass. */
	protected boolean isInherited = false;
	
	/** Indicates whether the member is from an inner class. */
	protected boolean isInner = false;
	
	/**
	 * The modifier flags for a class's member. The flags can be examined
	 * using class org.eclipse.jdt.core.Flags.
	 * @see org.eclipse.jdt.core.Flags
	 * @see org.eclipse.jdt.core.IMember#getFlags()
	 */
	protected int memberFlags = 0; // Flags.AccDefault = 0

	/**
	 * The label provided in the data file or the Eclipse handle name, often the
	 * node name. The label as displayed on the graph may be different. A graph
	 * node is often displayed using a labeler based on the toString method.
	 */
	protected String label;

	/** A simplified version of the label. */
	protected String simpleName;

	/** A collection of various scores. */
	protected HashMap<String, Number> scores = new HashMap<String, Number>();

	/** Any data a program wants to associate with a node. */
	protected Object userData = null;

	/** Controls whether the toString method produces a nonempty string. */
	protected boolean showToString = true;
	
	/** The type of score to use as part of a node label. */
	protected String scoreTypeForLabel = ScoreType.BASIC;

	protected static GraphMLMetadata<CallGraphNode> graphMLAccessMetaData =
		new GraphMLMetadata<CallGraphNode>(
			"accessMetadata", "public",
			new GraphMLAccessMetadataWriter());
	protected static GraphMLMetadata<CallGraphNode> graphMLMemberTypeMetaData =
		new GraphMLMetadata<CallGraphNode>(
			"memberTypeMetadata", "METHOD",
			new GraphMLMemberTypeMetadataWriter());
	protected static GraphMLMetadata<CallGraphNode> graphMLScoreMetaData =
		new GraphMLMetadata<CallGraphNode>(
			"scoreMetadata", "1.666", new GraphMLScoreMetadataWriter());
	protected static GraphMLMetadata<CallGraphNode> graphMLLabelMetaData =
		new GraphMLMetadata<CallGraphNode>(
			"labelMetadata", "unknownLabel", new GraphMLLabelMetadataWriter());

	protected CallGraphNode() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label for a node.
	 * Use this with caution.  Setting labels on a node directly can mess up
	 * the graph's indexing of nodes.
	 * @param label
	 */
	protected void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the simpleName
	 */
	public String getSimpleName() {
		return simpleName;
	}

	/**
	 * @param simpleName
	 *            the simpleName to set
	 */
	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	/**
	 * @return the memberFlags
	 */
	public int getMemberFlags() {
		return memberFlags;
	}

	/**
	 * @param memberFlags the memberFlags to set
	 */
	public void setMemberFlags(int memberFlags) {
		this.memberFlags = memberFlags;
	}

	public Number getScore(String String) {
		Number scoreN = scores.get(String);
		return scoreN;
	}

	public void setScore(String String, Number score) {
		scores.put(String, score);
	}

	public String getScoreTypeForLabel() {
		return scoreTypeForLabel;
	}

	public void setScoreTypeForLabel(String scoreForLabel) {
		this.scoreTypeForLabel = scoreForLabel;
	}

	/**
	 * @return the userData
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * @param userData the userData to set
	 */
	public void setUserData(Object userData) {
		this.userData = userData;
	}

	/**
	 * @return the isInherited
	 */
	public boolean isInherited() {
		return isInherited;
	}

	/**
	 * @param isInherited the isInherited to set
	 */
	public void setInherited(boolean isInherited) {
		this.isInherited = isInherited;
	}

	public boolean isInner() {
		return isInner;
	}

	public void setInner(boolean isInner) {
		this.isInner = isInner;
	}

	/**
	 * @param turnOffToString
	 *            the turnOffToString to set
	 */
	public void setShowToString(boolean b) {
		showToString = b;
	}

	public boolean getShowToString() {
		return showToString;
	}

    public String toNestedString()
    {
    	return simpleName + "\n";
    }
    


	public String toString() {
		StringBuffer buf = new StringBuffer();

		if (showToString) {
			if (simpleName != null) {
				buf.append(simpleName);
			} else if (label != null) {
				buf.append(label);
			}

			Number scoreN = getScore(scoreTypeForLabel);
			Double score = (scoreN == null) ? null : scoreN.doubleValue();
			if ((score != null) && !Double.isNaN(score)) {
				buf.append("(").append(String.format("%.1f", score))
						.append(")");
			}
		}
		return buf.toString();
	}

	/**
	 * @return the node type
	 */
	public NodeType getNodeType() {
		return nodeType;
	}

	/**
	 * @param nodeType
	 */
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * @return the graphMLMemberTypeMetaData
	 */
	public static GraphMLMetadata<CallGraphNode> getGraphMLMemberTypeMetaData() {
		return graphMLMemberTypeMetaData;
	}

	public static GraphMLMetadata<CallGraphNode> getGraphMLAccessMetaData() {
		return graphMLAccessMetaData;
	}

	public static GraphMLMetadata<CallGraphNode> getGraphMLScoreMetaData() {
		return graphMLScoreMetaData;
	}

	public static GraphMLMetadata<CallGraphNode> getGraphMLLabelMetaData() {
		return graphMLLabelMetaData;
	}

	/** Used to create nodes. */
	public static class CallGraphNodeFactory implements Factory<CallGraphNode> {
		protected static int id = 0;

		public CallGraphNode create() {
			CallGraphNode node = new CallGraphNode();
			node.setId(id);
			node.setLabel("CGNode" + id++);
			return node;
		}

	} // class CallGraphNodeFactory

	public static class GraphMLMemberTypeMetadataWriter implements
			Transformer<CallGraphNode, String> {
		public String transform(CallGraphNode node) {
			return node.getNodeType().toString();
		}
	}

	public static class GraphMLLabelMetadataWriter implements
			Transformer<CallGraphNode, String> {
		public String transform(CallGraphNode node) {
			String nodeLabel = node.getLabel();
			try {
				nodeLabel = URLEncoder.encode(nodeLabel, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			return nodeLabel;
		}
	}

	public static class GraphMLAccessMetadataWriter implements
			Transformer<CallGraphNode, String> {
		public String transform(CallGraphNode node) {
			String access = "default";
			int flags = node.getMemberFlags();
			if (Flags.isPrivate(flags)) {
				access = "private";
			} else if (Flags.isProtected(flags)) {
				access = "protected";
			} else if (Flags.isPublic(flags)) {
				access = "public";
			}
			return access;
		}
	}

	public static class GraphMLScoreMetadataWriter implements
			Transformer<CallGraphNode, String> {
		// TODO change getScore; extract GraphML helper class
		public String transform(CallGraphNode node) {
			return "" + node.getScore(ScoreType.BASIC);
		}
	}

	/**
	 * This class is used to create CallGraphNodes from GraphML via
	 * CallGraphReader2.
	 * 
	 * @author Keith
	 */
	public static class GraphMLNodeMetadataReader implements
			Transformer<NodeMetadata, CallGraphNode> {
		int n = 0;

		public CallGraphNode transform(NodeMetadata metadata) {
			CallGraphNode node = new CallGraphNode();
			Map<String, String> properties = metadata.getProperties();
			String label = properties.get(GRAPHML_LABEL);
			if (label == null) {
				label = "CGNode" + n++;
			} else {
				try {
					label = URLDecoder.decode(label, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
			}
			node.setLabel(label);
			String sScore = properties.get(GRAPHML_SCORE);
			double score = 0.0;
			try {
				score = Double.parseDouble(sScore);
			} catch (Exception e) {
			}
			node.setScore(ScoreType.BASIC, score);
			String sMember = properties.get(GRAPHML_MEMBER_TYPE);
			if (sMember.equals(NodeType.FIELD.toString())) {
				node.setNodeType(NodeType.FIELD);
			} else {
				node.setNodeType(NodeType.METHOD);
			}
			return node;
		}
	}

	/**
	 * This class generates the node's label. It is used by PajekNetWriter.
	 * 
	 * @author kcassell
	 */
	public static class NodeLabelTransformer implements
			Transformer<CallGraphNode, String> {

		public String transform(CallGraphNode node) {
			return node.getLabel();
		}
	} // NodeLabelTransformer

}