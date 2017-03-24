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
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;


public class MemberClusterTest extends TestCase {

	@Before
    public void setUp()
    {
    }


    @Test
    public void testAddElements()
    {
        String field1 = "field1";
        String field2 = "field2";
        String field3 = "field3";
        Vector<String> fieldVec = new Vector<String>();
        fieldVec.add(field2);
        fieldVec.add(field3);
        fieldVec.add(field1);
        MemberCluster cluster1 = new MemberCluster();
        cluster1.addElements(fieldVec);
        Set<String> fields = cluster1.getElements();
        assertTrue(fields.contains(field3));
        assertTrue(fields.contains(field1));
    }

    @Test
    public void testGetElements()
    {
        MemberCluster clusterFields = new MemberCluster();
        int elementCount = clusterFields.getElementCount();
        assertEquals(0, elementCount);
        Set<String> elements = clusterFields.getElements();
        assertEquals(0, elements.size());
        
        String field1 = "field1";
        String field2 = "field2";
        String field3 = "field3";
        Vector<String> fieldVec = new Vector<String>();
        fieldVec.add(field2);
        fieldVec.add(field3);
        fieldVec.add(field1);
        clusterFields.setClusterName("3fields");
        clusterFields.addElements(fieldVec);
        elementCount = clusterFields.getElementCount();
        assertEquals(3, elementCount);
        elements = clusterFields.getElements();
        assertEquals(3, elements.size());
        assertTrue(elements.containsAll(fieldVec));

        String method1 = "method1";
        String method2 = "method2";
        String method3 = "method3";
        Vector<String> methodVec = new Vector<String>();
        methodVec.add(method2);
        methodVec.add(method3);
        methodVec.add(method1);
        MemberCluster clusterMethods = new MemberCluster();
        clusterMethods.setClusterName("3methods");
        clusterMethods.addElements(methodVec);
        
        clusterFields.addCluster(clusterMethods);
        elementCount = clusterFields.getElementCount();
        assertEquals(6, elementCount);
        elements = clusterFields.getElements();
        assertEquals(6, elements.size());
        assertTrue(elements.containsAll(fieldVec));
        assertTrue(elements.containsAll(methodVec));
}


    @Test
    public void testGetChildren()
    {
        MemberCluster clusterFields = new MemberCluster();
        int elementCount = clusterFields.getElementCount();
        assertEquals(0, elementCount);
        Set<?> children = clusterFields.getChildren();
        assertEquals(0, children.size());
        
        String field1 = "field1";
        String field2 = "field2";
        String field3 = "field3";
        Vector<String> fieldVec = new Vector<String>();
        fieldVec.add(field2);
        fieldVec.add(field3);
        fieldVec.add(field1);
        clusterFields.setClusterName("3fields");
        clusterFields.addElements(fieldVec);
        elementCount = clusterFields.getElementCount();
        assertEquals(3, elementCount);
        children = clusterFields.getChildren();
        assertEquals(3, children.size());
        assertTrue(children.containsAll(fieldVec));

        String method1 = "method1";
        String method2 = "method2";
        String method3 = "method3";
        Vector<String> methodVec = new Vector<String>();
        methodVec.add(method2);
        methodVec.add(method3);
        methodVec.add(method1);
        MemberCluster clusterMethods = new MemberCluster();
        clusterMethods.setClusterName("3methods");
        clusterMethods.addElements(methodVec);
        
        clusterFields.addCluster(clusterMethods);
        elementCount = clusterFields.getElementCount();
        assertEquals(6, elementCount);
        children = clusterFields.getChildren();
        assertEquals(4, children.size());
        assertTrue(children.containsAll(fieldVec));
        assertTrue(children.contains(clusterMethods));
        assertTrue(!children.containsAll(methodVec));
}


    @Test
    public void testToNestedString()
    {
        String field1 = "field1";
        String field2 = "field2";
        String field3 = "field3";
        Vector<String> fieldVec = new Vector<String>();
        fieldVec.add(field2);
        fieldVec.add(field3);
        fieldVec.add(field1);
        MemberCluster clusterFields = new MemberCluster();
        clusterFields.setClusterName("3fields");
        clusterFields.addElements(fieldVec);
        String nestedString = clusterFields.toNestedString();
        System.out.println(nestedString);
        int indexName = nestedString.indexOf("3fields");
        int indexField1 = nestedString.indexOf("field1");
        int indexField2 = nestedString.indexOf("field2");
        int indexField3 = nestedString.indexOf("field3");
        assertTrue(indexName > -1);
        assertTrue(indexField1 > -1);
        assertTrue(indexField2 > -1);
        assertTrue(indexField3 > -1);

        String method1 = "method1";
        String method2 = "method2";
        String method3 = "method3";
        Vector<String> methodVec = new Vector<String>();
        methodVec.add(method2);
        methodVec.add(method3);
        methodVec.add(method1);
        MemberCluster clusterMethods = new MemberCluster();
        clusterMethods.setClusterName("3methods");
        clusterMethods.addElements(methodVec);
        
        clusterFields.addCluster(clusterMethods);
        nestedString = clusterFields.toNestedString();
        System.out.println(nestedString);

        indexField1 = nestedString.indexOf("field1");
        indexField2 = nestedString.indexOf("field2");
        indexField3 = nestedString.indexOf("field3");
        int indexMethod1 = nestedString.indexOf("method1");
        int indexMethod2 = nestedString.indexOf("method2");
        int indexMethod3 = nestedString.indexOf("method3");
        assertTrue(indexField1 > -1);
        assertTrue(indexField2 > -1);
        assertTrue(indexField3 > -1);
        assertTrue(indexMethod1 > -1);
        assertTrue(indexMethod2 > -1);
        assertTrue(indexMethod3 > -1);
    }

	@Test
	public void testToNewickString() {
		String field1 = "field1";
		String field2 = "field2";
		String field3 = "field3";
		Vector<String> fieldVec = new Vector<String>();
		fieldVec.add(field2);
		fieldVec.add(field3);
		fieldVec.add(field1);
		MemberCluster clusterFields = new MemberCluster();
		clusterFields.setClusterName("3fields");
		clusterFields.setDistance(0.3);
		clusterFields.addElements(fieldVec);
		String newickString = clusterFields.toNewickString();
		System.out.println(newickString);
		int indexComment = newickString.indexOf("0.3");
		int indexField1 = newickString.indexOf("field1");
		int indexField2 = newickString.indexOf("field2");
		int indexField3 = newickString.indexOf("field3");
		assertTrue(indexComment > -1);
		assertTrue(indexField1 > -1);
		assertTrue(indexField2 > -1);
		assertTrue(indexField3 > -1);

		String method1 = "method1";
		String method2 = "method2";
		String method3 = "method3";
		Vector<String> methodVec = new Vector<String>();
		methodVec.add(method2);
		methodVec.add(method3);
		methodVec.add(method1);
		MemberCluster clusterMethods = new MemberCluster();
		clusterMethods.setClusterName("3methods");
		clusterMethods.setDistance(0.5);
		clusterMethods.addElements(methodVec);

		clusterFields.addCluster(clusterMethods);
		newickString = clusterFields.toNewickString();
		System.out.println(newickString);

		indexField1 = newickString.indexOf("field1");
		indexField2 = newickString.indexOf("field2");
		indexField3 = newickString.indexOf("field3");
		int indexMethod1 = newickString.indexOf("method1");
		int indexMethod2 = newickString.indexOf("method2");
		int indexMethod3 = newickString.indexOf("method3");
		assertTrue(indexField1 > -1);
		assertTrue(indexField2 > -1);
		assertTrue(indexField3 > -1);
		assertTrue(indexMethod1 > -1);
		assertTrue(indexMethod2 > -1);
		assertTrue(indexMethod3 > -1);
	}

	@Test
	public void testGetClustersAtDistance() {
		String field1 = "field1";
		String field2 = "field2";
		String field3 = "field3";
		Vector<String> fieldVec = new Vector<String>();
		fieldVec.add(field2);
		fieldVec.add(field3);
		fieldVec.add(field1);
		MemberCluster clusterFields = new MemberCluster();
		clusterFields.setClusterName("3fields");
		clusterFields.setDistance(0.3);
		clusterFields.addElements(fieldVec);

		String method1 = "method1";
		String method2 = "method2";
		String method3 = "method3";
		Vector<String> methodVec = new Vector<String>();
		methodVec.add(method2);
		methodVec.add(method3);
		methodVec.add(method1);
		MemberCluster clusterMethods = new MemberCluster();
		clusterMethods.setClusterName("3methods");
		clusterMethods.setDistance(0.5);
		clusterMethods.addElements(methodVec);
		
		MemberCluster topCluster = new MemberCluster();
		topCluster.addCluster(clusterFields);
		topCluster.addCluster(clusterMethods);
		topCluster.setDistance(1.0);
		
		ArrayList<Object> subclusters = topCluster.getClustersAtDistance(1.0);
		assertEquals(1, subclusters.size());

		subclusters = topCluster.getClustersAtDistance(0.9);
		assertEquals(2, subclusters.size());
		assertTrue(subclusters.contains(clusterFields));
		assertTrue(subclusters.contains(clusterMethods));

		subclusters = topCluster.getClustersAtDistance(0.5);
		assertEquals(2, subclusters.size());
		assertTrue(subclusters.contains(clusterFields));
		assertTrue(subclusters.contains(clusterMethods));

		subclusters = topCluster.getClustersAtDistance(0.49);
		assertEquals(4, subclusters.size());
		assertTrue(subclusters.contains(clusterFields));
		assertTrue(subclusters.contains(method1));
		assertTrue(subclusters.contains(method2));
		assertTrue(subclusters.contains(method3));

		subclusters = topCluster.getClustersAtDistance(0.2);
		assertEquals(6, subclusters.size());
		assertTrue(subclusters.contains(field1));
		assertTrue(subclusters.contains(field2));
		assertTrue(subclusters.contains(field3));
		assertTrue(subclusters.contains(method1));
		assertTrue(subclusters.contains(method2));
		assertTrue(subclusters.contains(method3));
	}

    @Test
    public void testClusterSizesToString()
    {
        MemberCluster clusterFields1 = new MemberCluster();
        int elementCount = clusterFields1.getElementCount();
        assertEquals(0, elementCount);
        Set<String> elements = clusterFields1.getElements();
        assertEquals(0, elements.size());
        
        String field1 = "field1";
        String field2 = "field2";
        String field3 = "field3";
        Vector<String> fieldVec = new Vector<String>();
        fieldVec.add(field2);
        fieldVec.add(field3);
        fieldVec.add(field1);
        clusterFields1.setClusterName("3fields");
        clusterFields1.addElements(fieldVec);
        elementCount = clusterFields1.getElementCount();
        assertEquals(3, elementCount);
        elements = clusterFields1.getElements();
        
        MemberCluster clusterFields2 = new MemberCluster();
        clusterFields2.setClusterName("clusterFields2");
        clusterFields2.addElements(fieldVec);
        elementCount = clusterFields2.getElementCount();
        assertEquals(3, elementCount);

        String method1 = "method1";
        String method2 = "method2";
        Vector<String> methodVec = new Vector<String>();
        methodVec.add(method2);
        methodVec.add(method1);
        MemberCluster clusterMethods = new MemberCluster();
        clusterMethods.setClusterName("2methods");
        clusterMethods.addElements(methodVec);
        
        MemberCluster topCluster = new MemberCluster();
        topCluster.addCluster(clusterFields1);
        topCluster.addCluster(clusterMethods);
        elementCount = topCluster.getElementCount();
        assertEquals(5, elementCount);
        
        ArrayList<Object> clusters = new ArrayList<Object>();
        clusters.add(clusterFields1);
        clusters.add(topCluster);
        clusters.add(clusterFields2);
        clusters.add(clusterMethods);
        
        String sizesToString = MemberCluster.clusterSizesToString(clusters);
        System.out.println(sizesToString);
        assertEquals("5,3(2),2", sizesToString);
    }



}
