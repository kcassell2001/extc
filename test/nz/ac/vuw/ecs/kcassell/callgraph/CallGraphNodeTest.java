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

import junit.framework.TestCase;

import org.junit.Test;

public class CallGraphNodeTest extends TestCase {

	@Test
	public void testScore() {
		CallGraphNode node = new CallGraphNode();
		node.setScore("A", 2.0);
		Number score = node.getScore("A");
		assertEquals(2.0, score);
		score = node.getScore("B");
		assertNull(score);
	}

	@Test
	public void testUserData() {
		CallGraphNode node = new CallGraphNode();
		Object userData = node.getUserData();
		assertNull(userData);
		node.setUserData("dat");
		userData = node.getUserData();
		assertEquals("dat", userData);
	}

	@Test
	public void testToNestedString() {
		CallGraphNode node = new CallGraphNode();
		node.setSimpleName("simon");
		String string = node.toString();
		assertTrue(string.indexOf("simon") >= 0);
	}

	@Test
	public void testToString() {
		CallGraphNode node = new CallGraphNode();
		node.setSimpleName("simon");
		String string = node.toString();
		assertTrue(string.indexOf("simon") >= 0);
	}

	@Test
	public void testNodeType() {
		CallGraphNode node = new CallGraphNode();
		NodeType nodeType = node.getNodeType();
		assertEquals(NodeType.UNKNOWN, nodeType);
		node.setNodeType(NodeType.FIELD);
		nodeType = node.getNodeType();
		assertEquals(NodeType.FIELD, nodeType);
	}

}
