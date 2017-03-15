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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;

public class FPTree {
	/** The name of the root of the FPTree. */
	public static final String ROOT_NAME = "RootNode";
	
	/** All of the items in the tree, in decreasing order of support.
	 * NB: this is set by the FPGrowthMiner.buildFPTree algorithm! */
	protected ItemSupportList frequentItems = null;
	
	/** The key is an item name; the value is a list of nodes
	 * having that item name. */
	protected HashMap<String, ArrayList<FPTreeNode>> headerTable =
		new HashMap<String, ArrayList<FPTreeNode>>();
	
	/** The root of the FP tree */
	protected FPTreeNode root =
		new FPTreeNode(ROOT_NAME, 0, null);
	
	/** True when the tree is just a single straight branch; false
	 * otherwise. */
	private boolean hasOneBranch = true;
	
	private static final String SPACES =
		"                                                                        ";
	private static final String EOLN = System.getProperty("line.separator");
	
	public FPTree() {
//		addToHeaderTable(ROOT_NAME, root);
	}

	public FPTreeNode getRoot() {
		return root;
	}

	public HashMap<String, ArrayList<FPTreeNode>> getHeaderTable() {
		return headerTable;
	}

	/** @return all of the items in the tree, in decreasing order of support.
	 * NB: this is set by the FPGrowthMiner.buildFPTree algorithm! */
	public ItemSupportList getFrequentItems() {
		return frequentItems;
	}

	/** param the items in the tree, in decreasing order of support.
	 * NB: this is to be used exclusively by the FPGrowthMiner.buildFPTree
	 *  algorithm! */
	public void setFrequentItems(ItemSupportList frequentItems) {
		this.frequentItems = frequentItems;
	}

	public boolean hasOneBranch() {
		return hasOneBranch;
	}
	
	/**
	 * @return true when the tree has something in it besides the root,
	 * i.e. it has at least one frequent item.
	 */
	public boolean hasFrequentItems() {
		return headerTable.size() > 0;
	}
	
	/**
	 * Implements the "insert_tree" function for
	 * inserting the items of a pseudo-transaction into the FPTree.
	 * @param items the frequent items for a transaction, in decreasing
	 * frequency
	 * @param parent the node representing the previous item in the sequence
	 */
	public void insert(List<String> items, FPTreeNode parent, int support) {
		if (items.size() > 0) {
			String item = items.get(0);
			
			// Stop if it's an infrequent item
			if ((frequentItems == null) 
					|| (frequentItems.getSupport(item) != null)) {
				FPTreeNode child = parent.getChild(item);
				
				// New child - previously unseen sequence
				if (child == null) {
					child = new FPTreeNode(item, support, parent);
					boolean wasAdded = addToHeaderTable(item, child);
					// If the item was already in the header table, the tree
					// is branching.
					hasOneBranch = hasOneBranch && wasAdded;
				} else { // Existing node - increment count
					child.increaseCount(support);
				}
				// recurse on tail
				insert(items.subList(1, items.size()), child, support);
			}
		}
	}

	/**
	 * Makes sure the given item is in the header table.
	 * @param item the item for the header table
	 * @param node the node containing item
	 * @return true if item was added (not previously in the header table);
	 * false otherwise
	 */
	private boolean addToHeaderTable(String item, FPTreeNode node) {
		boolean addIt = !headerTable.containsKey(item);
		ArrayList<FPTreeNode> list = null;
		if (addIt) {
			list = new ArrayList<FPTreeNode>();
		} else {
			list = headerTable.get(item);
		}
		list.add(node);
		headerTable.put(item, list);
		return addIt;
	}
	
	@Override
	public String toString() {
		StringBuffer buf =
			new StringBuffer("FPTree [headerTable = " + headerTable + EOLN);
		treeToString(buf, root, 0);
		return buf.toString();
	}

	private String treeToString(StringBuffer buf, FPTreeNode node, int indent) {
		String spaces = SPACES;
		if (indent < SPACES.length() / 2) {
			spaces = SPACES.substring(0, 2 * indent);
		}
		buf.append(spaces).append(node).append(EOLN);
		Collection<FPTreeNode> children = node.getChildren();
		if (children != null) {
			for (FPTreeNode child : children) {
				treeToString(buf, child, indent + 1);
			}
		}
		return buf.toString();
	}

	/**
	 * Find all the paths through the tree that terminate at item
	 * @param item the least frequently occurring item in the patterns
	 * to be returned
	 * @return the patterns
	 */
	public Collection<ItemSupportList> getPatternsEndingWithItem(String item,
			Comparator<String> comparator) {
		ArrayList<ItemSupportList> patterns = new ArrayList<ItemSupportList>();
		ArrayList<FPTreeNode> itemNodes = headerTable.get(item);
		
		if (itemNodes != null) {
			// Go "sideways" through the "siblings" (nodes with the same names),
			// collecting paths to the root.
			for (FPTreeNode sibling : itemNodes) {
				FPTreeNode nodePtr = sibling;
				String nodeName = sibling.getItemName();
				ArrayList<String> path = new ArrayList<String>();
				ItemSupportList pathSupport =
					new ItemSupportList("pathFrom" + nodeName, path, comparator);
				
				// For each sibling node, move towards the root, collecting the items
				while (!nodeName.equals(ROOT_NAME)) {
					path.add(0, nodeName);
					int support = nodePtr.getSupport();
					pathSupport.setSupport(nodeName, support * 1.0);
					nodePtr = nodePtr.getParentNode();
					nodeName = nodePtr.getItemName();
				}
				patterns.add(pathSupport);
			}
		}
		return patterns;
	}

	/**
	 * Find all the paths through the tree that terminate at item, not
	 * including item itself.  All nodes in the path are assigned the
	 * support that item has.
	 * @param item the least frequently occurring item in the patterns
	 * to be returned
	 * @return the patterns
	 */
	public Collection<ItemSupportList> getConditionalPatternBase(String item,
			Comparator<String> comparator) {
		ArrayList<ItemSupportList> patterns = new ArrayList<ItemSupportList>();
		ArrayList<FPTreeNode> itemNodes = headerTable.get(item);
		
		if (itemNodes != null) {
			// Go "sideways" through the "siblings" (nodes with the same names),
			// collecting paths to the root.
			for (FPTreeNode sibling : itemNodes) {
				Double support = sibling.getSupport() * 1.0;
				FPTreeNode nodePtr = sibling.getParentNode();
				String nodeName = nodePtr.getItemName();
				ArrayList<String> path = new ArrayList<String>();
				ItemSupportList pathSupport =
					new ItemSupportList("pathFrom" + nodeName + ":" + support,
							path, comparator);
				
				// For each sibling node, move towards the root, collecting the items
				while (!nodeName.equals(ROOT_NAME)) {
					path.add(0, nodeName);
					pathSupport.setSupport(nodeName, support);
					nodePtr = nodePtr.getParentNode();
					nodeName = nodePtr.getItemName();
				}
				patterns.add(pathSupport);
			}
		}
		return patterns;
	}
}
