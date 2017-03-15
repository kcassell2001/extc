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

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class GoogleDistanceCalculatorTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testNormalizedGoogleDistance() {
		try {
			GoogleDistanceCalculator calculator = new GoogleDistanceCalculator();
			calculator.clearCache();
			String term1 = "betweenness";
			String term2 = "cluster";
			Double distance2terms = calculator.calculateDistance(term1, term2);
			System.out.println("Distance from " + term1 + " to " + term2 +
					" = " + distance2terms);

			term1 = "betweenness";
			term2 = "betweenness cluster";
			Double distanceSubsumedTerm1 = calculator.calculateDistance(term1, term2);
			System.out.println("Distance from " + term1 + " to " + term2 +
					" = " + distanceSubsumedTerm1);

			term1 = "betweenness";
			Double distanceSelf = calculator.calculateDistance(term1, term1);
			System.out.println("Distance from " + term1 + " to " + term1 +
					" = " + distanceSelf);

			term1 = "cluster";
			term2 = "betweenness cluster";
			Double distanceSubsumedTerm2 = calculator.calculateDistance(term1, term2);
			System.out.println("Distance from " + term1 + " to " + term2 +
					" = " + distanceSubsumedTerm2);

			term1 = "\"ugliest man in the world\"";
			term2 = "\"keith cassell\"";
			Double veryDistant = calculator.calculateDistance(term1, term2);
			System.out.println("Distance from " + term1 + " to " + term2 +
					" = " + veryDistant);

			term1 = "\"ugliest man in the world\"";
			term2 = "\"dr hook\"";
			Double hookDistant = calculator.calculateDistance(term1, term2);
			System.out.println("Distance from " + term1 + " to " + term2 +
					" = " + veryDistant);

			assertTrue(distanceSubsumedTerm2 > distanceSubsumedTerm1);
			assertTrue(distance2terms > distanceSubsumedTerm1);
			assertTrue(veryDistant > distance2terms);
			assertTrue(veryDistant > hookDistant);
			assertEquals(0.0, distanceSelf);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
