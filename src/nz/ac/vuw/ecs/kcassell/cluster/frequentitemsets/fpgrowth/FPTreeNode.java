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

import java.util.Collection;
import java.util.HashMap;

public class FPTreeNode {
	
	/** The item identifier. */
	protected String itemName;
	
	/** The support count - the number of transactions represented by
	 * the portion of the path reaching this node. */
	protected int support;
	
	/** The link to the previous node in one or more transaction sequences.  */
	protected FPTreeNode parentNode = null;

	/** The links to the next nodes in one or more transaction sequences.  */
	protected HashMap<String, FPTreeNode> children = null;

//	/** The forward link to the next node in a linked list of nodes with same
//	 * itemName.  */
//	Not needed - info is in the list maintained by the headerTable
//	protected FPTreeNode nodeLink = null;

	
	/**
	 * Three argument constructor.
	 * 
	 * @param name
	 *            the itemset identifier.
	 * @param support
	 *            the support value for the itemset.
	 * @param backRef
	 *            the backward link to the parent node.
	 */

	public FPTreeNode(String name, int support, FPTreeNode parentNode) {
		itemName = name;
		this.support = support;
		this.parentNode = parentNode;
		if (parentNode != null) {
			parentNode.addChild(this);
		}
	}
	
	private void addChild(FPTreeNode node) {
		if (children == null) {
			children = new HashMap<String, FPTreeNode>();
		}
		children.put(node.itemName, node);
}

	public void increaseCount(int i) {
		support += i;
	}

	public FPTreeNode getChild(String name) {
		FPTreeNode child = null;
		if (children != null) {
			child = children.get(name);
		}
		return child;
	}
	
	public Collection<FPTreeNode> getChildren() {
		Collection<FPTreeNode> result = null;
		if (children != null) {
			result = children.values();
		}
		return result;
	}

	public String getItemName() {
		return itemName;
	}

	public int getSupport() {
		return support;
	}

	public FPTreeNode getParentNode() {
		return parentNode;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(
		 "FPTreeNode " + itemName + ":" + support + " [");
		if (parentNode != null) {
			buf.append("parentNode=" + parentNode.itemName + ":" + parentNode.support);
		}
		if (children != null && children.size() > 0) {
			buf.append(", children: (");
			for (FPTreeNode child : children.values()) {
				buf.append(child.itemName + ":" + child.support + " ");
			}
			buf.deleteCharAt(buf.length()-1);
			buf.append(")");
		}
		buf.append("]");
		return buf.toString();
	}
}
