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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.TypeMetrics;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.persistence.MetricDatabaseLocator;
import nz.ac.vuw.ecs.kcassell.persistence.QueryBuilder;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.jung.graph.util.EdgeType;

public class MetricsView {
	/** The text area containing a SQL query. */
	public JTextArea metricsQueryArea;
	
	/** This panel will contain the metric info. */
	public JSplitPane mainPanel;
	
	/** The scroll pane containing the metrics table. */
	public JScrollPane metricsTableScroller;
	
	/** Information for the metrics table. */
	public MetricsTableModel tableModel;
	
	/** The table containing potentially troublesome classes. */
	public JTable metricsTable;
	
	public ListSelectionModel listSelectionModel;

	/** The index of the last selected element from the metrics table. */
    protected int lastMetricsSelectionIndex = -1;

    /** The enclosing application of which this view is a part. */
	private ExtC app = null;
   
	private static UtilLogger utilLogger =
		new UtilLogger("BetweennessCalculator");

    /**
     * This class handles selections on the metrics table.
     * @author Keith
     */
    class MetricsListSelectionHandler implements ListSelectionListener {
    	
    	/**
    	 * Create and display the graph for the indicated class.
    	 * @param handle The Eclipse handle for the class
    	 */
    	private void showGraph(String handle) {
    		try {
    	    	ApplicationParameters params = app.getApplicationParameters();
    	    	String sEdgeType = params.getParameter(
    	    			ParameterConstants.EDGE_TYPE_KEY,
    	    			EdgeType.DIRECTED.toString());
    	    	EdgeType edgeType = EdgeType.valueOf(sEdgeType);
    			JavaCallGraph callGraph = new JavaCallGraph(handle, edgeType);
    			app.showGraph(callGraph);
    		} catch (JavaModelException e) {
    			String msg = e.getMessage() +
    			".  The project may not be loaded in the Eclipse workspace," +
    			" or the file may not be up-to-date.";
    			JOptionPane.showMessageDialog(app.getFrame(), msg,
    					"Error showing graph", JOptionPane.WARNING_MESSAGE);
    		}
    	}

    	/**
    	 * Create and display the graph for the class corresponding
    	 *  to the row chosen.
    	 * @param e the selection event from the list
    	 */
        public void valueChanged(ListSelectionEvent e) { 
            ListSelectionModel selectionModel = (ListSelectionModel)e.getSource();
            if (!selectionModel.isSelectionEmpty()) {
                int minIndex = selectionModel.getMinSelectionIndex();
                int maxIndex = selectionModel.getMaxSelectionIndex();
                
                // If a single new item is selected, graph the class
                if (minIndex != lastMetricsSelectionIndex
                		&& maxIndex == minIndex) {
                	lastMetricsSelectionIndex = minIndex;
					String[] handles = tableModel.getHandles();

					try {
						metricsTable.setCursor(RefactoringConstants.WAIT_CURSOR);
						showGraph(handles[minIndex]);
					} finally {
						metricsTable.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}

					IType iType = EclipseUtils.getTypeFromHandle(handles[minIndex]);
					if (iType != null) {
						EclipseUtils.showClass(iType);
					}
                }
            }
        }	// valueChanged
    }	// MetricsListSelectionHandler
    
    
	/**
	 * A class for reading in metric data from the database.
	 */
	public static final class AccessDBAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private MetricsView metricView = null;

		public AccessDBAction(String name, MetricsView metricView) {
			super(name);
			this.metricView = metricView;
		}

		/**
		 * Reads in metric data from the database..
		 */
		public void actionPerformed(ActionEvent e) {
			metricView.loadProblemClassesFromDB(metricView.metricsQueryArea);
			// RecursiveProgramDetector.main(null);
		}
	} // AccessDBAction

	
	public MetricsView(ExtC app) {
		this.app = app;
	}
	
	/**
	 * @return the mainPanel
	 */
	public JComponent getMainPanel() {
		return mainPanel;
	}
	
	/**
	 * @return the class handles currently in the metrics table.
	 */
	public String[] getClassHandles() {
		String[] handles = tableModel.getHandles();
		return handles;
	}

	private String[] getMetricsTableColumnNames(List<TypeMetrics> classes) {
		String[] columnNames = null;
		TypeMetrics class1 = classes.get(0);
		Map<String, Metric> metricValues = class1.getValues();
		Set<String> metricNameSet = metricValues.keySet();
		List<String> metricNames = new ArrayList<String>(metricNameSet);
		Collections.sort(metricNames);
		metricNames.add(0, "Class");
		columnNames = metricNames.toArray(new String[metricNames.size()]);
		return columnNames;
	}

	public void setProblemClasses(List<TypeMetrics> classes) {
		if (!classes.isEmpty()) {
			String [] columnNames = getMetricsTableColumnNames(classes);
			Object [][] data = new Object[classes.size()] [columnNames.length];
			String [] handles = new String[classes.size()];
			int row = 0;
			
			for (TypeMetrics typeMetric : classes) {
				data[row][0] = typeMetric.getName();
				handles[row] = typeMetric.getHandle();
				for (int column = 1; column < columnNames.length; column++) {
					data[row][column] = typeMetric.getValue(columnNames[column]);
				}
				row++;
			}
			tableModel = new MetricsTableModel();
			tableModel.setColumnNames(columnNames);
			tableModel.setData(data);
			tableModel.setHandles(handles);
		}
	}

	public void setUpView() {
		createMainPanel();
		MetricsListSelectionHandler selectionHandler =
			new MetricsListSelectionHandler();
		listSelectionModel.addListSelectionListener(selectionHandler);
//		frame.validate();
//		frame.repaint();
	}

	public void createMainPanel() {
		mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		metricsQueryArea = new JTextArea(10, 80);
		metricsQueryArea.setLineWrap(true);
		metricsQueryArea.setWrapStyleWord(true);
		JScrollPane queryScroller = new JScrollPane(metricsQueryArea);
		QueryBuilder builder = new QueryBuilder();
		String sqlString = builder.getQuery();
		metricsQueryArea.setText(sqlString);
		mainPanel.setTopComponent(queryScroller);
		//JScrollPane metricPanelScroller = 
		new JScrollPane(mainPanel);
    	createMetricsTable();
    	mainPanel.validate();
    	mainPanel.repaint();
	}

	protected void createMetricsTable() {
		if (metricsTableScroller != null) {
			mainPanel.remove(metricsTableScroller);
		}
		if (tableModel == null) {
			tableModel = new MetricsTableModel();
			tableModel.setColumnNames(new String[] {"Classes"});
			tableModel.setData(
					new String[][] {{"**none found**"}, {"**none**"}});
			tableModel.setHandles(new String[] {"**none found**"});
		}
		metricsTable = new JTable(tableModel);
		//metricsTable.setRowSorter(new TableRowSorter<MetricsTableModel>(tableModel));
		metricsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		listSelectionModel = metricsTable.getSelectionModel();
		listSelectionModel.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		metricsTable.setSelectionModel(listSelectionModel);
		metricsTableScroller = new JScrollPane(metricsTable);
		Dimension preferredSize = calculatePreferredSize();
		metricsTableScroller.setPreferredSize(preferredSize);
		metricsTable.setFillsViewportHeight(true);
		metricsTable.setAutoCreateRowSorter(true);
		mainPanel.setBottomComponent(metricsTableScroller);
	}

	private Dimension calculatePreferredSize() {
		Container frame = mainPanel.getParent();
		Dimension frameSize = frame.getSize();
		int frameHeight = (int)frameSize.getHeight();
		int frameWidth = frame.getWidth();
		Dimension querySize = metricsQueryArea.getSize();
		int queryHeight = (int)querySize.getHeight();
		int viewportHeight = frameHeight - queryHeight - 100;
		int viewportWidth = frameWidth - 630;
		Dimension preferredSize = new Dimension(viewportWidth, viewportHeight);
		return preferredSize;
	}


	public void loadProblemClassesFromDB(final Component mainPane) {
		final MetricDatabaseLocator locator = new MetricDatabaseLocator();
		System.out.println("loadingProblemClasses from database...");

		Thread worker = new Thread("LoadFRomDBThread") {

			public void run() {
				// Container container = mainPane.getParent();
				List<TypeMetrics> classes;

				try {
					String sql = metricsQueryArea.getText();

					// If there isn't a legit query, use the default one
					if (sql == null || sql.length() < 5) {
						QueryBuilder builder = new QueryBuilder();
						sql = builder.getQuery();
						metricsQueryArea.setText(sql);
					}
					locator.setSqlQuery(sql);
					classes = locateProblemClasses(locator);
					if (classes != null && !classes.isEmpty()) {
						try {
							mainPanel.setCursor(RefactoringConstants.WAIT_CURSOR);
							setProblemClasses(classes);
						} finally {
							mainPanel.setCursor(RefactoringConstants.DEFAULT_CURSOR);
						}
					} else {
						String msg = "No problem classes found in the database.";
						JOptionPane.showMessageDialog(mainPanel, msg,
								"No Problem Classes Found",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (SQLException e) {
					String msg = "Unable to read classes from database: "
							+ e.getMessage();
					// logger.warning(msg + ": " + e);
					JOptionPane.showMessageDialog(mainPanel, msg,
							"Error Accessing Database",
							JOptionPane.WARNING_MESSAGE);
				}

				// Report the result using invokeLater().
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						System.out.println("createMetricsTable...");
						createMetricsTable();
						MetricsListSelectionHandler selectionHandler = new MetricsListSelectionHandler();
						listSelectionModel
								.addListSelectionListener(selectionHandler);
						System.out.println("... createMetricsTable done");
						app.getFrame().validate();
						app.getFrame().repaint();
					}
				});
			}

			private List<TypeMetrics> locateProblemClasses(
					final MetricDatabaseLocator locator) throws SQLException {
				List<TypeMetrics> classes = null;
				try {
					mainPanel.setCursor(RefactoringConstants.WAIT_CURSOR);
					classes = locator.findProblemClasses();
				} catch (Throwable e) {
					utilLogger.warning("locateProblemClasses caught " + e);
				} finally {
					mainPanel.setCursor(RefactoringConstants.DEFAULT_CURSOR);
				}
				return classes;
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

}