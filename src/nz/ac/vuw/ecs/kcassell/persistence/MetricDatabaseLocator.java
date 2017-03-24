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

package nz.ac.vuw.ecs.kcassell.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.TypeMetrics;
import net.sourceforge.metrics.persistence.Database;
import net.sourceforge.metrics.persistence.IDatabaseConstants;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

public class MetricDatabaseLocator implements IDatabaseConstants, Constants {

	protected String sqlQuery = null;
	
	private static UtilLogger utilLogger =
		new UtilLogger("MetricDatabaseLocator");

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	/**
	 * Collects all the software measurements for classes that match the 
	 * SQL query.
	 * @param args
	 */
	public List<TypeMetrics> findProblemClasses() throws SQLException {
		List<TypeMetrics> problemHandles = new ArrayList<TypeMetrics>();
		/*
		 * We are storing the Statement and Prepared statement object references
		 * in an array list for convenience.
		 */
		ArrayList<Statement> statements = new ArrayList<Statement>();
		Statement statement = null;
		ResultSet resultSet = null;
		Database db = new Database();
		Connection connection = null;
		/*
		 * We will be using Statement and PreparedStatement objects for
		 * executing SQL. These objects, as well as Connections and ResultSets,
		 * are resources that should be released explicitly after use, hence the
		 * try-catch-finally pattern used below.
		 */
		try {
			// db.setClientURL("jdbc:derby://localhost:1527/C:/metrics/metrics2DB");
			db.loadDriver();
			connection = db.prepareConnection();

			/*
			 * Creating a statement object that we can use for running various
			 * SQL statements commands against the database.
			 */
			statement = connection.createStatement();
			statements.add(statement);

			if (sqlQuery == null) {
				QueryBuilder builder = new QueryBuilder();
				sqlQuery = builder.getQuery();
			}
			boolean gotResultSet = statement.execute(sqlQuery);
			if (gotResultSet) {
				resultSet = statement.getResultSet();
				problemHandles = collectMetricResults(resultSet);
			}

			statement.close();
			connection.commit();
//			System.out.println("Committed the transaction");

			// In embedded mode, an application should shut down the database.
			db.shutDownEmbedded();
		} catch (SQLException sqle) {
			utilLogger.warning("SQLException = " + sqle);
			Database.printSQLException(sqle);
			throw sqle;
		} finally {
			// release all open resources to avoid unnecessary memory usage
			try {
				db.releaseResources(connection, statements, resultSet);
			} catch (Throwable re) {
				utilLogger.warning("caught " + re + " while releasing resources");
			}
		}
		return problemHandles;
	}

	public static List<TypeMetrics> collectMetricResults(ResultSet resultSet)
			throws SQLException {
		List<TypeMetrics> problemClasses = new ArrayList<TypeMetrics>();
		EclipseUtils.prepareWorkspace();

		while (resultSet.next()) {
			TypeMetrics problemClassMetrics = new TypeMetrics();
			String handle = resultSet.getString(HANDLE_FIELD.trim());
			IJavaElement element = JavaCore.create(handle);
			problemClassMetrics.setHandle(handle);
			problemClassMetrics.setJavaElement(element);

			ResultSetMetaData metaData = resultSet.getMetaData();
			for (int i = 2; i <= metaData.getColumnCount(); i++) {
				String metricName = metaData.getColumnName(i);
				Double value = resultSet.getDouble(metricName);
				Metric metric = new Metric(metricName, value);
				problemClassMetrics.setValue(metric);
			}
			problemClasses.add(problemClassMetrics);
		}
		return problemClasses;
	}

}
