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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class IdentifierDistanceCalculatorTest {

	public static final double DOUBLE_TOLERANCE = 0.0001;

	@Test
	public void testGetProperties() {
		IdentifierDistanceCalculator calc = new IdentifierDistanceCalculator();
		Set<String> props = calc.getProperties("myMethod");
		assertEquals(2, props.size());
		assertTrue(props.contains("my"));
		assertTrue(props.contains("method"));
		
		props = calc.getProperties("myMethods");
		assertEquals(2, props.size());
		assertTrue(props.contains("my"));
		assertTrue(props.contains("method"));
		
		props = calc.getProperties("myACM_Method2");
		assertEquals(3, props.size());
		assertTrue(props.contains("my"));
		assertTrue(props.contains("acm"));
		assertTrue(props.contains("method2"));
	}

	@Test
	public void testCalculateDistance() {
		IdentifierDistanceCalculator calc = new IdentifierDistanceCalculator();
		Double distance = calc.calculateDistance("myMethod", "myMethod");
		assertEquals(0.0, distance.doubleValue(), DOUBLE_TOLERANCE);
		distance = calc.calculateDistance("myMethod", "myMethods");
		assertEquals(0.0, distance.doubleValue(), DOUBLE_TOLERANCE);
		distance = calc.calculateDistance("myMethod", "myACMMethodWorks");
		assertEquals(0.5, distance.doubleValue(), DOUBLE_TOLERANCE);
		distance = calc.calculateDistance("myMethod", "zax");
		assertEquals(1.0, distance.doubleValue(), DOUBLE_TOLERANCE);
		distance = calc.calculateDistance("myMethod_wasGood", "_myMethodsIs_Good_");
		assertEquals(0.4, distance.doubleValue(), DOUBLE_TOLERANCE);
	}

}
