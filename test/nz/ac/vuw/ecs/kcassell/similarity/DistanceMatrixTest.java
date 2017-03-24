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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.cluster.Distance;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.junit.Before;
import org.junit.Test;

public class DistanceMatrixTest extends TestCase {

	private static final String B1 = "b1";
	private static final String A4 = "a4";
	private static final String A0 = "a0";

	private DistanceMatrix<String> matrix = null;
	
	@Before
    public void setUp()
    {
		List<String> headers = new ArrayList<String>();
		headers.add(A0);
		headers.add(A4);
		headers.add(B1);
		matrix = new DistanceMatrix<String>(headers );
    }

	@Test
	public void testGetIndex() {
		assertEquals(0, matrix.getIndex(A0));
		assertEquals(1, matrix.getIndex(A4));
		assertEquals(2, matrix.getIndex(B1));
	}

	@Test
	public void testSetDistance() {
		matrix.setDistance(A0, A4, 4);
		matrix.setDistance(A0, B1, 1);
		matrix.setDistance(A4, B1, 3);
		assertEquals(4.0, matrix.getDistance(A0, A4));
		assertEquals(1.0, matrix.getDistance(A0, B1));
		assertEquals(3.0, matrix.getDistance(A4, B1));
		assertEquals(RefactoringConstants.UNKNOWN_DISTANCE, matrix.getDistance(B1, B1));
		assertEquals(RefactoringConstants.UNKNOWN_DISTANCE, matrix.getDistance(A4, "eh?"));
		
	}

	@Test
	public void testFindNearest() {
		matrix.setDistance(A0, A4, 4);
		matrix.setDistance(A0, B1, 1);
		matrix.setDistance(A4, B1, 3);
		Distance<String> nearest = matrix.findNearest();
		String first = nearest.getFirst();
		String second = nearest.getSecond();
		assertTrue((first.equals(A0) && second.equals(B1))
				|| (second.equals(A0) || first.equals(B1)));
	}

	@Test
	public void testGetHeaders() {
		List<String> headers = matrix.getHeaders();
		assertTrue(headers.contains(A0));
		assertTrue(headers.contains(A4));
		assertTrue(headers.contains(B1));
		assertTrue(!headers.contains("eh?"));
	}

	@Test
	public void testToString() {
		matrix.setDistance(A0, A4, 4.4);
		matrix.setDistance(A0, B1, 1.23456);
		matrix.setDistance(A4, B1, 3.3);
		String string = matrix.toString();
		System.out.println(string);
		int a0index = string.indexOf(A0);
		int a1index = string.indexOf("a1");
		int a4index = string.indexOf(A4);
		int v3index = string.indexOf("3.3");
		assertTrue(a0index > -1);
		assertTrue(a1index == -1);
		assertTrue(a4index > -1);
		assertTrue(v3index > -1);
	}

}
