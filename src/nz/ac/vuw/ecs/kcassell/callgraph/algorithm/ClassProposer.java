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

package nz.ac.vuw.ecs.kcassell.callgraph.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.VertexLabeler;
import nz.ac.vuw.ecs.kcassell.callgraph.io.CallGraphReader;
import nz.ac.vuw.ecs.kcassell.callgraph.io.GraphReader;
import nz.ac.vuw.ecs.kcassell.cluster.BetweennessClusterer;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import edu.uci.ics.jung.graph.Graph;

public class ClassProposer
{
    protected static final UtilLogger logger =
        new UtilLogger("ClassProposer");
    
    /** The number of new classes to recommend. */
    protected int numToCreate = 4;
    
    /** The object that will write out the results as tabular data.
     * The values will be separated by the FIELD_SEPARATOR.   */
    protected PrintWriter output = null;

    /** The string that serves to separate the values for a given class. */
    public static final String FIELD_SEPARATOR = "^";

    /** The string that serves to separate the size values for multiple clusters. */
    public static final String SIZE_SEPARATOR = ", ";
    

    /**
     * @param numToCreate the number of new classes to recommend
     */
    public void setNumToCreate(int numToCreate)
    {
        this.numToCreate = numToCreate;
    }

    protected void printHeaderLine(PrintWriter output)
    {
        output.print("Class");
        output.print(FIELD_SEPARATOR);
        output.print("#nodes");
        output.print(FIELD_SEPARATOR);
        output.print("#edges");
        output.print(FIELD_SEPARATOR);
        output.print("t0 szs");
        
        for (int i = 1; i <= numToCreate; i++)
        {
            output.print(FIELD_SEPARATOR);
            output.print("-edges" + i); // #edges removed
            output.print(FIELD_SEPARATOR);
            output.print("t" + i + " szs"); // size of the clusters
        }
        output.println();
    }

    /** Creates a PrintWriter for writing the results.   */
    protected PrintWriter setUpOutput(String resultsFileName)
    {
        try
        {
            File outFile = new File(resultsFileName);
            FileWriter fileWriter = new FileWriter(outFile);
            output = new PrintWriter(new BufferedWriter(fileWriter));
            System.out.println("output will go to " + outFile.getAbsolutePath());
        }
        catch (IOException e1)
        {
            logger.severe("Error opening results file " 
                    + resultsFileName + ":" + e1);
            e1.printStackTrace();
            System.exit(0);
        }
        return output;
    }


    protected void detectClusters(JavaCallGraph callGraph)
    {
        Graph<CallGraphNode, CallGraphLink> jungGraph = callGraph.getJungGraph();
        BetweennessClusterer clusterer = new BetweennessClusterer(callGraph);
        int numEdgesToRemove = 0;
        Collection<CallGraphNode> clusters = clusterer.cluster(numEdgesToRemove);
        logger.info("Original clusters = " + clusters);
        int origClusterCount = clusters.size();
        int lastClusterCount = origClusterCount;
        printClusterSizes(clusters);

        while ((lastClusterCount < origClusterCount + numToCreate)
                && (numEdgesToRemove < jungGraph.getEdgeCount()))
        {
            numEdgesToRemove++;
            clusters = clusterer.cluster(numEdgesToRemove);
            
            // When a new cluster is produced, update the output
            if (clusters.size() > lastClusterCount)
            {
                lastClusterCount = clusters.size();
                output.print(FIELD_SEPARATOR);
                output.print(numEdgesToRemove); // #edges removed
                output.print(FIELD_SEPARATOR);
                printClusterSizes(clusters); // size of the clusters
            }
        }
        logger.info("number of edges removed =\n" + numEdgesToRemove);
        List<CallGraphLink> edgesRemoved = clusterer.getEdgesRemoved();
        String edgesRemovedString =
            clusterer.edgesRemovedToString(jungGraph, edgesRemoved);
        logger.info("edgesRemoved =\n" + edgesRemovedString);
        logger.info("Clusters =\n" + clusters);
    }

    /** Print the number of nodes in each cluster. */
    protected void printClusterSizes(Collection<CallGraphNode> nodes)
    {
    	List<Integer> sizes = CallGraphCluster.getClusterSizes(nodes);
    	Collections.sort(sizes);
//        buf.delete(buf.lastIndexOf(SIZE_SEPARATOR), buf.length() - 1);
        output.print(sizes.toString());
    }

	public static void main(String[] args)
    {
        ClassProposer proposer = new ClassProposer();
        String resultsFileName = "ClassProposerResults" + System.currentTimeMillis();
        PrintWriter output = proposer.setUpOutput(resultsFileName);
        proposer.printHeaderLine(output);
        CallGraphNode.CallGraphNodeFactory vertexFactory =
        	new CallGraphNode.CallGraphNodeFactory();
        CallGraphLink.CallGraphLinkFactory edgeFactory =
        	new CallGraphLink.CallGraphLinkFactory();
        CallGraphReader reader = new CallGraphReader(vertexFactory, edgeFactory);
        
        for (String dataFile : args)
        {
            try
            {
            	JavaCallGraph callGraph = reader.readPajekNetGraph(dataFile);
                proposer.detectClusters(callGraph);
                // calculateBarycentricScores(callGraph);
            }
            catch (IOException e)
            {
                ClassProposer.logger.severe("Error reading " + dataFile + ":" + e);
            }
            output.println();
        }   //for
        output.flush();
    }

	protected Graph<CallGraphNode, CallGraphLink> getGraphFromDataFile(
			PrintWriter output,
			GraphReader<CallGraphNode, CallGraphLink> graphReader,
			File dataFile)
			throws FileNotFoundException {
		Graph<CallGraphNode, CallGraphLink> jungGraph;
		System.out.println("Reading " + dataFile);
		logger.info("Processing Pajek net file: " + dataFile);
		printClassName(output, dataFile);
		readInputFile(graphReader, dataFile);
		jungGraph = graphReader.getGraph();
		output.print(jungGraph.getVertexCount()); output.print(FIELD_SEPARATOR);
		output.print(jungGraph.getEdgeCount()); output.print(FIELD_SEPARATOR);
        Collection<CallGraphNode> vertices = jungGraph.getVertices();
        VertexLabeler.relabelCallGraphNodes(graphReader, vertices);
		return jungGraph;
	}

    /**
     * Prints the class name (actually the input file's base).
     * @param output
     * @param dataFile
     */
    protected static void printClassName(PrintWriter output, File dataFile)
    {
        output.print(dataFile.getName().replaceFirst(".net", ""));
        output.print(FIELD_SEPARATOR);
    }

    /**
     * @param graphReader
     * @param dataFile
     * @throws FileNotFoundException
     */
    protected static void readInputFile(
            GraphReader<CallGraphNode, CallGraphLink> graphReader, File dataFile)
            throws FileNotFoundException
    {
        InputStream stream = new FileInputStream(dataFile);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(stream));
        graphReader.readPajekNetGraph(bufferedReader);
    }


}
