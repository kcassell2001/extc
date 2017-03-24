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

import java.util.HashMap;
import java.util.HashSet;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.cluster.MemberCluster;

import org.junit.Before;
import org.junit.Test;

public class LevenshteinDistanceCalculatorTest extends TestCase {
	
	private LevenshteinDistanceCalculator calc = null;
	private HashMap<String, MemberCluster> clusterHistory = null;
	
	@Before
	public void setUp() throws Exception {
		calc = new LevenshteinDistanceCalculator();
		clusterHistory = new HashMap<String, MemberCluster>();
	}

	@Test
	public void testCalculateDistance() {
        HashSet<String> fieldVec = new HashSet<String>();
        fieldVec.add("abcd");
        fieldVec.add("12345678");
        fieldVec.add("abcd5678");
        MemberCluster cluster1 = new MemberCluster();
        cluster1.addElements(fieldVec);
        cluster1.setClusterName("cluster1");
        clusterHistory.put("cluster1", cluster1);

		assertEquals(0.0, calc.calculateDistance("", ""));
		assertEquals(0.5, calc.calculateDistance("c", "cd"));
		assertEquals(1.0, calc.calculateDistance("", "cd"));
	}

	@Test
	public void testCalculateNormalizedDistance() {
		assertEquals(0.0, calc.calculateNormalizedDistance("", ""));
		assertEquals(1.0, calc.calculateNormalizedDistance("a", ""));
		assertEquals(0.0, calc.calculateNormalizedDistance("a", "a"));
		assertEquals(1.0, calc.calculateNormalizedDistance("a", "b"));
		assertEquals(0.5, calc.calculateNormalizedDistance("a", "aa"));
		assertEquals(1.0, calc.calculateNormalizedDistance("a", "bb"));
		assertEquals(0.5, calc.calculateNormalizedDistance("a", "ba"));
		assertEquals(0.75, calc.calculateNormalizedDistance("a", "bcad"));
		assertEquals(0.5, calc.calculateNormalizedDistance("abcd", "cbad"));
		assertEquals(0.75, calc.calculateNormalizedDistance("12345abc", "abc12345"));
		assertEquals(0.25, calc.calculateNormalizedDistance("123ab456", "123456"));
	}

}
