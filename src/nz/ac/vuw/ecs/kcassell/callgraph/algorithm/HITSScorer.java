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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.ScoreType;
import nz.ac.vuw.ecs.kcassell.callgraph.io.CallGraphReader;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.algorithms.scoring.HITS.Scores;
import edu.uci.ics.jung.graph.Graph;

public class HITSScorer
{
    private static final long serialVersionUID = 1L;

    private static final UtilLogger logger =
    	new UtilLogger("HITSScorer");
    
    protected String graphFile = null; //ThesisConstants.WORKSPACE_ROOT + "datasets/basetests/personCarAsym.net";
    protected String metricsFile = null; //ThesisConstants.WORKSPACE_ROOT + "datasets/basetests/personCarAsym.net";

    /** @param dataFile the file containing call graph info. */
    public void setGraphFile(String dataFile)
    {
        this.graphFile = dataFile;
    }

    /** @param metricsFile the file containing method metric data */
    public void setMetricsFile(String metricsFile)
    {
        this.metricsFile = metricsFile;
    }

    /**
     * Order the vertices such that those with the highest hub score appear first.
     * @param vertices the graph nodes
     * @param hitsAlgo the scoring algorithm
     * @return a sorted list of CallGraphNodes, highest hub scores first
     */
    public List<CallGraphNode> orderHubs(
            Collection<CallGraphNode> vertices,
            final HITS<CallGraphNode, CallGraphLink> hitsAlgo)
    {
        ArrayList<CallGraphNode> orderedHubs = new ArrayList<CallGraphNode>(vertices);
        
        Comparator<CallGraphNode> hubComparator = new Comparator<CallGraphNode>()
        {
            public int compare(CallGraphNode node1, CallGraphNode node2)
            {
                int result = 0;
                Scores score1 = hitsAlgo.getVertexScore(node1);
                Scores score2 = hitsAlgo.getVertexScore(node2);
                result = Double.compare(score1.hub, score2.hub) * -1; // highest first
                if (result == 0)
                {
                    String label1 = node1.getLabel();
                    String label2 = node2.getLabel();
                    if (label1 != null)
                    {
                        result = label1.compareTo(label2);
                    }
                }
                return result;
            }
        };
        Collections.sort(orderedHubs, hubComparator);
        return orderedHubs;
    }

    /**
     * Order the vertices such that those with the highest authority score appear first.
     * @param vertices the graph nodes
     * @param hitsAlgo the scoring algorithm
     * @return a sorted list of CallGraphNodes, highest authority scores first
     */
    public List<CallGraphNode> orderAuthorities(
            Collection<CallGraphNode> vertices,
            final HITS<CallGraphNode, CallGraphLink> hitsAlgo)
    {
        ArrayList<CallGraphNode> orderedAuthorities = new ArrayList<CallGraphNode>(vertices);
        
        Comparator<CallGraphNode> authorityComparator = new Comparator<CallGraphNode>()
        {
            public int compare(CallGraphNode node1, CallGraphNode node2)
            {
                int result = 0;
                Scores score1 = hitsAlgo.getVertexScore(node1);
                Scores score2 = hitsAlgo.getVertexScore(node2);
                result = Double.compare(score1.authority, score2.authority) * -1; // highest first
                if (result == 0)
                {
                    String label1 = node1.getLabel();
                    String label2 = node2.getLabel();
                    if (label1 != null)
                    {
                        result = label1.compareTo(label2);
                    }
                }
                return result;
            }
        };
        Collections.sort(orderedAuthorities, authorityComparator);
        return orderedAuthorities;
    }

    /**
     * 
     * @param args accepts two kinds of arguments:
     *   -metricsFile <fileName>
     *      indicates the file with metric data for the methods
     *   -graphFile <fileName>
     *      indicates the PajekNet data file
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        HITSScorer scorer = new HITSScorer();
        processCommandLineArgs(args, scorer);
        Factory<CallGraphNode> vertexFactory = new CallGraphNode.CallGraphNodeFactory();
        Factory<CallGraphLink> edgeFactory = new CallGraphLink.CallGraphLinkFactory();
        CallGraphReader graphReader =
        	new CallGraphReader(vertexFactory, edgeFactory);
        JavaCallGraph javaCallGraph = graphReader.readPajekNetGraph(scorer.graphFile);
        Graph<CallGraphNode,CallGraphLink> jungGraph = javaCallGraph.getJungGraph();
        scorer.assignHITSScores(jungGraph);
    }

    
    public void assignHITSScores(
            Graph<CallGraphNode, CallGraphLink> graph)
    {
        HITS<CallGraphNode, CallGraphLink> hitsAlgo =
            new HITS<CallGraphNode, CallGraphLink>(graph);
        hitsAlgo.evaluate();
        
        Collection<CallGraphNode> vertices = graph.getVertices();
        
        logger.fine("Label^authority^hub");
        for (CallGraphNode vertex : vertices)
        {
            Scores score = hitsAlgo.getVertexScore(vertex);
            vertex.setScore(ScoreType.AUTHORITY, score.authority);
            vertex.setScore(ScoreType.HUB, score.hub);
            String SEP = "^";
            logger.fine(vertex.getLabel() + SEP + score.authority + SEP + score.hub);
        }
        
        List<CallGraphNode> orderedHubs = orderHubs(vertices, hitsAlgo);
        logger.fine("Ordered hubs = " + orderedHubs);
        List<CallGraphNode> orderedAuths = orderAuthorities(vertices, hitsAlgo);
        logger.fine("Ordered authorities = " + orderedAuths);
    }


    private static void processCommandLineArgs(String[] args, HITSScorer demo)
    {
        for (int i = 0; i < args.length; i++)
        {
            if ("-metricsFile".equals(args[i]) && (args.length > i + 1))
            {
                demo.setMetricsFile(args[i+1]);
            }
            else if ("-graphFile".equals(args[i]) && (args.length > i + 1))
            {
                demo.setGraphFile(args[i+1]);
            }
        }
    }


}
