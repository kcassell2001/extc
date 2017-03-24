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

package nz.ac.vuw.ecs.kcassell.callgraph.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.util.MapSettableTransformer;
import edu.uci.ics.jung.algorithms.util.SettableTransformer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;

/**
 * This class can read a graph from a file.
 * 
 * @author Keith
 * 
 */
public class CallGraphReader {
	protected Graph<CallGraphNode, CallGraphLink> graph = null;

	/** Keeps track of the correspondence of labels to vertices. */
	HashMap<String, CallGraphNode> labelsToVertices = new HashMap<String, CallGraphNode>();

    private static final UtilLogger logger =
    	new UtilLogger("CallGraphReader");

	Factory<CallGraphNode> vertexFactory = null;
	Factory<CallGraphLink> edgeFactory = null;

	/**
	 * This keeps track of the correspondence between a vertex and its label.
	 */
	protected SettableTransformer<CallGraphNode, String> vertexLabeler = null;

	/**
	 * @param vertexFactory
	 * @param edgeFactory
	 */
	public CallGraphReader(Factory<CallGraphNode> vertexFactory,
			Factory<CallGraphLink> edgeFactory) {
		super();
		this.vertexFactory = vertexFactory;
		this.edgeFactory = edgeFactory;
	}

	public Graph<CallGraphNode, CallGraphLink> getGraph() {
		return graph;
	}

	/**
	 * @return the labelsToVertices
	 */
	public HashMap<String, CallGraphNode> getLabelsToVertices() {
		return labelsToVertices;
	}

	/**
	 * 
	 * @param args
	 *            the name of a file or directory
	 * @return a collection of files to be read
	 */
	public Vector<File> getPajekDataFiles(String[] args) {
		Vector<File> files = new Vector<File>();
		String dataSource = null;
		if (args.length > 0) {
			dataSource = args[0];
		}
		logger.info("input data source = " + dataSource);

		File fileOrDir = new File(dataSource);
		if (fileOrDir.exists()) {
			if (fileOrDir.isDirectory()) {
				PajekNetFilenameFilter filter = new PajekNetFilenameFilter();
				String[] fileList = fileOrDir.list(filter);

				for (String name : fileList) {
					files.add(new File(fileOrDir, name));
				}
			} else // single file
			{
				files.add(fileOrDir);
			}
		}
		logger.info("Files to process: " + files);
		return files;
	}

	/**
	 * Reads a graph stored in Pajek file format from the specified file.
	 * 
	 * @param dataFile
	 *            the name of the file to be read. <code>
	 * File Data Format
	 * 
	 * The file format accepted by Pajek provides information on vertices, arcs
	 * (directed edges), and undirected edges. A short example showing the file
	 * format is given below:
	 *  -------------------------------------
	 * *Vertices 3 
	 *  1 "Doc1" 0.0 0.0 0.0 ic Green bc Brown 
	 *  2 "Doc2" 0.0 0.0 0.0 ic Green bc Brown 
	 *  3 "Doc3" 0.0 0.0 0.0 ic Green bc Brown
	 * *Arcs
	 *  1 2 3 c Green 
	 *  2 3 5 c Black
	 * *Edges
	 *  1 3 4 c Green
	 *  ------------------------------------- 
	 * In the
	 * example there are 3 vertices Doc1, Doc2 and Doc3 denoted by numbers 1, 2
	 * and 3. The (fill) color of these nodes is Green and the border color is
	 * Brown. The initial layout location of the nodes is (0,0,0). Note that the
	 * (x,y,z) values can be changed interactively after drawing.
	 * 
	 * There are two arcs (directed edges). The first goes from node 1 (Doc1) to
	 * node 2 (Doc2) with a weight of 3 and in color Green.
	 * 
	 * For edges, there is one from node 1 (Doc1) to node 3 (Doc3) of weight of
	 * 4, and is colored green.
     * </code>
	 * @return a graph
	 */
	public JavaCallGraph readPajekNetGraph(String dataFile) throws IOException {
		BufferedReader bufferedReader = getBufferedReader(dataFile);
		JavaCallGraph callGraph = readPajekNetGraph(bufferedReader);
		return callGraph;
	}

	/**
	 * @param bufferedReader
	 */
	public JavaCallGraph readPajekNetGraph(BufferedReader bufferedReader)
			throws IOException {
		graph = new SparseMultigraph<CallGraphNode, CallGraphLink>();
		PajekNetReader<Graph<CallGraphNode, CallGraphLink>, CallGraphNode, CallGraphLink> pajekReader = new PajekNetReader<Graph<CallGraphNode, CallGraphLink>, CallGraphNode, CallGraphLink>(
				vertexFactory, edgeFactory);
		CallGraphLink.SettableLinkWeightTransformer linkWeightTransformer =
			new CallGraphLink.SettableLinkWeightTransformer();
		pajekReader.setEdgeWeightTransformer(linkWeightTransformer);
		// pajekReader.setVertexLabeller(vertex_labels)
		pajekReader.load(bufferedReader, graph);
		vertexLabeler = pajekReader.getVertexLabeller();

		transformVertices();
		logEdges();
		JavaCallGraph callGraph =
			new JavaCallGraph(graph, labelsToVertices, vertexLabeler);
		return callGraph;
	}

	public JavaCallGraph readGraphMLGraph(String fileName) throws IOException {
		BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
		graph = new SparseMultigraph<CallGraphNode, CallGraphLink>();
		// GraphMLReader<Graph<CallGraphNode, CallGraphLink>, CallGraphNode,
		// CallGraphLink> graphMLreader;
		try {
			// graphMLreader = new GraphMLReader<Graph<CallGraphNode,
			// CallGraphLink>, CallGraphNode, CallGraphLink>(vertexFactory,
			// edgeFactory);
			// graphMLreader.load(fileReader, graph);
			GraphMLReader2<Graph<CallGraphNode, CallGraphLink>, CallGraphNode, CallGraphLink> graphMLreader = new GraphMLReader2<Graph<CallGraphNode, CallGraphLink>, CallGraphNode, CallGraphLink>(
					fileReader,
					new JavaCallGraph.CallGraphMetadataTransformer(),
					new CallGraphNode.GraphMLNodeMetadataReader(),
					new CallGraphLink.EdgeMetadataTransformer(),
					new CallGraphLink.HyperEdgeMetadataTransformer());
			graph = graphMLreader.readGraph();
		} catch (Exception e) {
			String msg = "Troubles creating GraphMLReader";
			logger.warning(msg + ": " + e);
			throw new IOException(msg);
		}
		Map<CallGraphNode, String> map = new HashMap<CallGraphNode, String>();
		vertexLabeler = new MapSettableTransformer<CallGraphNode, String>(map);
		labelsToVertices.clear();

		Collection<CallGraphNode> vertices = graph.getVertices();
		for (CallGraphNode node : vertices) {
			String label = node.getLabel();
			labelsToVertices.put(label, node);
			vertexLabeler.set(node, label);
		}
		JavaCallGraph callGraph = new JavaCallGraph(graph, labelsToVertices,
				vertexLabeler);
		return callGraph;
	}

	/**
	 * @param fileName
	 *            the location of the data file
	 * @return a reader for the data file
	 * @throws FileNotFoundException
	 */
	private BufferedReader getBufferedReader(String fileName)
			throws FileNotFoundException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				fileName);
		if (is == null) {
			is = new FileInputStream(fileName);
		}
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(is));
		return bufferedReader;
	}

	public String getVertexLabel(CallGraphNode vertex) {
		String vertexLabel = vertexLabeler.transform(vertex);
		return vertexLabel;
	}

	protected String getEdgeLabel(CallGraphLink edge) {
		Pair<CallGraphNode> vertices = graph.getEndpoints(edge);
		CallGraphNode node1 = vertices.getFirst();
		CallGraphNode node2 = vertices.getSecond();
		String vertexLabel1 = vertexLabeler.transform(node1);
		String vertexLabel2 = vertexLabeler.transform(node2);
		String edgeLabel = vertexLabel1 + "<=>" + vertexLabel2;
		return edgeLabel;
	}

	public SettableTransformer<CallGraphNode, String> getVertexLabeler() {
		return vertexLabeler;
	}

	/**
	 * Assign the node names in the file to the node.simpleName
	 * @return a map of names to nodes
	 */
	private HashMap<String, CallGraphNode> transformVertices() {
		Collection<CallGraphNode> vertices = graph.getVertices();
		for (CallGraphNode vertex : vertices) {
			String vertexLabel = vertexLabeler.transform(vertex);
			vertex.setSimpleName(vertexLabel);
			labelsToVertices.put(vertexLabel, vertex);
			logger.finer("Vertex " + vertex + " has label " + vertexLabel);
		}
		return labelsToVertices;
	}

	private void logEdges() {
		Collection<CallGraphLink> edges = graph.getEdges();
		Iterator<CallGraphLink> eIterator = edges.iterator();
		CallGraphLink edge = null;

		while (eIterator.hasNext()) {
			edge = eIterator.next();
			String edgeLabel = getEdgeLabel(edge);
			logger.finer("[" + edgeLabel + "], ");
		}
	}

}
