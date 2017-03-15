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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorEnum;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorIfc;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceMatrix;
import nz.ac.vuw.ecs.kcassell.similarity.LevenshteinDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.junit.Before;
import org.junit.Test;

public class MatrixBasedAgglomerativeClustererTest extends TestCase {

	private static final String B1 = "b+1";
	private static final String A4 = "a+4";
	private static final String A0 = "a+0";

	private MatrixBasedAgglomerativeClusterer clusterer = null;

	private static DistanceCalculatorIfc<String> calc =
		new DistanceCalculatorIfc<String>() {

			/**
			 * Calculates the distance by computing the differences between the numbers
			 * at the end of the names (after the "+" character), e.g. the distance
			 * between a+4 and b+1 is 3 (4 - 1 = 3).
			 */
			public Number calculateDistance(String name1, String name2) {
				Number num1 = extractTrailingNumber(name1);
				Number num2 = extractTrailingNumber(name2);
				Number dist = Math.abs(num1.doubleValue() - num2.doubleValue());
				return dist;
			}

			private Number extractTrailingNumber(String name1) {
				String sNum = null;
				int indexPlus = name1.indexOf("+");
				if (indexPlus >= 0) {
					sNum = name1.substring(indexPlus + 1, name1.length());
				}
				Number num = new Double(sNum);
				return num;
			}

			public DistanceCalculatorEnum getType() {
				return null;
			}
	};
	
	@Before
    public void setUp()
    {
		ApplicationParameters parameters = ApplicationParameters.getSingleton();
		parameters.setParameter(ParameterConstants.LINKAGE_KEY,
				ClusterCombinationEnum.SINGLE_LINK.toString());
		List<String> headers = new ArrayList<String>();
		headers.add(A0);
		headers.add(A4);
		headers.add(B1);
		clusterer = new MatrixBasedAgglomerativeClusterer(headers, calc);
    }


	@Test
	public void testNameCluster() {
		String firstName = "al";
		String secondName = "bo";
		MemberCluster cluster = new MemberCluster();
		cluster.addElement(firstName);
		cluster.addElement(secondName);
		clusterer.previousIteration = 0;
		String newName = clusterer.nameCluster(cluster, firstName);
		assertEquals("al+1", newName);
		assertEquals("al+1", cluster.getClusterName());

		clusterer.previousIteration = 3;
		newName = clusterer.nameCluster(cluster, firstName);
		assertEquals("al+4", newName);
		assertEquals("al+4", cluster.getClusterName());
		newName = clusterer.nameCluster(cluster, secondName);
		assertEquals("bo+4", newName);
		assertEquals("bo+4", cluster.getClusterName());

		clusterer.previousIteration = 3;
		newName = clusterer.nameCluster(cluster, "zeb+1+5+9");
		assertEquals("zeb+4", newName);
	}

	@Test
	public void testGetClusters() {
		Collection<String> oldHeaders = clusterer.getClusters();
		assertEquals(3, oldHeaders.size());
		assertTrue(oldHeaders.contains(A0));
		assertTrue(oldHeaders.contains(A4));
		assertTrue(oldHeaders.contains(B1));
		assertTrue(!oldHeaders.contains("eh?"));
	}

	@Test
	public void testCreateCluster() {
		clusterer.previousIteration = 0;
		Distance<String> nearest =
			new Distance<String>(A0, A4, 6);
		MemberCluster cluster = clusterer.createCluster(nearest);
		Set<?> children = cluster.getChildren();
		assertTrue(children.contains(A0));
		assertTrue(children.contains(A4));
		assertTrue(!children.contains(B1));
		assertEquals(2, cluster.getElementCount());
		Set<String> elements = cluster.getElements();
		assertTrue(elements.contains(A0));
		assertTrue(elements.contains(A4));
		assertTrue(!elements.contains(B1));
		assertEquals("a+1", cluster.getClusterName());
	}

	@Test
	public void testGetNewHeaders() {
		List<String> oldHeaders = clusterer.getDistanceMatrix().getHeaders();
		assertEquals(3, oldHeaders.size());
		assertTrue(oldHeaders.contains(A0));
		assertTrue(oldHeaders.contains(A4));
		assertTrue(oldHeaders.contains(B1));
		clusterer.previousIteration = 0;
		Distance<String> nearest =
			new Distance<String>(A0, A4, 6);
		MemberCluster cluster = clusterer.createCluster(nearest);
		List<String> newHeaders = clusterer.getNewHeaders(cluster);
		assertEquals(2, newHeaders.size());
		assertTrue(!newHeaders.contains(A0));
		assertTrue(!newHeaders.contains(A4));
		assertTrue(newHeaders.contains(B1));
		assertTrue(newHeaders.contains(cluster.getClusterName()));
	}

	@Test
	public void testModifyMatrix() {
		clusterer.previousIteration = 10;
		Distance<String> nearest =
			new Distance<String>(A0, A4, 6);
		MemberCluster cluster = clusterer.createCluster(nearest);
		String clusterName = cluster.getClusterName();
		assertEquals("a+11", clusterName);
		clusterer.modifyMatrix(cluster);
		DistanceMatrix<String> matrix = clusterer.getDistanceMatrix();
		List<String> newHeaders = matrix.getHeaders();
		assertEquals(2, newHeaders.size());
		assertTrue(newHeaders.contains(clusterName));
		// a+0 is within a+11, so the distance between b+1 and a+11 (a+0) is 1
		assertEquals(1.0, matrix.getDistance(clusterName, B1));
		assertEquals(RefactoringConstants.UNKNOWN_DISTANCE,
				matrix.getDistance(clusterName, A0));
		assertEquals(RefactoringConstants.UNKNOWN_DISTANCE,
				matrix.getDistance(A4, A0));
	}

	@Test
	public void testGetSmallestDistanceToGroup() {
		ArrayList<String> fieldVec = new ArrayList<String>();
		MemberCluster cluster = new MemberCluster();
		fieldVec.add("abcd");
        fieldVec.add("12345678");
        fieldVec.add("abcd5678");
        cluster.addElements(fieldVec);
		LevenshteinDistanceCalculator levCalc =
			new LevenshteinDistanceCalculator();
		clusterer = new MatrixBasedAgglomerativeClusterer(fieldVec, levCalc);

		assertEquals(1.0, clusterer.getDistanceToGroup("", cluster, 1.0));
		assertEquals(0.5, clusterer.getDistanceToGroup("", cluster, 0.5));
		assertEquals(0.0, clusterer.getDistanceToGroup("abcd", cluster, 1.0));
		assertEquals(0.25, clusterer.getDistanceToGroup("abc", cluster, 1.0));
		assertEquals(0.5, clusterer.getDistanceToGroup("5678", cluster, 1.0));
		assertEquals(0.25, clusterer.getDistanceToGroup("123ab678", cluster, 1.0));
		assertEquals(0.25, clusterer.getDistanceToGroup("123a567", cluster, 1.0));
	}

	@Test
	public void testCalculateDistance2Clusters() {
        ArrayList<String> fieldVec1 = new ArrayList<String>();
        fieldVec1.add("abcd");
        fieldVec1.add("12345678");
        fieldVec1.add("abcd5678");
        MemberCluster cluster1 = new MemberCluster();
		LevenshteinDistanceCalculator levCalc =
			new LevenshteinDistanceCalculator();
		clusterer = new MatrixBasedAgglomerativeClusterer(fieldVec1, levCalc);
        cluster1.addElements(fieldVec1);
        cluster1.setClusterName("cluster1");
        clusterer.clusterHistory.put("cluster1", cluster1);

        MemberCluster cluster2 = new MemberCluster();
        clusterer.clusterHistory.put("cluster2", cluster2);
        Number distance = clusterer.calculateDistance("cluster1", "cluster2");
		assertEquals(1.0, distance.doubleValue());
		cluster2.addElement("abcd1234");
		assertEquals(0.5, clusterer.calculateDistance("cluster1", "cluster2"));
		cluster2.addElement("5678");
		assertEquals(0.5, clusterer.calculateDistance("cluster1", "cluster2"));
		cluster2.addElement("12cd5678");
		assertEquals(0.25, clusterer.calculateDistance("cluster1", "cluster2"));

        MemberCluster cluster3 = new MemberCluster();
        clusterer.clusterHistory.put("cluster3", cluster3);
		cluster3.addElement("1245678");
		cluster2.addCluster(cluster3);
		assertEquals(0.125, clusterer.calculateDistance("cluster1", "cluster2"));
		cluster3.addElement("12345678");
		cluster2.addCluster(cluster3);
		assertEquals(0.0, clusterer.calculateDistance("cluster1", "cluster2"));
	}

	@Test
	public void testCalculateDistance() {
        ArrayList<String> fieldVec = new ArrayList<String>();
        fieldVec.add("abcd");
        fieldVec.add("12345678");
        fieldVec.add("abcd5678");
        MemberCluster cluster1 = new MemberCluster();
        cluster1.addElements(fieldVec);
        cluster1.setClusterName("cluster1");
		LevenshteinDistanceCalculator levCalc =
			new LevenshteinDistanceCalculator();
		clusterer = new MatrixBasedAgglomerativeClusterer(fieldVec, levCalc);
        clusterer.clusterHistory.put("cluster1", cluster1);

		assertEquals(1.0, clusterer.calculateDistance("", "cluster1"));
		assertEquals(0.0, clusterer.calculateDistance("", ""));
		assertEquals(0.5, clusterer.calculateDistance("c", "cd"));
		assertEquals(1.0, clusterer.calculateDistance("", "cd"));
		assertEquals(0.0, clusterer.calculateDistance("abcd", "cluster1"));
		assertEquals(0.25, clusterer.calculateDistance("abc", "cluster1"));
		assertEquals(0.5, clusterer.calculateDistance("5678", "cluster1"));
		assertEquals(0.25, clusterer.calculateDistance("123ab678", "cluster1"));
		assertEquals(0.25, clusterer.calculateDistance("123a567", "cluster1"));
	}

	@Test
	public void testClusterOnce() {
		clusterer.clusterOnce();
		Collection<String> newHeaders = clusterer.getClusters();
		DistanceMatrix<String> matrix = clusterer.getDistanceMatrix();
		assertEquals(2, newHeaders.size());
		assertTrue(newHeaders.contains(A4));
		assertTrue(newHeaders.contains("a+1"));
		assertEquals(1.0, matrix.getDistance("a+1", A4));
	}

	@Test
	public void testCluster() {
		clusterer.cluster(2);
		Collection<String> clusters = clusterer.getClusters();
		assertEquals(1, clusters.size());
		Iterator<String> iterator = clusters.iterator();
		String clusterName = iterator.next();
		MemberCluster cluster = clusterer.clusterHistory.get(clusterName);
		assertEquals(3, cluster.getElementCount());
	}

}
