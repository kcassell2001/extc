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

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.persistence.IDatabaseConstants;

// TODO lots!

/**
 * This class builds a SQL query for locating classes that violate certain
 * metric thresholds. In general, for a given metric, one specifies a maximum or
 * minimum threshold value for that metric. As a special case, negative values
 * for a given threshold mean that metric should not be involved in the query.
 */
public class QueryBuilder implements IDatabaseConstants, Constants {
	public double minDCd = 0.5;
	public double minDCi = 1.0;
	public double minLCC = 0.4;
	public double minTCC = 0.25;
	public int maxLCOMCK = 20;
	public double maxLCOMHS = 0.8;
	public int maxFields = 5;
	public int maxMethods = 10;

	/** Builds a SQL query used to identify problem classes in the database. */
	public String getQuery() {
		String sqlString = SELECT + NUM_METHODS + "." + HANDLE_FIELD + ", "
				+ LCOMCK + "." + VALUE_FIELD + AS + LCOMCK + ", " + LCOMHS
				+ "." + VALUE_FIELD + AS + LCOMHS + ", " + TCC + "."
				+ VALUE_FIELD + AS + TCC + ", " + LCC + "." + VALUE_FIELD + AS
				+ LCC + ", " + DCD + "." + VALUE_FIELD + AS + DCD + ", " + DCI
				+ "." + VALUE_FIELD + AS + DCI + ", " + NUM_FIELDS + "."
				+ VALUE_FIELD + AS + NUM_FIELDS + ", " + NUM_METHODS + "."
				+ VALUE_FIELD + AS + NUM_METHODS + " \n" +
				FROM
				+ METRIC_VALUES_TABLE + LCOMCK + ", " + METRIC_VALUES_TABLE
				+ LCOMHS + " " + ", " + METRIC_VALUES_TABLE + TCC + " " + ", "
				+ METRIC_VALUES_TABLE + LCC + " " + ", " + METRIC_VALUES_TABLE
				+ DCD + " " + ", " + METRIC_VALUES_TABLE + DCI + " " + ", "
				+ METRIC_VALUES_TABLE + NUM_METHODS + " " + ", "
				+ METRIC_VALUES_TABLE + NUM_FIELDS + " \n" + 
				WHERE
				+ NUM_METHODS + "." + ACRONYM_FIELD + "= '" + NUM_METHODS + "' " + AND
				+ NUM_METHODS + "." + VALUE_FIELD + "> " + maxMethods + "\n "
				+ AND + getWhereClause(LCOMCK, ">", maxLCOMCK) + AND
				+ getWhereClause(LCOMHS, ">", maxLCOMHS) + AND
				+ getWhereClause(TCC, "<", minTCC) + AND
				+ getWhereClause(LCC, "<", minLCC) + AND
				+ getWhereClause(DCD, "<", minDCd) + AND
				+ getWhereClause(DCI, "<", minDCi) + AND
				+ getWhereClause(NUM_FIELDS, ">", maxFields)
				+ ORDER_BY + LCC + " " + ASCENDING
		;
		return sqlString;
	}

	/**
	 * Builds part of a SQL where clause, something like: NOM.handle =
	 * NOF.handle AND NOF.acronym = 'NOF' AND NOF.value > 0.5
	 * 
	 * @param tableSpec
	 *            the reference to the metric, e.g. NOF. This is also the
	 *            "alias" of the table name
	 * @param comp
	 *            the string representation of the comparison operator, e.g.
	 *            " > "
	 * @param val
	 *            the string representation of the comparison value, e.g. "0.5"
	 * @return
	 */
	protected String getWhereClause(String tableSpec, String comp, double val) {
		String result = NUM_METHODS + "." + HANDLE_FIELD + " = " + tableSpec
				+ "." + HANDLE_FIELD + AND + tableSpec + "." + ACRONYM_FIELD
				+ "= '" + tableSpec + "' " + AND + tableSpec + "."
				+ VALUE_FIELD + comp + val + "\n ";
		return result;
	}

}