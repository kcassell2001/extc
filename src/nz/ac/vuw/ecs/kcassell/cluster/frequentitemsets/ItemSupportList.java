package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class associates a transaction's (client class's calls) items (class members)
 * with a number representing its count (or support, or ...).
 * @author Keith Cassell
 */
public class ItemSupportList {
	
	/** The name of the client class (transaction) */
	protected String name = null;
	
	/** Keeps track of the support for each item (called method).
	 * The key is the client method's handle; the value is its support. */
	protected Map<String, Double> supportMap = new HashMap<String, Double>();
	
	/** Indicates whether the members list has been updated since the suportMap
	 * has changed.
	 */
	protected boolean isDirty = true;

	/**
	 * This is an ordered list of the items. It serves as a cache of items in
	 * the support Map and is dependent on the isDirty flag, so it is not guaranteed
	 * to be up to date.
	 */
	protected List<String> items = new ArrayList<String>();

	/** The comparator that will be used to sort the members.  If null,
	 * the default lexical comparator will be used on the keys. */
	protected Comparator<String> comparator = null;

	/**
	 * @param name
	 * @param items (called members) in the transaction
	 */
	public ItemSupportList(String name, Collection<String> items,
			Comparator<String> comparator) {
		super();
		this.name = name;
		this.comparator = comparator;

		if (items != null) {
			for (String item : items) {
				supportMap.put(item, 1.0);
			}
		}
		isDirty = true;
	}
	
	/**
	 * @param name
	 * @param items (called members) in the transaction
	 */
	public ItemSupportList(String name, Collection<String> items,
			Double support, Comparator<String> comparator) {
		super();
		this.name = name;
		this.comparator = comparator;

		if (items != null) {
			for (String item : items) {
				supportMap.put(item, support);
			}
		}
		isDirty = true;
	}
	
	public Double addSupport(String handle, Double newSupport) {
		Double support = newSupport;
		if (supportMap.containsKey(handle)) {
			Double oldSupport = supportMap.get(handle);
			if (oldSupport != null) {
				support = newSupport.doubleValue() + oldSupport.doubleValue();
			}
		}
		supportMap.put(handle, support);
		isDirty = true;
		return support;
	}
	
	public void setSupport(String handle, Double newSupport) {
		if (newSupport == null) {
			supportMap.remove(handle);
		} else {
			supportMap.put(handle, newSupport);
		}
		isDirty = true;
	}
	
	public Double getSupport(String handle) {
		return supportMap.get(handle);
	}
	
	public Map<String, Double> getSupportMap() {
		return supportMap;
	}

	public Comparator<String> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<String> comparator) {
		if (this.comparator != comparator) {
			isDirty = true;
			this.comparator = comparator;
		}
	}

	/**
	 * 
	 * @param byWhat indicates whether the keys, values, or neither determines the
	 * ordering of the results
	 * @return the keys ordered based on the byWhat value
	 */
	public List<String> sortItems() {
		items = new ArrayList<String>(supportMap.keySet());
		if (comparator != null) {
			Collections.sort(items, comparator);
		} else {
			Collections.sort(items);
		}
		isDirty = false;
		return items;
	}

	public List<String> getItems() {
		if (isDirty) {
			sortItems();
		}
		return items;
	}
	
	public String getName() {
		return name;
	}


	/**
	 * Creates a string representation for a collection of frequent item lists.
	 * All items in the frequent item lists are assumed to have the same quantity.
	 * @param combos a collection of frequent item lists.
	 * @return a String with one frequent item list per line
	 */
	public static String patternsToString(Collection<ItemSupportList> combos) {
		StringBuffer buf = new StringBuffer();
		for (ItemSupportList combo : combos) {
			List<String> items = combo.getItems();
			buf.append(items + ": " + combo.getSupport(items.get(0)) + "\n");
		}
		String resultString = buf.toString();
		return resultString;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(
				"ItemSupportList [name=" + name + ", " +
				((comparator == null) ? "null comparator"
						: comparator.getClass().getSimpleName() + comparator.hashCode()) +
				", supportMap=\n");
		if (isDirty) {
			sortItems();
		}
		for (String member : items) {
			buf.append("\t").append(member);
			buf.append(":\t").append(supportMap.get(member)).append("\n");
		}
		return buf.toString();
	}

}
