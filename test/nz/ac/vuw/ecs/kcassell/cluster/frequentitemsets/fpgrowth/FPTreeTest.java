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

package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class FPTreeTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testInsert() {
		FPTree tree = new FPTree();
		FPTreeNode root = tree.getRoot();
		ArrayList<String> items1 = new ArrayList<String>();
		items1.add("a");
		items1.add("b");
		items1.add("c");
		tree.insert(items1, root, 1);
		System.out.println(tree.toString());
		Collection<FPTreeNode> rootChildren = root.getChildren();
		assertEquals(1, rootChildren.size());
		FPTreeNode childA = rootChildren.iterator().next();
		assertEquals("a", childA.getItemName());
		assertEquals(1, childA.getSupport());

		Collection<FPTreeNode> aChildren = childA.getChildren();
		assertEquals(1, aChildren.size());
		Iterator<FPTreeNode> iterator = aChildren.iterator();
		FPTreeNode childB = iterator.next();
		assertEquals("b", childB.getItemName());
		assertEquals(1, childB.getSupport());

		Collection<FPTreeNode> bChildren = childB.getChildren();
		assertEquals(1, bChildren.size());
		FPTreeNode childC = bChildren.iterator().next();
		assertEquals("c", childC.getItemName());
		assertEquals(1, childC.getSupport());

		Collection<FPTreeNode> cChildren = childC.getChildren();
		assertNull(cChildren);

		ArrayList<String> items2 = new ArrayList<String>();
		items2.add("a");
		items2.add("c");
		items2.add("d");
		tree.insert(items2, root, 1);
		System.out.println(tree.toString());
		rootChildren = root.getChildren();
		assertEquals(1, rootChildren.size());
		childA = rootChildren.iterator().next();
		assertEquals("a", childA.getItemName());
		assertEquals(2, childA.getSupport());

		aChildren = childA.getChildren();
		assertEquals(2, aChildren.size());
		iterator = aChildren.iterator();
		FPTreeNode childA0 = iterator.next();
		FPTreeNode childA1 = iterator.next();
		assertTrue(("b".equals(childA0.getItemName())
				    && "c".equals(childA1.getItemName()))
				    || ("c".equals(childA0.getItemName())
						&& "b".equals(childA1.getItemName())));
		assertEquals(1, childB.getSupport());

		Set<Entry<String,ArrayList<FPTreeNode>>> entrySet = 
			tree.headerTable.entrySet();
		assertEquals(4, entrySet.size());
		assertTrue(tree.headerTable.containsKey("a"));
		assertTrue(tree.headerTable.containsKey("b"));
		assertTrue(tree.headerTable.containsKey("c"));
		assertTrue(tree.headerTable.containsKey("d"));
		assertTrue(!tree.headerTable.containsKey("e"));
	}

}
