/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Copyright (c) 2011, Keith Cassell
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

package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.apriori;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Based on the Apriori algorithm described in
 * Rakesh Agrawal and Ramakrishnan Srikant. "Fast algorithms for mining
 * association rules in large databases", Proceedings of the 20th International
 * Conference on Very Large Data Bases, VLDB, pages 487-499, Santiago, Chile,
 * September 1994.
 * 
 * @author kcassell
 * @see http://www.google.com/codesearch/p?hl=en#3XkT1SrHFLU/trunk/src/entities/InterHashTree.java&q=apriori%20hashtree%20lang:java&d=0
 * 
 */
public class AprioriClusterer<T> {
	
	/**
	 * Candidate item sets are stored in a hash tree which consists
	 * of interior nodes and leaf nodes.
	 * @author Keith Cassell
	 * @param <T>
	 */
	protected class HashTree {
		/** The root of the tree is depth 1 as per Agrawal. */
		protected InteriorNode<T> root = new InteriorNode<T>(1);;

		public HashTree() {
		}

		public void insert(AprioriItemSet<T> itemSet) {
			root.insert(itemSet);
		}
	}

	protected abstract static class HashTreeNode<T>
	{
		protected int depth;
		
        public abstract void insert(AprioriItemSet<T> itemSet);
	}

	/**
	 * An InteriorNode provides efficient access for locating an item
	 * set based on its members.  It hashes based on the item set's element
	 * stored at the specified depth.
	 */
	protected static class InteriorNode<T> extends HashTreeNode<T>
	{
		/** The nodes at the next depth level. */
		HashMap<T, HashTreeNode<T>> childNodes =
			new HashMap<T, AprioriClusterer.HashTreeNode<T>>();

		/**
		 * @param depth
		 */
		public InteriorNode(int depth) {
			super();
			this.depth = depth;
		}

		public void insert(AprioriItemSet<T> itemSet) {
			T itemAtDepth = itemSet.getItem(depth);
			HashTreeNode<T> child = childNodes.get(itemAtDepth);
			if (child == null) {
				int nextDepth = depth + 1;
				if (nextDepth < itemSet.size() - 1) {
					child = new InteriorNode<T>(nextDepth);
				} else {
					child = new LeafNode<T>(nextDepth);
				}
				childNodes.put(itemAtDepth, child);
			}
			child.insert(itemSet);
		}
	
	}

	/**
	 * A LeafNode contains item sets
	 */
	protected static class LeafNode<T> extends HashTreeNode<T>
	{
		protected HashMap<T, AprioriItemSet<T>> itemSets
		    = new HashMap<T, AprioriItemSet<T>>();

		public LeafNode(int d) {
			super();
			depth = d;
		}

		@Override
		public void insert(AprioriItemSet<T> itemSet) {
            itemSets.put(itemSet.getItem(depth), itemSet);
		}
	}	// end class LeafNode
	
	
	public List<AprioriItemSet<T>> generateItemSets(Collection<AprioriItemSet<T>> transactions,
			int support) {
		ArrayList<AprioriItemSet<T>> itemSets = new ArrayList<AprioriItemSet<T>>();
		Collection<AprioriItemSet<T>> largeItemSets =
			generateLargeOneItemSets(transactions, support);
		for (int k = 2; largeItemSets.size() > 0; k++) {
			Collection<AprioriItemSet<T>> candidates =
				generateCandidates(largeItemSets);
			// for all transactions
			//   Ct = subset(Ck, t) // candidates contained in t
			//   for all candidates, candidate.count++
			// largeItemSets = all candidate with count > support
		}
		return itemSets;
	}

	private List<AprioriItemSet<T>> generateLargeOneItemSets(Collection<AprioriItemSet<T>> transactions,
			int support) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Collection<AprioriItemSet<T>> generateCandidates(Collection<AprioriItemSet<T>> priorItemSets) {
		Collection<AprioriItemSet<T>> candidates = joinPriorItemSets(priorItemSets);
		candidates = pruneCandidates(candidates, priorItemSets);		
		return candidates;
	}

	/**
	 * Combines those item sets that are the same except for their last members.
	 * @param priorItemSets item sets with k-1 members
	 * @return candidate items sets with k members
	 */
	@SuppressWarnings("unchecked")
	protected Collection<AprioriItemSet<T>> joinPriorItemSets(Collection<AprioriItemSet<T>> priorItemSets) {
		Collection<AprioriItemSet<T>> candidates = new HashSet<AprioriItemSet<T>>();
		
		for (AprioriItemSet<T> itemSet1 : priorItemSets) {
			T last1 = itemSet1.last();
			SortedSet<T> allButLast1 = itemSet1.allButLast();
			
			for (AprioriItemSet<T> itemSet2 : priorItemSets) {
				T last2 = itemSet2.last();
				SortedSet<T> allButLast2 = itemSet2.allButLast();
				
				if (allButLast1.equals(allButLast2) && 
						((Comparable<T>)last1).compareTo(last2) < 0) {
					// TODO increase efficiency by adding to existing item set
					// instead of creating new objects?
					TreeSet<T> candidateItems = new TreeSet<T>(itemSet1.getItems());
					candidateItems.add(last2);
					AprioriItemSet<T> candidate = new AprioriItemSet<T>(candidateItems);
					candidates.add(candidate);
				}
			}
		}
		return candidates;
	}
	
	protected Collection<AprioriItemSet<T>> pruneCandidates(Collection<AprioriItemSet<T>> candidates,
			Collection<AprioriItemSet<T>> priorItemSets) {
		Iterator<AprioriItemSet<T>> iterator = candidates.iterator();
		while (iterator.hasNext()) {
			AprioriItemSet<T> itemSet = iterator.next();
			if (!passesSubsetTest(itemSet, priorItemSets)) {
				iterator.remove();
			}
		}
		return candidates;
	}

	protected boolean passesSubsetTest(AprioriItemSet<T> itemSet,
			Collection<AprioriItemSet<T>> priorItemSets) {
		SortedSet<? extends T> items = itemSet.getItems();
		ArrayList<T> itemList = new ArrayList<T>(items);
		List<List<T>> combinations = getCombinations(itemList);
		boolean allIn = true;
		
		// make sure all subsets of the item set of size k - 1
		// are present in the priorItemSets
		for(int i = 0; i < combinations.size() && allIn; i++) {
			List<T> subList = combinations.get(i);
			allIn = inPriorItemSets(subList, priorItemSets);
		}
		return allIn;
	}

	protected boolean inPriorItemSets(List<T> subList,
			Collection<AprioriItemSet<T>> priorItemSets) {
		boolean allIn = true;
		Iterator<AprioriItemSet<T>> iterator = priorItemSets.iterator();
//		while (allIn && iterator.hasNext()) {
//			AprioriItemSet<T> itemSet = iterator.next();
//			allIn = 
//		}
		return allIn;
	}

	/**
	 * @param items a list of items
	 * @return all sublists of items where each sublist has one fewer element
	 * than items
	 */
	protected static <T> List<List<T>> getCombinations(List<T> items) {
		List<List<T>> combinations = new ArrayList<List<T>>();
		
		for (int i = 0; i < items.size(); i++) {
			ArrayList<T> sublist = new ArrayList<T>(items);
			sublist.remove(i);
			combinations.add(sublist);
		}
		return combinations;
	}

}
