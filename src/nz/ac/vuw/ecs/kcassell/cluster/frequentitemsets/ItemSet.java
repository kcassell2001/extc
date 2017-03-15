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

package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A sorted collection of items.
 * @author kcassell
 *
 */
public class ItemSet<T> {
	
//	/** The sorted collection of items represented as a list. */
//	protected List<T> itemsList = new ArrayList<T>();

	/** The sorted collection of items represented as a set. */
	protected SortedSet<T> items = new TreeSet<T>();
	
//	/** The number of "transactions" that contain this item set.
//	 * (In the Apriori paper, this is a percentage.) */
//	protected int support = 0;
	
	/**
	 * Builds an ItemSet using the provided items
	 * @param items
	 */
	public ItemSet(SortedSet<T> items) {
		this.items = items;
	}

	/**
	 * Builds an ItemSet using the provided items
	 * @param items
	 */
	public ItemSet(Collection<T> items) {
		this.items = new TreeSet<T>(items);
	}

	public SortedSet<T> getItems() {
		return items;
	}
	
	public void add(T item) {
		items.add(item);
	}

	/**
	 * @return the last element in the item set
	 */
	public T last() {
		T last = null;
		
		if (items.size() > 0) {
			last = items.last();
		}
		return last;
	}
	
	/**
	 * @return all of the items except the last one
	 */
	public SortedSet<T> allButLast() {
		SortedSet<T> result = items.headSet(items.last());
		return result;
	}
	
	public String toString() {
		return items.toString();
	}

	public T getItem(int i) {
		T result = null;
		if (i < items.size()) {
			ArrayList<T> list = new ArrayList<T>(items);
			result = list.get(i);
		}
		return result;
	}

	public int size() {
		return items.size();
	}
}
