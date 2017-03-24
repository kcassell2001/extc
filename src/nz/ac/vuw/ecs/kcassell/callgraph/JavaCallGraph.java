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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseSearchUtils;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.jung.algorithms.util.MapSettableTransformer;
import edu.uci.ics.jung.algorithms.util.SettableTransformer;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.io.graphml.GraphMetadata;

/**
 * This class stores information about Java method calls and attribute accesses
 * in a graph data structure.
 * 
 * @author Keith
 * 
 */
public class JavaCallGraph
implements Cloneable, ParameterConstants, RefactoringConstants {
	protected Graph<CallGraphNode, CallGraphLink> jungGraph =
		new SparseMultigraph<CallGraphNode, CallGraphLink>();

	// private final Logger logger =
	// Logger.getLogger(this.getClass().getSimpleName());

	protected Factory<CallGraphNode> vertexFactory =
		new CallGraphNode.CallGraphNodeFactory();
	protected Factory<CallGraphLink> edgeFactory =
		new CallGraphLink.CallGraphLinkFactory();

	/** The type of the link if not otherwise specified. */
	private EdgeType defaultEdgeType = EdgeType.UNDIRECTED;

    /** Indicates whether constructors should be included in the graph.
     * (Cached parameter value.) */
    private boolean includeConstructors = true;
    
	private boolean includeObjectMethods = true;

	private boolean includeLoggers = true;

	private boolean includeStatics = true;

	private boolean condenseImposed = false;

	private boolean condenseObjectsMethods = false;

	/** Keeps track of the correspondence of labels to vertices. */
	protected HashMap<String, CallGraphNode> labelsToVertices =
		new HashMap<String, CallGraphNode>();

	/** The name of the thing being graphed. */
	protected String name = "";
	
	/** The Eclipse handle representing the IType being graphed. */
	protected String handle = null;
	
	/** The type of score to use as part of calculations. */
	protected String primaryScore = ScoreType.BASIC;

	/** The supertypes of the type whose call graph is here,
	 *  in bottom up order. */
	private IType[] supertypes;


	/**
	 * This keeps track of the correspondence between a vertex and its label.
	 */
	protected SettableTransformer<CallGraphNode, String> vertexLabeler =
		new MapSettableTransformer<CallGraphNode, String>(
			new HashMap<CallGraphNode, String>());

	public JavaCallGraph() {
		super();
		getParameters();
	}

	/**
	 * Constructor for use with graphs read in via JUNG I/O (PajekNet, GraphML)
	 * @param jungGraph the graph read from file
	 * @param labelsToVertices matches labels to nodes
	 * @param vertexLabeler
	 */
	public JavaCallGraph(Graph<CallGraphNode, CallGraphLink> jungGraph,
			HashMap<String, CallGraphNode> labelsToVertices,
			SettableTransformer<CallGraphNode, String> vertexLabeler) {
		super();
		getParameters();
		this.jungGraph = jungGraph;
		this.labelsToVertices = labelsToVertices;
		this.vertexLabeler = vertexLabeler;

		// Assume that all of the edges are of the same type
		Collection<CallGraphLink> edges = jungGraph.getEdges(EdgeType.DIRECTED);
		if (edges.isEmpty()) {
			defaultEdgeType = EdgeType.UNDIRECTED;
		} else {
			defaultEdgeType = EdgeType.DIRECTED;
		}
	}
	
	/**
	 * Builds a JavaCallGraph based on an Eclipse handle
	 * @param handle the Eclipse handle (uid)
	 * @param edgeType directed/undirected
	 * @throws JavaModelException
	 */
	public JavaCallGraph(String handle, EdgeType edgeType)
			throws JavaModelException {
		getParameters();
		CallData callData = EclipseUtils.createCallData(handle);
		this.handle = handle;
		IJavaElement element = JavaCore.create(handle);
		setName(element.getElementName());
		setDefaultEdgeType(edgeType);
		processCallData(callData);
	}

	private void getParameters() {
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		includeConstructors =
			parameters.getBooleanParameter(INCLUDE_CONSTRUCTORS_KEY, true);
	    includeObjectMethods =
	    	parameters.getBooleanParameter(INCLUDE_OBJECT_METHODS_KEY, true);
	    includeLoggers =
	    	parameters.getBooleanParameter(INCLUDE_LOGGERS_KEY, true);
		includeStatics = parameters.getBooleanParameter(INCLUDE_STATICS_KEY, true);
		condenseImposed =
			parameters.getBooleanParameter(CONDENSE_IMPOSED_METHODS_KEY, false);
		condenseObjectsMethods =
			parameters.getBooleanParameter(CONDENSE_OBJECTS_METHODS_KEY, false);
		String sEdgeType =
			parameters.getParameter(EDGE_TYPE_KEY, EdgeType.DIRECTED.toString());
		defaultEdgeType = EdgeType.valueOf(sEdgeType);
		//TODO consider synchronized as a grouping mechanism
	}


	/**
	 * 
	 * @return the underlying JUng graph representation
	 */
	public Graph<CallGraphNode, CallGraphLink> getJungGraph() {
		return jungGraph;
	}

	public HashMap<String, CallGraphNode> getLabelsToVertices() {
		return labelsToVertices;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the handle
	 */
	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getPrimaryScore() {
		return primaryScore;
	}

	public void setPrimaryScore(String primaryScore) {
		this.primaryScore = primaryScore;
	}

	/**
	 * @return the defaultEdgeType
	 */
	public EdgeType getDefaultEdgeType() {
		return defaultEdgeType;
	}

	public void setDefaultEdgeType(EdgeType defaultEdgeType) {
		this.defaultEdgeType = defaultEdgeType;
	}

    /**
     * Returns an identifier for the graph - usually the Eclipse
     * handle, but possibly the name or the hash code.
     * @param callGraph the graph whose id is desired
     * @return the id;
     */
    public String getGraphId() {
		String graphId = getHandle();
		
		if (graphId == null || graphId.length() < 1) {
			graphId = getName();
		}
		if (graphId == null) {
			graphId = "" + hashCode();
		}
		return graphId;
    }

	public String getVertexLabel(CallGraphNode vertex) {
		String vertexLabel = vertexLabeler.transform(vertex);
		return vertexLabel;
	}

	public String getEdgeLabel(CallGraphLink edge) {
		Pair<CallGraphNode> vertices = jungGraph.getEndpoints(edge);
		CallGraphNode node1 = vertices.getFirst();
		CallGraphNode node2 = vertices.getSecond();
		String vertexLabel1 = vertexLabeler.transform(node1);
		String vertexLabel2 = vertexLabeler.transform(node2);
		String edgeLabel = vertexLabel1 + "<=>" + vertexLabel2;
		return edgeLabel;
	}

	// protected void transformVertices()
	// {
	// Collection<CallGraphNode> vertices = graph.getVertices();
	// Iterator<CallGraphNode> vIterator = vertices.iterator();
	// while (vIterator.hasNext())
	// {
	// CallGraphNode vertex = vIterator.next();
	// String vertexLabel = vertexLabeler.transform(vertex);
	// logger.fine("Vertex " + vertex + " has label " + vertexLabel);
	// }
	// }

	public static void setNodeTypes(Collection<CallGraphNode> vertices) {
		for (CallGraphNode v : vertices) {
			if (v.getLabel().endsWith(")")) {
				v.setNodeType(NodeType.METHOD);
			} else {
				v.setNodeType(NodeType.FIELD);
			}
		}
	}

	/**
	 * @return the vertexFactory
	 */
	public Factory<CallGraphNode> getVertexFactory() {
		return vertexFactory;
	}

	/**
	 * @return the edgeFactory
	 */
	public Factory<CallGraphLink> getEdgeFactory() {
		return edgeFactory;
	}

	/**
	 * This just returns the graph and is used only to fulfill the contract for
	 * GraphMLReader2.
	 * 
	 * @author Keith
	 * 
	 */
	public static class CallGraphMetadataTransformer implements
			Transformer<GraphMetadata, Graph<CallGraphNode, CallGraphLink>> {

		public Graph<CallGraphNode, CallGraphLink> transform(GraphMetadata arg0) {
			return new SparseMultigraph<CallGraphNode, CallGraphLink>();
		}
	} // class CallGraphMetadataTransformer

	public CallGraphNode getNode(String name) {
		return labelsToVertices.get(name);
	}

	public CallGraphLink findLink(CallGraphNode node1, CallGraphNode node2) {
		CallGraphLink link = jungGraph.findEdge(node1, node2);
		return link;
	}

	public List<CallGraphLink> getLinks(CallGraphNode node) {
		Collection<CallGraphLink> links = jungGraph.getIncidentEdges(node);
		return new ArrayList<CallGraphLink>(links);
	}

	public Pair<CallGraphNode> getEndpoints(CallGraphLink edge) {
		return jungGraph.getEndpoints(edge);
	}

	public List<CallGraphNode> getNodes() {
		Collection<CallGraphNode> vertices = jungGraph.getVertices();
		return new ArrayList<CallGraphNode>(vertices);
	}

	/**
	 * Add a node to the graph
	 * 
	 * @param node
	 *            the node to add
	 */
	public void addNode(CallGraphNode node) {
		jungGraph.addVertex(node);
		String label = node.getLabel();
		vertexLabeler.set(node, label);
		labelsToVertices.put(label, node);
	}

	/**
	 * Creates and adds a node to the graph
	 */
	public CallGraphNode createNode() {
		CallGraphNode node = createRawNode();
		addNode(node);
		return node;
	}

	/**
	 * Creates and adds a node to the graph
	 * 
	 * @param name
	 *            the name of the node to add
	 */
	public CallGraphNode createNode(String name) {
		CallGraphNode node = createRawNode();
		node.setLabel(name);
		node.setSimpleName(name);
		addNode(node);
		return node;
	}

	/**
	 * Creates a node without adding it to the graph
	 */
	private CallGraphNode createRawNode() {
		CallGraphNode node = null;

		if (vertexFactory == null) {
			node = new CallGraphNode();
		} else {
			node = vertexFactory.create();
		}
		return node;
	}

	public CallGraphLink createLink(CallGraphNode node1, CallGraphNode node2,
			EdgeType edgeType) {
		CallGraphLink link = null;

		if (edgeFactory == null) {
			link = new CallGraphLink();
		} else {
			link = edgeFactory.create();
		}
		link.setLabel(node1.getLabel() + "-" + node2.getLabel());
		jungGraph.addEdge(link, node1, node2, edgeType);
		return link;
	}

	public CallGraphLink createLink(CallGraphNode node1, CallGraphNode node2) {
		return createLink(node1, node2, defaultEdgeType);
	}

	public static JavaCallGraph toDirectedGraph(JavaCallGraph graphIn) {
		Graph<CallGraphNode, CallGraphLink> jungGraphIn = graphIn.getJungGraph();
		JavaCallGraph graphOut = null;
		if (jungGraphIn instanceof DirectedSparseMultigraph<?, ?>) {
			graphOut = graphIn;
		} else {
			graphOut = new JavaCallGraph();
			graphOut.jungGraph = new DirectedSparseMultigraph<CallGraphNode, CallGraphLink>();
			graphOut.setHandle(graphIn.getHandle());
			graphOut.setName(graphIn.getName());

			for (CallGraphNode node : graphIn.getNodes()) {
				graphOut.addNode(node);
			}
			
			// Add directed edges
			for (CallGraphNode node : graphIn.getNodes()) {
				Collection<CallGraphNode> successors =
					graphIn.jungGraph.getSuccessors(node);
				for (CallGraphNode successor : successors) {
					graphOut.createLink(node, successor, EdgeType.DIRECTED);
				}
			}
		}
		return graphOut;
	}

	public static JavaCallGraph toUndirectedGraph(JavaCallGraph graphIn) {
		EdgeType edgeType = graphIn.getDefaultEdgeType();
		
		JavaCallGraph graphOut = null;
		if (edgeType.equals(EdgeType.UNDIRECTED)) {
			graphOut = graphIn;
		} else {
			graphOut = new JavaCallGraph();
			graphOut.setDefaultEdgeType(EdgeType.UNDIRECTED);
			graphOut.setHandle(graphIn.getHandle());
			graphOut.setName(graphIn.getName());

			for (CallGraphNode node : graphIn.getNodes()) {
				graphOut.addNode(node);
			}
			for (CallGraphLink link : graphIn.getEdges()) {
				Pair<CallGraphNode> endpoints = graphIn.getEndpoints(link);
				graphOut.createLink(endpoints.getFirst(), endpoints.getSecond(),
						EdgeType.UNDIRECTED);
			}
		}
		return graphOut;
	}

	public Collection<CallGraphLink> getEdges() {
		return jungGraph.getEdges();
	}

	@Override
	public String toString() {
		String result = "JavaCallGraph [\ngraph=\n" + jungGraph;
		result += "\nlabelsToVertices=\n" + labelsToVertices;
		result += "\nvertexLabeler=\n" + vertexLabeler + "]";
		return result;
	}

	public Collection<CallGraphNode> getNeighbors(CallGraphNode node) {
		return jungGraph.getNeighbors(node);
	}

	/**
	 * Adds the provided call data to the graph.
	 * 
	 * @param callData
	 */
	public void processCallData(CallData callData) {
		IType type = EclipseUtils.getTypeFromHandle(handle);
		Set<IField> attributes = callData.getAttributes();
		createNodesForAttributes(attributes, type);
		Set<IMethod> methods = callData.getMethods();
		createNodesForMethods(methods, type);
		
		Map<IMethod, HashSet<IField>> attributesAccessedMap =
			callData.getAttributesAccessedMap();
		createLinksToAttributes(attributesAccessedMap);
		
		Map<IMethod, HashSet<IMethod>> methodsCalledMap =
			callData.getMethodsCalledMap();
		createLinksToMethods(methodsCalledMap);
		System.out.println(this.toString());
	}

	protected void createLinksToMethods(
			Map<IMethod, HashSet<IMethod>> methodsCalledMap) {
		Set<Entry<IMethod, HashSet<IMethod>>> methodEntries =
			methodsCalledMap.entrySet();
		for (Entry<IMethod, HashSet<IMethod>> entry : methodEntries) {
			IMethod method = entry.getKey();
			String methodHandle = method.getHandleIdentifier();
			CallGraphNode methodNode = labelsToVertices.get(methodHandle);

			// May get nulls when methods are removed from the graph, e.g.
			// constructors
			if (methodNode == null) {
				System.err.println("No method found for " + methodHandle);
			} else {
				HashSet<IMethod> calledMethods = entry.getValue();
				for (IMethod calledMethod : calledMethods) {
					String calledMethodHandle = calledMethod.getHandleIdentifier();
					CallGraphNode calledMethodNode = labelsToVertices.get(calledMethodHandle);
					if (calledMethodNode == null) {
						System.err.println("No calledMethod found for " + calledMethodHandle);
					} else {
						createLink(methodNode, calledMethodNode);
					}
				} // for calledMethods
			}
		} // for methodEntries
	}

	protected void createLinksToAttributes(
			Map<IMethod, HashSet<IField>> attributesAccessedMap) {
		Set<Entry<IMethod, HashSet<IField>>> entries =
			attributesAccessedMap.entrySet();
		for (Entry<IMethod, HashSet<IField>> entry : entries) {
			IMethod method = entry.getKey();
			String methodHandle = method.getHandleIdentifier();
			CallGraphNode methodNode = labelsToVertices.get(methodHandle);

			// May get nulls when methods are removed from the graph, e.g.
			// constructors
			if (methodNode == null) {
				System.err.println("No method found for " + methodHandle);
			} else {
				HashSet<IField> iFields = entry.getValue();
				for (IField iField : iFields) {
					String fieldHandle = iField.getHandleIdentifier();
					CallGraphNode fieldNode = labelsToVertices.get(fieldHandle);
					if (fieldNode == null) {
						System.err.println("No field found for " + fieldHandle);
					} else {
						createLink(methodNode, fieldNode);
					}
				} // for fields
			}
		} // for entries
	}

	protected void createNodesForMethods(Collection<IMethod> methods,
			IType type) {
		for (IMethod method : methods) {
			try {
				String methodHandle = method.getHandleIdentifier();
				int flags = method.getFlags();
				if ((includeObjectMethods || !EclipseUtils.isRedefinedObjectMethod(methodHandle))
						&& (includeConstructors || !method.isConstructor())
						&& (includeStatics || !Flags.isStatic(flags))
						) {
					CallGraphNode node = createNode(methodHandle);
					node.setSimpleName(method.getElementName());
					node.setNodeType(NodeType.METHOD);
					try {
						node.setMemberFlags(method.getFlags());
						// node.setInherited(type != null
						// && !type.equals(method.getDeclaringType()));
						IType declaringType = method.getDeclaringType();
						if (type != null && declaringType != null) {
							if (EclipseSearchUtils.hasSupertype(type,
									declaringType)) {
								node.setInherited(true);
							} else if (!type.equals(declaringType)) {
								node.setInner(true);
							}
						}
					} catch (JavaModelException e) {
						// ignore
					}
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void createNodesForAttributes(Collection<IField> attributes,
			IType type) {
		// Create a node for each attribute
		for (IField attribute : attributes) {
			String handle = attribute.getHandleIdentifier();
			try {
				String className = attribute.getTypeSignature();
				//getDeclaringType().getFullyQualifiedName();
				int flags = attribute.getFlags();
				if ((includeStatics || !Flags.isStatic(flags))
						&& (includeLoggers || (className.indexOf("Logger") < 0))) {
					CallGraphNode node = createNode(handle);
					node.setSimpleName(attribute.getElementName());
					node.setNodeType(NodeType.FIELD);
					node.setMemberFlags(flags);
					IType declaringType = attribute.getDeclaringType();

					if (type != null && declaringType != null) {
						if (EclipseSearchUtils
								.hasSupertype(type, declaringType)) {
							node.setInherited(true);
						} else if (!type.equals(declaringType)) {
							node.setInner(true);
						}
					}
				}
			} catch (JavaModelException e) {
				// ignore
			}
		}
	}
	
	/**
	 * Returns the methods that are required by an interface or an ancestor.
	 * @param thisType the class being examined
	 * @return a cluster of inherited methods, or null when there are none
	 * @throws JavaModelException
	 */
	public CallGraphCluster getRequiredMethods(IType thisType,
			boolean includeObjectMethods)
			throws JavaModelException {
		CallGraphCluster requiredMethodCluster = null;
		IMethod[] theseMethods = thisType.getMethods();
		HashSet<CallGraphNode> requiredMethods = new HashSet<CallGraphNode>();

		IType[] supertypes = getSupertypes();
		HashSet<IMethod> allSuperMethods = new HashSet<IMethod>();
		
		for (IType supertype : supertypes) {
			if (includeObjectMethods
					|| !"Object".equals(supertype.getElementName())) {
				IMethod[] methods = supertype.getMethods();
				List<IMethod> superMethods = Arrays.asList(methods);
				allSuperMethods.addAll(superMethods);
			}
		}
		
		// Check each method to see if it is declared elsewhere
		for (IMethod method : theseMethods) {
			for (IMethod superMethod : allSuperMethods) {
				if (method.isSimilar(superMethod)) {
					CallGraphNode node =
						labelsToVertices.get(method.getHandleIdentifier());
					
					if (node != null) {
						requiredMethods.add(node);
					}
					break;
				}
			}
		}
		if (!requiredMethods.isEmpty()) {
			requiredMethodCluster = new CallGraphCluster(requiredMethods);
			requiredMethodCluster.setNodeType(NodeType.REQUIRED_METHOD_CLUSTER);
		}
		return requiredMethodCluster;
	}

	/**
	 * Gets the supertypes of the specified type, in bottom up order.
	 * @param type
	 * @return the supertypes
	 * @throws JavaModelException
	 */
	public IType[] getSupertypes() throws JavaModelException {
		if (supertypes == null) {
			IType type = EclipseUtils.getTypeFromHandle(handle);
			ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);
			supertypes = typeHierarchy.getAllSupertypes(type);
		}
		return supertypes;
	}
	
	/**
	 * Removed the nodes with the provided names from the graph
	 * 
	 * @param membersToIgnore
	 *            a collection of node names
	 */
	public void removeFromGraph(Object[] membersToIgnore) {
		if (jungGraph != null) {
			for (Object obj : membersToIgnore) {
				CallGraphNode node = labelsToVertices.get(obj.toString());
				if (node != null) {
					jungGraph.removeVertex(node);
				}
			} // for
		}
	}


	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Removes methods from the graph that were inherited from Object
	 * (clone, equals, hashCode, toString)
	 */
	void removeObjectsMethods() {
		Graph<CallGraphNode, CallGraphLink> jungGraph = getJungGraph();
		Collection<CallGraphNode> vertices = jungGraph.getVertices();
		ArrayList<CallGraphNode> toRemove = new ArrayList<CallGraphNode>();
	
		for (CallGraphNode node : vertices) {
			String label = node.getLabel();
			
			if (EclipseUtils.isRedefinedObjectMethod(label)) {
				toRemove.add(node);
			}
		}
		
		for (CallGraphNode node : toRemove) {
			jungGraph.removeVertex(node);
		}
	}

	/**
	 * Modify the call graph based on the parameters
	 * @param oldGraph the original graph, before the parameters
	 *     were applied
	 * @return the new graph, after the parameters were applied
	 * @throws JavaModelException 
	 */
	public JavaCallGraph getAltGraphUsingParams()
	throws JavaModelException {
		//TODO handle inherited members
		getParameters();
		String handle = getHandle();
		JavaCallGraph newGraph = new JavaCallGraph(handle, defaultEdgeType);
		
		if (condenseImposed) {
			IJavaElement element = JavaCore.create(handle);
			if (element instanceof IType) {
				IType type = (IType) element;
				// Remove Object's methods beforehand, so they don't get
				// put into the clustered node
				if (!includeObjectMethods) {
					removeObjectsMethods();
				}
				newGraph = GraphCondenser.condenseRequiredMethods(newGraph,
						type, condenseObjectsMethods);
				//TODO handle inherited
			}
		}		return newGraph;
	}

}
