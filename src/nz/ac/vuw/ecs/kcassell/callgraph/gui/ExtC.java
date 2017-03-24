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

package nz.ac.vuw.ecs.kcassell.callgraph.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.callgraph.algorithm.CycleCalculator;
import nz.ac.vuw.ecs.kcassell.callgraph.io.CallGraphReader;
import nz.ac.vuw.ecs.kcassell.callgraph.io.CallGraphWriter;
import nz.ac.vuw.ecs.kcassell.callgraph.io.GraphFileFilter;
import nz.ac.vuw.ecs.kcassell.callgraph.io.PajekNetFilenameFilter;
import nz.ac.vuw.ecs.kcassell.cluster.BetweennessClusterer;
import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth.FrequentMethodsMiner;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.similarity.ClustererEnum;
import nz.ac.vuw.ecs.kcassell.similarity.IntraClassDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.apache.commons.collections15.Factory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class ExtC
implements ChangeListener, ParameterConstants, RefactoringConstants {
	private static final int AGGLOMERATION_TAB_INDEX = 4;
	private static final int AGG_CLUSTERING_TAB_INDEX = 3;
	private static final int BETWEENNESS_TAB_INDEX = 2;
	private static final int GRAPH_TAB_INDEX = 1;
	private static final int METRICS_TAB_INDEX = 0;
	private static final int SPANNING_FOREST_TAB_INDEX = 5;


	/** Maintains various parameters for the application. */
	protected ApplicationParameters applicationParameters = null;

	/**
	 * Records last directory used, so we can use that as the default starting
	 * point next time
	 */
	private static String lastDirAccessed = PROJECT_ROOT;

	/** The GUI frame */
	protected JFrame frame = new JFrame();

	/** The graph view */
	protected GraphView graphView = null;

	/** The view for batch output. */
	protected BatchOutputView batchOutputView = null;

	/** The view for edge betweenness clustering. */
	protected ClusteringView betweennessView = null;

	/** The view for agglomerative clustering. */
	protected ClusteringView aggClusteringView = null;
	protected AgglomerationView agglomerationView = null;

	/** The view for agglomerative clustering. */
	protected SpanningForestView spanningForestView = null;

	/** Indicates whether constructors should be included in the graph. */
	protected boolean includeConstructors = false;

	/** The main panel in the GUI */
	protected JTabbedPane mainPane = null;

	protected MetricsView metricsView = null;

	private static UtilLogger logger = null;

	private static ExtC singleton = new ExtC();

	/**
	 * A class for reading in a graph in PajekNet or GraphML format after
	 * receiving an actionPerformed event.
	 */
	private static final class ReadGraphAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final ExtC extC;

		private ReadGraphAction(String name, ExtC viewer) {
			super(name);
			this.extC = viewer;
		}

		/**
		 * Pops up a file chooser then reads in the graph from the specified
		 * file and loads it.
		 */
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser(lastDirAccessed);
			GraphFileFilter filter = new GraphFileFilter();
			// Java 1.6 FileNameExtensionFilter filter =
			// new FileNameExtensionFilter("PajekNet & GraphML",
			// PajekNetFilenameFilter.PAJEK_NET_EXT, "xml");
			chooser.setFileFilter(filter);
			int option = chooser.showOpenDialog(extC.frame);

			if (option == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				try {
					JavaCallGraph readGraph = getGraph(file.getAbsolutePath(),
							extC);

					if (readGraph != null) {
						extC.graphView.loadGraph(readGraph);
					}
				} catch (IOException e1) {
					String msg = "Unable to read graph from file " + file;
					logger.warning(msg + ": " + e);
					JOptionPane.showMessageDialog(extC.frame, msg,
							"Error reading file", JOptionPane.WARNING_MESSAGE);
				}
			}
		} // actionPerformed
	} // ReadPajekNetAction

	/**
	 * A class for storing a graph in a persistent format (PajekNet or GraphML)
	 * or as a picture (EPS or PNG) after receiving an actionPerformed event.
	 */
	private final class WriteGraphAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final ExtC extC;

		private WriteGraphAction(String name, ExtC viewer) {
			super(name);
			this.extC = viewer;
		}

		/**
		 * Pops up a file chooser then writes in the graph from the specified
		 * file and loads it.
		 */
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser(lastDirAccessed);
			GraphFileFilter filter = new GraphFileFilter();
			chooser.setFileFilter(filter);
			int option = chooser.showSaveDialog(extC.frame);

			if (option == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				lastDirAccessed = file.getParent();
				try {
					String fileName = file.getAbsolutePath();

					if (fileName.endsWith(PajekNetFilenameFilter.PAJEK_NET_EXT)) {
						CallGraphWriter.writePajekNet(extC.graphView.graph
								.getJungGraph(), fileName);
					} else if (fileName.endsWith("eps")) {
						Component component = getGraphComponent();
						CallGraphWriter.saveEPS(component, fileName);
					} else if (fileName.endsWith("png")) {
						Component component = getGraphComponent();
						CallGraphWriter.savePNG(component, fileName);
					} else if (fileName.endsWith("xml")) {
						CallGraphWriter.writeGraphML(extC.graphView.graph,
								fileName);
					} else {
						String message = "For GraphML, choose a file ending in '.xml'.  "
								+ "For Pajek format, choose a file ending in '.net'.  "
								+ "For graphics, choose a file ending in '.eps' or '.png'.";
						JOptionPane.showMessageDialog(extC.frame, message,
								"Unknown file extension",
								JOptionPane.WARNING_MESSAGE);
					}
				} catch (IOException e1) {
					String msg = "Problems writing file " + file;
					logger.warning(msg + ": " + e1);
					JOptionPane.showMessageDialog(extC.frame, msg,
							"Error writing file", JOptionPane.WARNING_MESSAGE);
				}
			}
		} // actionPerformed

		/**
		 * Return the component containing the graph in the currently selected
		 * tab. If a tab without a graph is selected, returns the component
		 * (visualizer) with the graph view.
		 * 
		 * @return the component/visualizer containing the chosen graph
		 */
		protected Component getGraphComponent() {
			Component component = graphView.visViewer;
			int selectedIndex = mainPane.getSelectedIndex();
			if (selectedIndex == BETWEENNESS_TAB_INDEX) {
				component = betweennessView.getVisualizer();
				// component = betweennessApplet.getVisualizer();
			} else if (selectedIndex == AGG_CLUSTERING_TAB_INDEX) {
				component = aggClusteringView.getVisualizer();
			} else if (selectedIndex == SPANNING_FOREST_TAB_INDEX) {
				component = spanningForestView.getVisualizer();
			}
			return component;
		}
	} // WriteGraphAction

	/**
	 * A class for saving the active text area to a file
	 *  after receiving an actionPerformed event.
	 */
	private final class WriteTextAreaAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final ExtC extC;

		private WriteTextAreaAction(String name, ExtC viewer) {
			super(name);
			this.extC = viewer;
		}

		/**
		 * Pops up a file chooser then writes in the graph from the specified
		 * file and loads it.
		 */
		public void actionPerformed(ActionEvent event) {
			int selectedIndex = mainPane.getSelectedIndex();
			if (selectedIndex == METRICS_TAB_INDEX) {
				String msg = "Saving of metric data not yet implemented.";
				JOptionPane.showMessageDialog(frame, msg, "Not Implemented",
						JOptionPane.WARNING_MESSAGE);
			} else if (selectedIndex == GRAPH_TAB_INDEX) {
				String msg = "No text to save from the graph view.";
				JOptionPane.showMessageDialog(frame, msg, "Not Applicable",
						JOptionPane.WARNING_MESSAGE);
			} else {
				JFileChooser chooser = new JFileChooser(lastDirAccessed);
				int option = chooser.showSaveDialog(extC.frame);

				if (option == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					lastDirAccessed = file.getParent();
					try {
						String fileName = file.getAbsolutePath();
						JTextArea textArea = null;
						PrintWriter writer = new PrintWriter(
								new BufferedWriter(new FileWriter(fileName)));

						if (selectedIndex == BETWEENNESS_TAB_INDEX) {
							textArea = betweennessView.getClustersTextArea();
							writeTextToFile(textArea, writer);
						} // end if betweenness
						else if (selectedIndex == AGG_CLUSTERING_TAB_INDEX) {
							textArea = aggClusteringView.getClustersTextArea();
							writeTextToFile(textArea, writer);
						} // end if agglomeration
						else if (selectedIndex == AGGLOMERATION_TAB_INDEX) {
							JOptionPane.showMessageDialog(extC.frame,
									"No text to write.",
									"No text to write",
									JOptionPane.WARNING_MESSAGE);
						} // end if agglomeration
						else if (selectedIndex == SPANNING_FOREST_TAB_INDEX) {
							textArea = spanningForestView.getClustersTextArea();
							writeTextToFile(textArea, writer);
						} // end if spanning forest
						else {
							textArea = batchOutputView.getTextArea();
							writeTextToFile(textArea, writer);
						}
					} catch (IOException e1) {
						String msg = "Problems writing file " + file;
						logger.warning(msg + ": " + e1);
						JOptionPane.showMessageDialog(extC.frame, msg,
								"Error writing file",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}	// else
		} // actionPerformed

		protected void writeTextToFile(JTextArea textArea, PrintWriter writer) {
			String text = textArea.getText();
			writer.print(text);
			writer.close();
		}
	}	// class WriteTextAreaAction

	/**
	 * A class for saving the active text area to a file
	 *  after receiving an actionPerformed event.
	 */
	private final class WriteMetricsAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final ExtC extC;

		private WriteMetricsAction(String name, ExtC viewer) {
			super(name);
			this.extC = viewer;
		}

		/**
		 * Pops up a file chooser then writes in the graph from the specified
		 * file and loads it.
		 */
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser(lastDirAccessed);
			int option = chooser.showSaveDialog(extC.frame);

			if (option == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				lastDirAccessed = file.getParent();
				try {
					metricsView.tableModel.tableToCSVFile(file);
				} catch (Exception e1) {
					String msg = "Problems writing file " + file;
					logger.warning(msg + ": " + e1);
					JOptionPane.showMessageDialog(extC.frame, msg,
							"Error writing file", JOptionPane.WARNING_MESSAGE);
				}
			}
		} // actionPerformed

	}	// class WriteMetricsAction

	/**
	 * Reads in the application parameters
	 * @see getSingleton
	 */
	private ExtC() {
		logger = new UtilLogger("ExtC");
		applicationParameters = ApplicationParameters.getSingleton();
		try {
//			String extcProperty = System.getProperty("extc.properties",
//					PROJECT_ROOT + "/extc.properties");
//			logger.info("extcProperty = " + extcProperty);
			logger.info("System properties = " + System.getProperties());
			if (PROPERTIES_FILE == null) {
				logger.warning("Null PROPERTIES_FILE");
			} else {
				Properties params =
					applicationParameters.loadProperties(PROPERTIES_FILE);
				logger.info("ExtC params = " + params);
			}
		} catch (Exception e) {
			if (logger == null) {
				String msg = "No Logger in ExtC!";
				System.err.println(msg);
				JOptionPane.showMessageDialog(frame, msg,
						"Logger Initialization Problem",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				String msg = "Extc() unable to load " + PROPERTIES_FILE + ": "
						+ e;
				logger.warning(msg);
				JOptionPane.showMessageDialog(frame, msg,
						"Parameter Initialization Problem",
						JOptionPane.WARNING_MESSAGE);
			}
		}
		createGUI();
	}

	/**
	 * @return the singleton
	 */
	public static ExtC getSingleton() {
		return singleton;
	}

	public void getConnectedComponents(
			Graph<CallGraphNode, CallGraphLink> jungGraph) {
		CycleCalculator cycleCalculator = new CycleCalculator();
		Set<CallGraphCluster> stronglyConnectedComponents = cycleCalculator
				.getStronglyConnectedComponents(jungGraph);
		System.out.println("SCC =\n" + stronglyConnectedComponents);
	}

	/**
	 * @return the frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	public GraphView getGraphView() {
		return graphView;
	}

	public BatchOutputView getBatchOutputView() {
		return batchOutputView;
	}

	public ClusteringView getBetweennessView() {
		return betweennessView;
	}

	public ClusteringView getAggClusteringView() {
		return aggClusteringView;
	}

	public AgglomerationView getAgglomerationView() {
		return agglomerationView;
	}

	public SpanningForestView getSpanningForestView() {
		return spanningForestView;
	}

	public MetricsView getMetricsView() {
		return metricsView;
	}

	/**
	 * @return the applicationParameters
	 */
	public ApplicationParameters getApplicationParameters() {
		return applicationParameters;
	}

	public static String getLastDirAccessed() {
		return lastDirAccessed;
	}

	public static void setLastDirAccessed(String lastDirAccessed) {
		ExtC.lastDirAccessed = lastDirAccessed;
	}

	public void clear() {
		// visViewer.stop();
		// graph.removeAllEdges();
		// graph.removeAllVertices();
		// sl.clear();
		// visViewer.restart();
	}

	/**
	 * Reads in a JavaCallGraph from a PajekNet file
	 * 
	 * @param graphFile
	 *            the PajekNet file
	 * @return the newly created graph
	 * @throws IOException
	 */
	private static JavaCallGraph getGraph(String graphFile, ExtC extC)
			throws IOException {
		JavaCallGraph callGraph = null;

		// Read the graph
		if (graphFile.endsWith(PajekNetFilenameFilter.PAJEK_NET_EXT)) {
			callGraph = getPajekNetGraph(graphFile);
		} else if (graphFile.endsWith("xml")) {
			callGraph = readGraphMLGraph(graphFile);
		} else {
			if (extC != null) {
				String msg = "Unknown file type.  Unable to read graph from file "
						+ graphFile;
				JOptionPane.showMessageDialog(extC.frame, msg,
						"Error reading file", JOptionPane.WARNING_MESSAGE);
			}
		}

		// LevelFinder levelFinder = new LevelFinder();
		// Graph<CallGraphNode, CallGraphLink> outputGraph =
		// extractMinimalSpanningForest(levelFinder, graphIn);

		// Graph<CallGraphNode, CallGraphLink> outputGraph =
		// createDAG(levelFinder, graphIn);
		// return outputGraph;
		return callGraph;
	}

	private static JavaCallGraph readGraphMLGraph(String graphFile)
			throws IOException {
		JavaCallGraph callGraph;
		Factory<CallGraphNode> vertexFactory = new CallGraphNode.CallGraphNodeFactory();
		Factory<CallGraphLink> edgeFactory = new CallGraphLink.CallGraphLinkFactory();
		CallGraphReader graphReader = new CallGraphReader(vertexFactory,
				edgeFactory);
		callGraph = graphReader.readGraphMLGraph(graphFile);
		return callGraph;
	}

	private static JavaCallGraph getPajekNetGraph(String graphFile)
			throws IOException {
		Factory<CallGraphNode> vertexFactory = new CallGraphNode.CallGraphNodeFactory();
		Factory<CallGraphLink> edgeFactory = new CallGraphLink.CallGraphLinkFactory();
		CallGraphReader reader = new CallGraphReader(vertexFactory, edgeFactory);
		JavaCallGraph callGraph = reader.readPajekNetGraph(graphFile);
		return callGraph;
	}

	/**
	 * Builds the graphical user interface.
	 * 
	 * @return the main frame
	 */
	public JFrame createGUI() {
		frame = new JFrame();
		frame.setTitle("ExtC Visualizer");

		// Get the size of the default screen
		Dimension frameDimension = Toolkit.getDefaultToolkit().getScreenSize();
		frameDimension.setSize(frameDimension.getWidth(), frameDimension
				.getHeight() - 30);
		frame.setPreferredSize(frameDimension);
		createMainPane();
		frame.getContentPane().add(mainPane);
		JMenuBar menuBar = createMenuBar();
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	protected JMenuBar createMenuBar() {
		JMenu fileMenu = createFileMenu();
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		return menuBar;
	}

	/**
	 * Creates the main pane (the one with all of the tabs).
	 * 
	 * @return the tabbed pane
	 */
	private JTabbedPane createMainPane() {
		mainPane = new JTabbedPane();
		mainPane.addChangeListener(this);
		metricsView = new MetricsView(this);
		metricsView.setUpView();
		graphView = new GraphView(this);
		createBetweennessView();
		createAggClusteringView();
		createAgglomerationView();
		createSpanningForestView();
		batchOutputView = new BatchOutputView(this);
		mainPane.addTab("Metrics", metricsView.getMainPanel());
		mainPane.addTab("Call Graph", graphView.getMainPanel());
		mainPane.addTab("Betweenness", betweennessView.getMainPanel());
		mainPane.addTab("AggClustering", aggClusteringView.getMainPanel());
		mainPane.addTab("Agglomeration", agglomerationView.getMainPanel());
		mainPane.addTab("SpanningForest", spanningForestView.getMainPanel());
		mainPane.addTab("Batch Output", batchOutputView.getMainPanel());
		return mainPane;
	}

	protected void createAgglomerationView() {
		agglomerationView = new AgglomerationView(this);
	}

	protected void createAggClusteringView() {
		AgglomerativeApplet clusteringApplet = new AgglomerativeApplet();
		aggClusteringView = new ClusteringView(clusteringApplet, this);
	}

	protected void createSpanningForestView() {
		SpanningForestApplet clusteringApplet = new SpanningForestApplet();
		spanningForestView = new SpanningForestView(clusteringApplet, this);
	}

	protected void createBetweennessView() {
		BetweennessGraphApplet betweennessApplet =
			new BetweennessGraphApplet();
		betweennessView = new ClusteringView(betweennessApplet, this);
	}

	// see edu.uci.ics.jung.samples.GraphEditorDemo
	public static void main(String[] args) {
		JavaCallGraph graph = readInitialGraph(args);
		ExtC app = new ExtC();
		app.showGraph(graph);
	}

	/**
	 * Display an intraclass dependency graph representing a Java class
	 * @param graph the graph to display
	 */
	public void showGraph(JavaCallGraph graph) {
		if (graph == null) {
			graph = new JavaCallGraph();
		}
		graphView.loadGraph(graph);
		mainPane.setSelectedIndex(GRAPH_TAB_INDEX);
		frame.validate();
		frame.repaint();
	}

	public void showFrequentItems(String handle) throws CoreException {
		FrequentMethodsMiner miner = new FrequentMethodsMiner();
		miner.getFrequentFrequentlyUsedMethods(handle);
	}

	public void printDistances(String handle) {
		try {
			ApplicationParameters params = ApplicationParameters.getSingleton();
			String edgeType = params.getParameter(EDGE_TYPE_KEY,
					EdgeType.DIRECTED.toString());
			JavaCallGraph graph = new JavaCallGraph(handle, EdgeType
					.valueOf(edgeType));
			IntraClassDistanceCalculator calc = new IntraClassDistanceCalculator(
					graph);
			String s = calc.toString();
			System.out.println(s);
		} catch (JavaModelException e) {
			String msg = e.getMessage()
					+ ".  The project may not be loaded in the Eclipse workspace,"
					+ " or the file may not be up-to-date.";
			JOptionPane.showMessageDialog(frame, msg, "Error getting graph",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// Only used by main
	private static JavaCallGraph readInitialGraph(String[] args) {
		// final Logger logger = Logger.getLogger("GraphViewer");

		String graphFile = PROJECT_ROOT
				+ "datasets/SmallTests/boxkite.net";

		for (int i = 0; i < args.length; i++) {
			if ("-graphFile".equals(args[i]) && (args.length > i + 1)) {
				graphFile = args[i + 1];
			}
		}

		JavaCallGraph graph = null;
		try {
			graph = getGraph(graphFile, null);
		} catch (IOException e1) {
			logger.info("Unable to read graph " + graphFile + "\n" + e1);
		}

		return graph;
	}

	private JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
		menu.add(new ReadGraphAction("Open Graph...", this));
		menu.add(new WriteGraphAction("Save Graph as...", this));
		menu.add(new WriteMetricsAction("Save Metrics Data as CSV...", this));
		menu.add(new WriteTextAreaAction("Save Text Area as...", this));
		menu.add(new MetricsView.AccessDBAction("Get Data From Database",
				metricsView));
		return menu;
	}

	/**
	 * Sets up for whatever tab is newly chosen.
	 */
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		JavaCallGraph callGraph = graphView.getGraph();

		if (source instanceof JTabbedPane) {
			JTabbedPane pane = (JTabbedPane) source;
			int selectedIndex = pane.getSelectedIndex();
			
			if (callGraph == null  && selectedIndex != METRICS_TAB_INDEX) {
				String msg = "Choose a class to display.";
				JOptionPane.showMessageDialog(frame, msg,
						"No Class Chosen", JOptionPane.WARNING_MESSAGE);
			} else if (selectedIndex == BETWEENNESS_TAB_INDEX) {
				BetweennessClusterer clusterer =
					new BetweennessClusterer(callGraph);
				BetweennessGraphApplet betweennessApplet =
					(BetweennessGraphApplet) betweennessView
						.getClusteringApplet();
				betweennessApplet.setBetweennessCalculator(clusterer);
				// betweennessApplet.start();
				String id = callGraph.getGraphId();
				String oldId = betweennessView.getGraphId();

				if (!oldId.equals(id)) {
					betweennessView.setUpBetweennessClustering(callGraph);
				}
			} // end if betweenness
			else if (selectedIndex == AGG_CLUSTERING_TAB_INDEX) {
				String id = callGraph.getGraphId();
				String oldId = aggClusteringView.getGraphId();

				if (!oldId.equals(id)) {
					ApplicationParameters params =
						ApplicationParameters.getSingleton();
					String sClusterer = params.getParameter(CLUSTERER_KEY,
							ClustererEnum.MIXED_MODE.toString());
					if (ClustererEnum.MIXED_MODE.toString().equalsIgnoreCase(
							sClusterer)) {
						aggClusteringView.setUpMixedModeClustering(callGraph);
					} else {
						aggClusteringView
								.setUpAgglomerativeClustering(callGraph);
					}
					// TODO remove debug
					System.out.println("Clusterer = " + sClusterer);
				}
			} // end if agglomeration
			else if (selectedIndex == AGGLOMERATION_TAB_INDEX) {
				String id = callGraph.getGraphId();
				String oldId = aggClusteringView.getGraphId();

				if (!oldId.equals(id)) {
					agglomerationView.performAgglomerativeClustering(callGraph);
				}
			} // end if agglomeration
			else if (selectedIndex == SPANNING_FOREST_TAB_INDEX) {
				String id = callGraph.getGraphId();
				String oldId = spanningForestView.getGraphId();

				if (!oldId.equals(id)) {
					spanningForestView.setUpSpanningForest(callGraph);
				}
			} // end if spanning forest
		}
	} // actionPerformed

}
