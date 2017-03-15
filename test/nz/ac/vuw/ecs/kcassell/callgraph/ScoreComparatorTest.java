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

package nz.ac.vuw.ecs.kcassell.callgraph;

import static org.junit.Assert.*;

import org.junit.Test;

public class ScoreComparatorTest {

    ScoreComparator comparator = new ScoreComparator();

	@Test
	public void testCompare() {
        CallGraphNode node1 = new CallGraphNode();
        node1.setScore(ScoreType.BASIC, 1.0);
        CallGraphNode node2 = new CallGraphNode();
        node2.setScore(ScoreType.BASIC, 2.0);
        CallGraphNode node3 = new CallGraphNode();
        node3.setScore(ScoreType.BASIC, 1.0);
        assertEquals(1.0, node1.getScore(ScoreType.BASIC));
        assertEquals(2.0, node2.getScore(ScoreType.BASIC));
        assertEquals(1.0, node3.getScore(ScoreType.BASIC));
        
		comparator.setScoreBeingCompared(ScoreType.HUB);
        assertTrue(comparator.compare(node1, node2) < 0);
        assertTrue(comparator.compare(node1, node3) < 0);
        assertTrue(comparator.compare(node2, node3) < 0);
		comparator.setScoreBeingCompared(ScoreType.BASIC);
        assertTrue(comparator.compare(node1, node2) < 0);
        assertTrue(comparator.compare(node1, node3) < 0);
        assertTrue(comparator.compare(node2, node3) > 0);
        
        node1.setId(1);
        node2.setId(2);
        node3.setId(3);
        assertTrue(comparator.compare(node1, node2) < 0);
        assertTrue(comparator.compare(node1, node3) < 0);
        assertTrue(comparator.compare(node2, node3) > 0);
        comparator.setAscending(false);
        assertTrue(comparator.compare(node1, node2) > 0);
        assertTrue(comparator.compare(node1, node3) > 0);
        assertTrue(comparator.compare(node2, node3) < 0);
        
        //lastName(43.0); getPersonInfo(71.0); getPersonName(9.0)
        CallGraphNode node43 = new CallGraphNode();
        node43.setScore(ScoreType.BASIC, 43.0);
        node43.setId(43);
        CallGraphNode node71 = new CallGraphNode();
        node71.setScore(ScoreType.BASIC, 71.0);
        node71.setId(7);
        CallGraphNode node9 = new CallGraphNode();
        node9.setScore(ScoreType.BASIC, 9.0);
        node9.setId(9);
        assertTrue(comparator.compare(node43, node71) > 0);
        assertTrue(comparator.compare(node43, node9) < 0);
        assertTrue(comparator.compare(node9, node43) > 0);
	}

}
