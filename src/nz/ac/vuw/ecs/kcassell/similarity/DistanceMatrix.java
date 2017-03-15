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

package nz.ac.vuw.ecs.kcassell.similarity;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.cluster.Distance;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * The DistanceMatrix stores distances between different objects.  By default,
 * This class assumes that distances are symmetrical, e.g. the distance
 * from A to B is the same as the distance from B to A; however,
 * the client class may change that by modifying the isSymmetric member.
 * When the matrix is symmetric, values are stored in the "lower left", i.e.
 * valid values are at matrix[row][column] where row >= column.
 * @author Keith
 * @param <V> the type of the objects for which the distances are being stored
 */
public class DistanceMatrix<V> implements RefactoringConstants {
	
	/** Indicates whether dist(a, b) == dist(b, a) */
	protected boolean isSymmetric = true;

	/** The raw data. For now at least, a square matrix. */
	protected DoubleMatrix2D matrix = null;

	/** The members used for row and column headers. */
	protected List<V> headers;

	/** keeps track of which index in the array corresponds to each member. */
	protected HashMap<V, Integer> memberIndex =
		new HashMap<V, Integer>();

	/**
	 * Builds a distance matrix using the headers provided.
	 * @param callGraph the graph of nodes whose distances will be stored
	 * @param distanceCalculator calculates the distances between nodes
	 */
	public DistanceMatrix(List<V> headers) {
		this.headers = headers;
		int index = 0;

		// Keep track of which index in the array corresponds to each member
		for (V member : headers) {
			memberIndex.put(member, index++);
		}

		int size = headers.size();
		matrix = new DenseDoubleMatrix2D(size, size);
		matrix.assign(UNKNOWN_DISTANCE.doubleValue());
	}
	
	
	/**
	 * Use the distance calculator to fill in the distance matrix.
	 * @param calc the distance calculator
	 */
	public void fillMatrix(DistanceCalculatorIfc<V> calc) {
		for (int row = 0; row < headers.size(); row++) {
			V obj1 = headers.get(row);
			for (int col = 0; col <= row; col++) {
				V obj2 = headers.get(col);
				Number distance = calc.calculateDistance(obj1, obj2);
				setDistance(obj1, obj2, distance);
			}
		}
	}



	/**
	 * Gets the index corresponding to the supplied member.
	 * 
	 * @param member
	 *            the member whose index value is being searched for
	 * @return the nonnegative index if the index exists; negative otherwise
	 */
	protected int getIndex(V member) {
		int index = -1;

		Integer indexInteger = memberIndex.get(member);
		// For some reason, some methods within anonymous classes don't get
		// indexed properly.
		// TODO figure out why error can occur and/or write to log
		if (indexInteger != null) {
			index = indexInteger;
		}
		return index;
	}

	public Number getDistance(V node1, V node2) {
		Number distance = UNKNOWN_DISTANCE;

		Integer index1 = memberIndex.get(node1);
		Integer index2 = memberIndex.get(node2);
		if (index1 != null && index2 != null) {
			if (isSymmetric && index2 > index1) {
				distance = matrix.get(index2, index1);
			} else {
				distance = matrix.get(index1, index2);
			}
		}
		return distance;
	}

	public void setDistance(V node1, V node2, Number distance) {
		if (distance != null) {
			Integer index1 = memberIndex.get(node1);
			Integer index2 = memberIndex.get(node2);
			if (index1 != null && index2 != null) {
				if (isSymmetric && index2 > index1) {
					matrix.set(index2, index1, distance.doubleValue());
				} else {
					matrix.set(index1, index2, distance.doubleValue());
				}
			}
		}
	}

	/**
	 * @return the two elements that are closest together.
	 * Ties are broken arbitrarily.
	 */
	public Distance<V> findNearest() {
		Distance<V> nearest = null;
		double smallestYet = MAX_DISTANCE.doubleValue();
		
		if (headers.size() > 1) {
			smallestYet = matrix.get(1, 0);
			nearest = new Distance<V>(headers.get(0), headers.get(1), smallestYet);
		}
		int row = 0;
		for (V element1: headers) {
			int col = 0;
			for (V element2: headers) {
				double distance = matrix.get(row, col);
				
				if ( // (distance != 0) 
						(row != col)
						&& (distance != UNKNOWN_DISTANCE.doubleValue()) 
						&& (distance < smallestYet)) {
					nearest = new Distance<V>(element1, element2, distance);
					smallestYet = distance;
				}
				col++;
			}
			row++;
		}
		return nearest;
	}
	
	/**
	 * @return the headers
	 */
	public List<V> getHeaders() {
		return headers;
	}

	/**
	 * @return a human-readable form of the matrix
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("DistanceMatrix@"
				+ hashCode() + "\n");
		int size = headers.size();
        DecimalFormat numberFormatter = new DecimalFormat();
        numberFormatter.setMaximumFractionDigits(2);

        // print rows
        if (isSymmetric) {
        	symmetricToString(buf, size, numberFormatter);
        } else {
        	asymmetricToString(buf, size, numberFormatter);
        }
		return buf.toString();
	}

	/**  Prints the entire matrix */
	protected void asymmetricToString(StringBuffer buf, int size,
			DecimalFormat numberFormatter) {
		for (int i = 0; i < size; i++) {
			buf.append(" ").append(i).append(" ");
			String member = headers.get(i).toString();
			member = String.format("%-10.10s", member);
			buf.append(member);
			for (int j = 0; j < size; j++) {
				double distance = matrix.get(i, j);
				if (distance == UNKNOWN_DISTANCE.doubleValue()) {
					buf.append("\t-");
				} else {
					buf.append("\t").append(numberFormatter.format(distance));
				}
			}
			buf.append("\n");
		}
	}

	/**  Prints the bottom left of the symmetric matrix */
	protected void symmetricToString(StringBuffer buf, int size,
			DecimalFormat numberFormatter) {
		for (int i = 0; i < size; i++) {
			buf.append(" ").append(i).append(" ");
			String member = headers.get(i).toString();
			String member10 = String.format("%-10.10s", member);
			buf.append(member10); // the shortened member name begins the row
			for (int j = 0; j < i; j++) {
				double distance = matrix.get(i, j);
				if (distance == UNKNOWN_DISTANCE.doubleValue()) {
					buf.append("\t-");
				} else {
					buf.append("\t").append(numberFormatter.format(distance));
				}
			}
			buf.append("\t").append(member); // the name is the column header
			buf.append("\n");
		}
	}

} // class DistanceMatrix
