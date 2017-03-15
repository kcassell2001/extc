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

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.metrics.persistence.Database;
import net.sourceforge.metrics.persistence.IDatabaseConstants;
import nz.ac.vuw.ecs.kcassell.callgraph.gui.ClusterUIConstants;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;

public class RecordInserter
implements ClusterUIConstants, IDatabaseConstants  {

	/**
	 * Save metric values for the indicated Java element and all of its
	 * subelements to a database.
	 * @param element
	 *            the highest level element to be saved, e.g. a project
	 * @throws InvocationTargetException
	 * @throws SQLException 
	 */
	public void saveMeasurementsToDB(Collection<SoftwareMeasurement> measurements)
	// , IProgressMonitor monitor)
			throws InvocationTargetException, SQLException {
		Database db = new Database();
		db.loadDriver();
		Connection connection = null;
		/* Store the Statement and Prepared statement object references
		 * in a list for convenience. */
		List<Statement> statements = new ArrayList<Statement>();
		Statement statement = null;
		ResultSet resultSet = null;
		
		/* Statements, PreparedStatements, Connections and ResultSets
		 * are resources that should be released explicitly after use, hence the
		 * try-catch-finally pattern used below. */
		try {
			connection = db.prepareConnection();

			/* Creating a statement object that we can use for running various
			 * SQL statements commands against the database. */
			statement = connection.createStatement();
			statements.add(statement);
			PreparedStatement deleteStatement =
				addDeleteMetricValuesPreparedStatement(connection, statements);
			PreparedStatement insertStatement =
				addInsertMetricValuesPreparedStatement(connection, statements);
			
			for (SoftwareMeasurement measurement : measurements) {
				String handle = measurement.getHandle();
				Double value = measurement.getMeasurement();
				String metricId = measurement.getMetricId();
				Integer prefKey = measurement.getPrefKey();
				saveMetricValue(deleteStatement, insertStatement, metricId, value,
						handle, prefKey);
			}

			statement.close();
			connection.commit();
			System.out.println("Committed the transaction");

			// In embedded mode, an application should shut down the database.
			db.shutDownEmbedded();
		} catch (SQLException sqle) {
			Database.printSQLException(sqle);
			throw sqle;
		} finally {
			// release all open resources to avoid unnecessary memory usage
			db.releaseResources(connection, statements, resultSet);
		}
	}

	private PreparedStatement addDeleteMetricValuesPreparedStatement(
			Connection connection, List<Statement> statements)
			throws SQLException {
		String sqlString = DELETE + METRIC_VALUES_TABLE +
		WHERE + HANDLE_FIELD + " = ? " + AND  + ACRONYM_FIELD + " = ? " +
		AND + USER_PREFERENCES_FOREIGN_KEY + " = ?";
		PreparedStatement statement =
			connection.prepareStatement(sqlString);
		statements.add(statement);
		return statement;
	}

	private PreparedStatement addInsertMetricValuesPreparedStatement(
			Connection connection, List<Statement> statements)
			throws SQLException {
		// Values: handle, metricId, value, preferenceKey
		String sqlString =
			INSERT + METRIC_VALUES_TABLE + VALUES + "(?, ?, ?, ?)";
		PreparedStatement statement = connection.prepareStatement(sqlString);
		statements.add(statement);
		return statement;
	}

	private void saveMetricValue(PreparedStatement deleteStatement,
			PreparedStatement insertStatement, String metricId, double value,
			String handle, int prefKey) {
		deleteOldMetricValue(deleteStatement, metricId, handle, prefKey);
		insertNewMetricValue(insertStatement, metricId, value, handle, prefKey);
	}

	private void deleteOldMetricValue(PreparedStatement deleteStatement,
			String metricId, String handle, int prefKey) {
		// Delete old metric values from prior runs
		try {
			deleteStatement.setString(1, handle);
			deleteStatement.setString(2, metricId);
			deleteStatement.setInt(3, prefKey);
			deleteStatement.executeUpdate();
		} catch (SQLException e) {
			// quietly swallow the exception. In many cases, there will be
			// nothing in the database to delete
		}
	}
	
	private void insertNewMetricValue(PreparedStatement insertStatement,
			String metricId, double value, String handle, int prefKey) {
		// Insert the new values
		try {
			insertStatement.setString(1, handle);
			insertStatement.setString(2, metricId);
			insertStatement.setDouble(3, value);
			insertStatement.setInt(4, prefKey);
			insertStatement.executeUpdate();
		} catch (SQLException e) {
			Database.printSQLException(e);
		}
	}

	/**
	 * Gets the primary key for the row in the table corresponding
	 * to the current user preferences.  If such a row does not already exist,
	 * it is created.
	 * @return the key
	 */
	public static int getPreferencesKey()
	throws SQLException {
		int key = 1;
    	ApplicationParameters params = ApplicationParameters.getSingleton();
		int useOrig = 0;
		int connectIfc =
			params.getBooleanParameterAsInt(CONDENSE_REQUIRED_METHODS, false);
		int countAbstract =
			params.getBooleanParameterAsInt(CONDENSE_REQUIRED_METHODS, false);
		int countConstructors =
			params.getBooleanParameterAsInt(INCLUDE_CONSTRUCTORS, false);
		int countDeprecated = 0;
		int countInheritedAttributes =
			params.getBooleanParameterAsInt(INCLUDE_INHERITED, false);
		int countInheritedMethods =
			params.getBooleanParameterAsInt(INCLUDE_INHERITED, false);
		int countInners = params.getBooleanParameterAsInt(INCLUDE_INNERS, false);
		int countLoggers = params.getBooleanParameterAsInt(INCLUDE_LOGGERS, false);
		int countObjectsMethods =
			params.getBooleanParameterAsInt(INCLUDE_OBJECT_METHODS, false);
		int countPublicMethodsOnly = 0;
		int countStaticAttributes =
			params.getBooleanParameterAsInt(INCLUDE_STATIC_MEMBERS, false);
		int countStaticMethods =
			params.getBooleanParameterAsInt(INCLUDE_STATIC_MEMBERS, false);
		String ignoreMembersPattern = "";;

		String selectString = buildSelectPreferenceString(useOrig, connectIfc,
				countAbstract, countConstructors, countDeprecated,
				countInheritedAttributes, countInheritedMethods, countInners,
				countLoggers, countObjectsMethods, countPublicMethodsOnly,
				countStaticAttributes, countStaticMethods, ignoreMembersPattern);

		Database db = new Database();
		db.loadDriver();
		Connection connection = null;
		Statement statement = null;
		List<Statement> statements = new ArrayList<Statement>();

		/* Statements, PreparedStatements, Connections and ResultSets
		 * are resources that should be released explicitly after use, hence the
		 * try-catch-finally pattern used below. */
		try {
			connection = db.prepareConnection();

			/*
			 * Creating a statement object that we can use for running various
			 * SQL statements commands against the database.
			 */
			statement = connection.createStatement();
			statements.add(statement);

			Statement selectStatement = connection.createStatement();
			statements.add(selectStatement);
			ResultSet resultSet = selectStatement.executeQuery(selectString);

			// If there is an existing row with these preferences, use the key
			if (resultSet.next()) {
				key = resultSet.getInt(1);
				resultSet.close();
			}
			// If no existing row exists, insert one
			else {
				resultSet.close();
				insertPreference(connection, statements, useOrig, connectIfc,
						countAbstract, countConstructors, countDeprecated,
						countInheritedAttributes, countInheritedMethods,
						countInners, countLoggers, countObjectsMethods,
						countPublicMethodsOnly, countStaticAttributes,
						countStaticMethods, ignoreMembersPattern);

				// Get the newly created key
				selectStatement = connection.createStatement();
				statements.add(selectStatement);
				resultSet = selectStatement.executeQuery(selectString);
				if (resultSet.next()) {
					key = resultSet.getInt(1);
				}
				resultSet.close();
			}
			connection.commit();
		} catch (SQLException sqle) {
			Database.printSQLException(sqle);
			throw sqle;
		} finally {
			// release all open resources to avoid unnecessary memory usage
			db.releaseResources(connection, statements, null);
		}
		return key;
	}

	private static String buildSelectPreferenceString(int useOrig, int connectIfc,
			int countAbstract, int countConstructors, int countDeprecated,
			int countInheritedAttributes, int countInheritedMethods,
			int countInners, int countLoggers, int countObjectsMethods,
			int countPublicMethodsOnly, int countStaticAttributes,
			int countStaticMethods, String ignoreMembersPattern) {
		String selectString = SELECT + PREFERENCE_ID_FIELD +
			FROM + USER_PREFERENCES_TABLE +
			WHERE +
			USE_ORIGINALS_PREF + " = " + useOrig + " " + AND +
			CONNECT_INTERFACE_METHODS_PREF + " = " + connectIfc + " " + AND +
			COUNT_ABSTRACT_METHODS_PREF + " = " + countAbstract + " " + AND +
			COUNT_CONSTRUCTORS_PREF + " = " + countConstructors + " " + AND +
			COUNT_DEPRECATED_PREF + " = " + countDeprecated + " " + AND +
			COUNT_INHERITED_ATTRIBUTES_PREF + " = " + countInheritedAttributes + " " + AND +
			COUNT_INHERITED_METHODS_PREF + " = " + countInheritedMethods + " " + AND +
			COUNT_INNERS_PREF + " = " + countInners + " " + AND +
			COUNT_LOGGERS_PREF + " = " + countLoggers + " " + AND +
			COUNT_OBJECTS_METHODS_PREF + " = " + countObjectsMethods + " " + AND +
			COUNT_PUBLIC_METHODS_ONLY_PREF + " = " + countPublicMethodsOnly + " " + AND +
			COUNT_STATIC_ATTRIBUTES_PREF + " = " + countStaticAttributes + " " + AND +
			COUNT_STATIC_METHODS_PREF + " = " + countStaticMethods + " " + AND +
			IGNORE_MEMBERS_PATTERN_PREF + " = '" + ignoreMembersPattern + "'"
			;
		return selectString;
	}

	/**
	 * Insert a row into the user preferences table.
	 * @throws SQLException
	 */
	private static void insertPreference(Connection connection,
			List<Statement> statements, int useOrig, int connectIfc,
			int countAbstract, int countConstructors, int countDeprecated,
			int countInheritedAttributes, int countInheritedMethods,
			int countInners, int countLoggers, int countObjectsMethods,
			int countPublicMethodsOnly, int countStaticAttributes,
			int countStaticMethods, String ignoreMembersPattern)
			throws SQLException {
		String insertString = INSERT + USER_PREFERENCES_TABLE +
		"(" +
		USE_ORIGINALS_PREF + ", " +
		CONNECT_INTERFACE_METHODS_PREF + ", " +
		COUNT_ABSTRACT_METHODS_PREF + ", " +
		COUNT_CONSTRUCTORS_PREF + ", " +
		COUNT_DEPRECATED_PREF + ", " +
		COUNT_INHERITED_ATTRIBUTES_PREF + ", " +
		COUNT_INHERITED_METHODS_PREF + ", " +
		COUNT_INNERS_PREF + ", " +
		COUNT_LOGGERS_PREF + ", " +
		COUNT_OBJECTS_METHODS_PREF + ", " +
		COUNT_PUBLIC_METHODS_ONLY_PREF + ", " +
		COUNT_STATIC_ATTRIBUTES_PREF + ", " +
		COUNT_STATIC_METHODS_PREF + ", " +
		IGNORE_MEMBERS_PATTERN_PREF +
		")" +
		VALUES + 
		"(" +
		useOrig + ", " +
		connectIfc + ", " +
		countAbstract + ", " +
		countConstructors + ", " +
		countDeprecated + ", " +
		countInheritedAttributes + ", " +
		countInheritedMethods + ", " +
		countInners + ", " +
		countLoggers + ", " +
		countObjectsMethods + ", " +
		countPublicMethodsOnly + ", " +
		countStaticAttributes + ", " +
		countStaticMethods + ", " +
		"'" + ignoreMembersPattern + "'" +
		")";
		Statement insertStatement = connection.createStatement();
		statements.add(insertStatement);
		insertStatement.executeUpdate(insertString);
	}


}
