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

package nz.ac.vuw.ecs.kcassell.utils;

/**
 * This interface defines constants for use in interacting with
 * parameters affecting program behavior.  Many of these are also
 * keys in the property file (extc.properties), so these need to
 * consistent.
 * @author Keith
 */
public interface ParameterConstants {


	/** The key for determining how many total clusters should
	 * be created via agglomeration.	 */
	public static final String AGGLOMERATION_CLUSTERS_KEY =
		"agglomerationClusters";

	/** The key for determining the distance calculator to
	    use in clustering. */
	public static final String CALCULATOR_KEY = "distanceCalculator";

	/** The key for determining the distance calculator to
    use in clustering. */
	public static final String CLUSTERER_KEY = "clusterer";

	/** The key for determining the dendrogram format to
    use for displaying the results of agglomerative clustering. */
	public static final String CLUSTER_TEXT_FORMAT_KEY = "clusterTextFormat";

	/** The key for determining the edge type - directed or undirected. */
	public static final String EDGE_TYPE_KEY = "edgeType";
	
	/* The key for retrieving the parameter specifying the graph layout. See
	 * nz.ac.vuw.ecs.kcassell.callgraph.gui.GraphLayoutEnum for acceptable
	 * values.   */
	public static final String GRAPH_LAYOUT_KEY = "graphLayout";
	
	/** The key for retrieving the list of parts of identifiers
	   that shouldn't be considered properties. */
	public static final String IDENTIFIER_PARTS_TO_IGNORE_KEY =
		"identifierPartsToIgnore";
	
	/** The key for determining the group linkage to
    use in agglomerative clustering. */
	public static final String LINKAGE_KEY = "groupLinkage";

	/** The key for determining the max size threshold for the number of members
	 * in a class.  */
	public static final String MAX_MEMBERS_KEY = "maxMembers";
	
	/** The key for determining how many new betweenness clusters should
	 * be created.	 */
	public static final String NEW_BETWEENNESS_CLUSTERS_KEY =
		"newBetweennessClusters";
	
	/** The key for retrieving the parameter specifying how graph
	 * nodes should be sized.
	 * See nz.ac.vuw.ecs.kcassell.callgraph.ScoreType for acceptable values */
	public static final String NODE_SIZING_KEY = "nodeSizing";
	
	
	////////// The keys for retrieving the parameters specifying which nodes
	////////// should be condensed into a single cluster node.
	
	/** Key - should inherited methods be condensed?  NOTE:
	  CONDENSE_OBJECT_METHODS_KEY overrides this for methods defined on Object */
//	public static final String CONDENSE_INHERITED_KEY = "condenseInherited";

	/** Key - should methods inherited from Object be condensed? */
	public static final String CONDENSE_OBJECTS_METHODS_KEY = "condenseObjectMethods";

	/** Key - should methods specified by interfaces and superclasses be
	 * condensed? NOTE: CONDENSE_OBJECT_METHODS_KEY overrides this for methods
	 * defined on Object.  */
	public static final String CONDENSE_IMPOSED_METHODS_KEY = "condenseInterfaces";

	/** Key - should methods involved in recursive cycles be condensed? */
	public static final String CONDENSE_RECURSIVE_CYCLES_KEY = "condenseRecursiveCycles";

	/** Key - should constructors be shown in the graph? */
	public static final String INCLUDE_CONSTRUCTORS_KEY = "includeConstructors";
	
	/** Key - should inherited members be shown in the graph? */
	public static final String INCLUDE_INHERITED_KEY = "includeInherited";
	
	/** Key - should inner class members be shown in the graph? */
	public static final String INCLUDE_INNERS_KEY = "includeInners";
	
	/** Key - should fields of type java.util.Logger be shown in the graph? */
	public static final String INCLUDE_LOGGERS_KEY = "includeLogger";
	
	/** Key - should Object's members be shown in the graph? */
	public static final String INCLUDE_OBJECT_METHODS_KEY = "includeObjectMethods";
	
	/** Key - should static members be shown in the graph? */
	public static final String INCLUDE_STATICS_KEY = "includeStatic";
	
	//////////// Graph colors
	
	/** The key for determining the default color for connected edges. */
	public static final String CONNECTED_EDGE_COLOR_KEY = "connectedEdgeColor";
	
	/** The key for determining the default color for deleted edges. */
	public static final String DELETED_EDGE_COLOR_KEY = "deletedEdgeColor";
	
	/** The key for determining the default color for field nodes. */
	public static final String FIELD_COLOR_KEY = "fieldColor";

	/** The key for determining the default color for method nodes. */
	public static final String METHOD_COLOR_KEY = "methodColor";

	/** The key for determining the default color for picked nodes. */
	public static final String PICKED_COLOR_KEY = "pickedColor";
	
	/** The key for determining the default color for private members. */
	public static final String PRIVATE_COLOR_KEY = "privateColor";
	
	/** The key for determining the default color for protected members. */
	public static final String PROTECTED_COLOR_KEY = "protectedColor";
	
	/** The key for determining the default color for package default members. */
	public static final String PACKAGE_DEFAULT_COLOR_KEY = "packageDefaultColor";
	
	/** The key for determining the default color for public members. */
	public static final String PUBLIC_COLOR_KEY = "publicColor";
	
	/** The key for determining the default color for unpicked nodes. */
	public static final String UNPICKED_COLOR_KEY = "unpickedColor";

}