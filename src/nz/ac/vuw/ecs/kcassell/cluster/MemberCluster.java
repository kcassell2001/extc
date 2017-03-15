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

package nz.ac.vuw.ecs.kcassell.cluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;
import nz.ac.vuw.ecs.kcassell.utils.StringUtils;

/**
 * This represents clusters of a class's members. A cluster consists of some
 * combination of 0 or more String identifiers for Eclipse member handles and 0
 * or more subclusters.
 * 
 * @author kcassell
 */
public class MemberCluster implements ClusterIfc<String> {

	/** The separator used between elements in an output
	 *  list of cluster sizes. */ 
	static String QUANTITY_SEP = ",";

	/** The subcomponents are either string elements or MemberClusters */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Set children = new TreeSet(new ClusterComparator());

	/**
	 * The number of elements are in the cluster (including those in
	 * subclusters).
	 */
	protected int elementCount = 0;

	/** A user comment. It can be anything, e.g. a distance. */
	protected String comment = "";

	protected String clusterName = "";

	/** The distance between the subclusters. */
	protected Double distance = Double.MIN_VALUE;

	/**
	 * @return the clusterName
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * @param clusterName
	 *            the clusterName to set
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@SuppressWarnings("unchecked")
	public void addElement(String element) {
		children.add(element);
		elementCount++;
	}

	@SuppressWarnings("unchecked")
	public void addElements(Collection<String> elements) {
		children.addAll(elements);
		elementCount += elements.size();
	}

	@SuppressWarnings("unchecked")
	public void addCluster(MemberCluster cluster) {
		children.add(cluster);
		elementCount += cluster.getElementCount();
	}

	/**
	 * @return the number of elements in the cluster (including those in
	 *         subclusters.
	 */
	public int getElementCount() {
		return elementCount;
	}

	/** @return the elements in the cluster (including those in subclusters. */
	public Set<String> getElements() {
		Set<String> elements = new HashSet<String>();

		for (Object obj : children) {
			if (obj instanceof String) {
				elements.add((String) obj);
			} else if (obj instanceof MemberCluster) {
				MemberCluster cluster = (MemberCluster) obj;
				Set<String> elements2 = cluster.getElements();
				elements.addAll(elements2);
			}
		}
		return elements;
	}

	/**
	 * @return the children
	 */
	public Set<?> getChildren() {
		return children;
	}
	
	/**
	 * @param cutOff the distance for which the clusters should be determined
	 * @return all the clusters that existed at the given distance cut off.
	 */
	public ArrayList<Object> getClustersAtDistance(double cutOff) {
		ArrayList<Object> clusters = new ArrayList<Object>();
		
		if (distance <= cutOff) {
			clusters.add(this);
		} else {
			for (Object child : children) {
				if (child instanceof MemberCluster) {
					MemberCluster subcluster = (MemberCluster)child;
					ArrayList<Object> subclusters = subcluster.getClustersAtDistance(cutOff);
					clusters.addAll(subclusters);
				} else {
					clusters.add(child);
				}
			} // for
		} // else (distance > cutoff)
		return clusters;
	}

	protected Integer getClusterIteration() {
		int it = 0;
		String name = getClusterName();
		// Makes use of a naming convention oneName+OtherCount
		int indexPlus = name.lastIndexOf("+");
		// If the name of the cluster matches the "+ convention",
		// extract the iteration number
		if (indexPlus >= 0) {
			String sit = name.substring(indexPlus + 1);
			try {
				it = Integer.parseInt(sit);
			} catch (Exception e) {
				// ignore
			}
		}
		return it;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nz.ac.vuw.ecs.kcassell.callgraph.ClusterIfc#toNestedString()
	 */
	public String toNestedString() {
		StringBuffer buf = new StringBuffer();
		toNestedString(0, buf);
		String nestedString = buf.toString();
		return nestedString;
	}

	protected void toNestedString(int indentLevel, StringBuffer buf) {
		// If this is a top level (visible) node, print its name
		if (indentLevel == 0) {
			buf.append(clusterName).append("\n");
		}
		indentLevel++;
		String leadSpaces = StringUtils.SPACES140.substring(0, 2 * indentLevel);

		for (Object component : children) {
			if (component instanceof MemberCluster) {
				buf.append(leadSpaces);
				MemberCluster cluster = (MemberCluster) component;
				String name = cluster.getClusterName();
				// Makes use of a naming convention oneName+OtherCount
				int indexPlus = name.lastIndexOf("+");
				// If the name of the cluster matches the "+ convention",
				// extract the iteration number
				if (indexPlus >= 0) {
					String it = name.substring(indexPlus + 1);
					buf.append("|+" + it + " (" + cluster.comment + ")\n");
				} else {
					buf.append("|+" + name + "\n");
				}
				cluster.toNestedString(indentLevel, buf);
			} else if (component instanceof String) { // element
				buf.append(leadSpaces).append("|-");
				String name = EclipseUtils.getNameFromHandle(component
						.toString());
				buf.append(name).append("\n");
			}
		}
	}

	public String toFlatString() {
		StringBuffer buf = new StringBuffer(clusterName).append(":\n");
		toFlatString(buf);
		String flatString = buf.toString();
		return flatString;
	}

	/**
	 * Print all of the (leaf) elements of a cluster, disregarding
	 * any nested structure.
	 * @param buf collects the result
	 */
	protected void toFlatString(StringBuffer buf) {
		String leadSpaces = "  ";
		// If this is a leaf, print its name
		if (children == null || children.size() == 0) {
			buf.append(clusterName).append("\n");
		}

		for (Object component : children) {
			if (component instanceof MemberCluster) {
				MemberCluster cluster = (MemberCluster) component;
				cluster.toFlatString(buf);
			} else if (component instanceof String) { // element
				buf.append(leadSpaces);
				String name =
					EclipseUtils.getNameFromHandle(component.toString());
				buf.append(name).append("\n");
			}
		}
	}

	/**
	 * Generates a string representation of the tree using the Newick/New
	 * Hampshire format. Several software packages can create dendrograms based
	 * on this representation.
	 * @see http://evolution.genetics.washington.edu/phylip/newicktree.html
	 */
	public String toNewickString() {
		StringBuffer buf = new StringBuffer();
		HashSet<String> namesSeen = new HashSet<String>();
		toNewickString(0, buf, namesSeen, 1.0);
		buf.append(";\n");
		String nestedString = buf.toString();
		return nestedString;
	}

	protected void toNewickString(int indentLevel, StringBuffer buf,
			HashSet<String> namesSeen, Double parentDistance) {
		String leadSpaces = StringUtils.SPACES140.substring(0, 2 * indentLevel);
		buf.append(leadSpaces).append("(");
		int nextIndent = indentLevel + 1;

		for (Object child : children) {
			buf.append("\n");
			if (child instanceof MemberCluster) {
				MemberCluster cluster = (MemberCluster) child;
				
				// If it's a group of one, treat it as an individual
				if (cluster.getElementCount() == 1) {
					Set<?> grandChildGroup = cluster.getChildren();
					Object grandChild = grandChildGroup.iterator().next();
					memberToNewickString(buf, namesSeen, leadSpaces, grandChild);
				} else { // recursively handle the child
					cluster.toNewickString(nextIndent, buf, namesSeen, distance);
				}
			} else if (child instanceof String) { // element
				memberToNewickString(buf, namesSeen, leadSpaces, child);
			}
			buf.append(",");
		}
		int length = buf.length();
		buf.delete(buf.lastIndexOf(","), length); // eliminate the last ","
		buf.append("\n");
		buf.append(leadSpaces).append(") ");
		appendInternalNewickNodeName(buf);
		appendNewickBranchLength(buf, parentDistance);
	}

	protected void memberToNewickString(StringBuffer buf,
			HashSet<String> namesSeen, String leadSpaces, Object component) {
		String name = EclipseUtils.getNameFromHandle(component
				.toString());
		if (namesSeen.contains(name)) {
			name += "_" + namesSeen.size();
			namesSeen.add(name);
		}
		buf.append(leadSpaces).append("  ").append(name);
		// add Newick branch length
		buf.append(":");
		// Ensure there is a nonzero branch length
		buf.append(String.format("%.2f", Math.max(0.01, distance)));
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	private void appendNewickBranchLength(StringBuffer buf, Double parentDistance) {
		// Double clusteringIteration = getClusterIteration();
		// // We make the branch length proportional to the iteration, although
		// // proportional to the distance measure might be better
		// buf.append(":").append(clusteringIteration);
//		double maxChildDistance = getMaxChildDistance();
		double branchLength = parentDistance - distance;
		branchLength = Math.max(0.01, branchLength);
		buf.append(":").append(String.format("%.2f", branchLength));
	}

	/**
	 * Label the internal node with the distance (appended with the iteration).
	 * Appending the iteration is necessary to make the label unique (to satisfy
	 * Matlab). Spaces are replaced with underscores.
	 */
	private void appendInternalNewickNodeName(StringBuffer buf) {
		Integer clusteringIteration = getClusterIteration();
		if (clusteringIteration > 0) {
			buf.append("it").append(clusteringIteration.intValue());
			buf.append("-").append(String.format("%.2f", distance));
		} else {
			buf.append(getClusterName());
		}
	}

	public String toString() {
		String text = "";
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		String clusterFormat =
			 parameters.getParameter(
							ParameterConstants.CLUSTER_TEXT_FORMAT_KEY,
							ClusterTextFormatEnum.NEWICK.toString());
		ClusterTextFormatEnum textFormatEnum =
			ClusterTextFormatEnum.valueOf(clusterFormat);
		if (ClusterTextFormatEnum.FLAT.equals(textFormatEnum)) {
			text = toFlatString();
		} else if (ClusterTextFormatEnum.NESTED.equals(textFormatEnum)) {
			text = toNestedString();
		} else if (ClusterTextFormatEnum.NEWICK.equals(textFormatEnum)) {
			text = toNewickString();
		} else {
			text = toNestedString();
		}
		return text;
	}

	/**
	 * Saves agglomerated clusters to a file in Newick format
	 * @param className the name of the class whose members were clustered
	 * @param cluster the final cluster produced
	 * @return the file where the data was saved
	 * @throws IOException 
	 */
	public static String saveResultsToFile(String className,
			MemberCluster cluster,
			String sClusterer,
			String sCalc,
			String sLinkage) throws IOException {
		String newickFile = RefactoringConstants.DATA_DIR + "Dendrograms/" +
					className + sClusterer + sCalc + sLinkage + ".tree";
		cluster.writeNewickToFile(newickFile);
		return newickFile;
	}

	/**
	 * Write the Newick representation of the cluster to the specified file.
	 * @param fileName
	 * @throws IOException
	 */
	protected void writeNewickToFile(String fileName) throws IOException {
		PrintWriter writer = null;
		FileWriter fileWriter = null;
		
		try {
			fileWriter = new FileWriter(fileName);
			writer = new PrintWriter(
					new BufferedWriter(fileWriter));
			String clusterString = toNewickString();
			writer.print(clusterString );
		} finally {
			if (writer != null) {
				writer.close();
			} else if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Output the cluster sizes.  If there are multiple clusters
	 * of the same size, put the number in parentheses.
	 * @param clusters clusters ordered by size
	 * @return something like:  "5,3,1(4)"
	 */
	public static String clusterSizesToString(ArrayList<Object> clusters) {
		StringBuffer sbuf = new StringBuffer();
		int previousClusterSize = -1;
		int sameQuantity = 1;  // # of consecutive clusters of this size
		ArrayList<Object> sortedClusters =
			new ArrayList<Object>(clusters);
		MemberClusterSizeComparator comparator = new MemberClusterSizeComparator();
		comparator.setAscending(false);
		Collections.sort(sortedClusters, comparator);
		
		for (Object object : sortedClusters) {
			int clusterSize = determineClusterSize(object);

			if (previousClusterSize == -1) {
				sameQuantity = 1;
			} else if (previousClusterSize == clusterSize) {
				sameQuantity++;
			} else { // produce output for the previous cluster size
				appendQuantityToBuffer(sbuf, previousClusterSize, sameQuantity);
				sameQuantity = 1;
			}
			previousClusterSize = clusterSize;
		} // for
		// handle the last one
		appendQuantityToBuffer(sbuf, previousClusterSize, sameQuantity);
		sbuf.deleteCharAt(sbuf.length() - 1);  // remove trailing separator
		return sbuf.toString();
	}

	private static void appendQuantityToBuffer(StringBuffer sbuf,
			int previousClusterSize, int sameQuantity) {
		// only 1 cluster of the previous size
		if (sameQuantity == 1) {
			sbuf.append(previousClusterSize).append(QUANTITY_SEP );
		} else { // mult. clusters of the previous size
			sbuf.append(previousClusterSize);
			sbuf.append("(").append(sameQuantity).append(")");
			sbuf.append(QUANTITY_SEP );
		}
	}

	private static int determineClusterSize(Object object) {
		int clusterSize = 1;
		if (object instanceof MemberCluster) {
			MemberCluster cluster = (MemberCluster)object;
			clusterSize = cluster.getElementCount();
		} else {
			clusterSize = 1;
		}
		return clusterSize;
	}



}
