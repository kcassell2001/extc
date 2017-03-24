package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;

import org.junit.Test;

/**
 * The intermediate results in these tests will be different than those
 * in Han's paper, because our sorting takes into account both the
 * frequency of occurrence and the item name.  Han's sorting is based on
 * frequency of occurrence and then the order in which the items were see
 * in the transactions.  Our method will generate the same FPTree
 * regardless of the ordering of the transactions; Han's will not.
 * @author kcassell
 *
 */
public class FPGrowthMinerTest extends TestCase {
	
	// Used by setUpTransactionsM1M4
	Collection<ItemSupportList> transactions = null;
	ItemSupportList t1 = null;
	ItemSupportList t2 = null;
	ItemSupportList t3 = null;
	ItemSupportList t4 = null;

	// Used by setUpTransactionsHan
	Collection<ItemSupportList> transactionsHan = null;
	ItemSupportList t100 = null;
	ItemSupportList t200 = null;
	ItemSupportList t300 = null;
	ItemSupportList t400 = null;
	ItemSupportList t500 = null;

	private void setUpTransactionsM1M4() {
		transactions = new ArrayList<ItemSupportList>();
		t1 = new ItemSupportList("client1", null, null);
		transactions.add(t1);
		
		ArrayList<String> members2 = new ArrayList<String>();
		members2.add("m1");
		members2.add("m2");
		t2 = new ItemSupportList("client1", members2, null);
		transactions.add(t2);

		ArrayList<String> members3 = new ArrayList<String>();
		members3.add("m2");
		members3.add("m3");
		t3 = new ItemSupportList("client1", members3, null);
		transactions.add(t3);

		ArrayList<String> members4 = new ArrayList<String>();
		members4.add("m1");
		members4.add("m2");
		members4.add("m3");
		members4.add("m4");
		t4 = new ItemSupportList("client1", members4, null);
		transactions.add(t4);
	}

	/**
	 * Creates the transactions used in Han's paper.
	 */
	private void setUpTransactionsHan() {
		transactionsHan = new ArrayList<ItemSupportList>();

		ArrayList<String> members100 = new ArrayList<String>();
		members100.add("f");
		members100.add("a");
		members100.add("c");
		members100.add("d");
		members100.add("g");
		members100.add("i");
		members100.add("m");
		members100.add("p");
		t100 = new ItemSupportList("t100", members100, null);
		transactionsHan.add(t100);
		
		ArrayList<String> members200 = new ArrayList<String>();
		members200.add("a");
		members200.add("b");
		members200.add("c");
		members200.add("f");
		members200.add("l");
		members200.add("m");
		members200.add("o");
		t200 = new ItemSupportList("t200", members200, null);
		transactionsHan.add(t200);

		ArrayList<String> members300 = new ArrayList<String>();
		members300.add("b");
		members300.add("f");
		members300.add("h");
		members300.add("j");
		members300.add("o");
		t300 = new ItemSupportList("t300", members300, null);
		transactionsHan.add(t300);

		ArrayList<String> members400 = new ArrayList<String>();
		members400.add("b");
		members400.add("c");
		members400.add("k");
		members400.add("s");
		members400.add("p");
		t400 = new ItemSupportList("t400", members400, null);
		transactionsHan.add(t400);

		ArrayList<String> members500 = new ArrayList<String>();
		members500.add("a");
		members500.add("f");
		members500.add("c");
		members500.add("e");
		members500.add("l");
		members500.add("p");
		members500.add("m");
		members500.add("n");
		t500 = new ItemSupportList("t500", members500, null);
		transactionsHan.add(t500);

	}

	/*
	 * builds:
FPTreeNode RootNode:0 [, children: (f:1 c:4)]
  FPTreeNode f:1 [parentNode=RootNode:0, children: (b:1)]
    FPTreeNode b:1 [parentNode=f:1]
  FPTreeNode c:4 [parentNode=RootNode:0, children: (f:3 b:1)]
    FPTreeNode f:3 [parentNode=c:4, children: (a:3)]
      FPTreeNode a:3 [parentNode=f:3, children: (b:1 m:2)]
        FPTreeNode b:1 [parentNode=a:3, children: (m:1)]
          FPTreeNode m:1 [parentNode=b:1]
        FPTreeNode m:2 [parentNode=a:3, children: (p:2)]
          FPTreeNode p:2 [parentNode=m:2]
    FPTreeNode b:1 [parentNode=c:4, children: (p:1)]
      FPTreeNode p:1 [parentNode=b:1]
	 */
	@Test
	public void testBuildFPTreeFromFrequentItems() {
		setUpTransactionsHan();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems =
			miner.getFrequentItems(transactionsHan, 3);
//		frequentItems.setComparator(miner.comparator);
		FPTree tree =
			miner.buildFPTreeFromFrequentItems(transactionsHan, frequentItems);

//		System.out.println("testBuildFPTreeFromFrequentItems tree =\n" + tree);

		HashMap<String,ArrayList<FPTreeNode>> headerTable = tree.getHeaderTable();
		Set<String> keySet = headerTable.keySet();
		assertEquals(6, keySet.size());
		ArrayList<FPTreeNode> cSibs = headerTable.get("c");
		assertEquals(1, cSibs.size());
		ArrayList<FPTreeNode> fSibs = headerTable.get("f");
		assertEquals(2, fSibs.size());
		ArrayList<FPTreeNode> aSibs = headerTable.get("a");
		assertEquals(1, aSibs.size());
		ArrayList<FPTreeNode> bSibs = headerTable.get("b");
		assertEquals(3, bSibs.size());
		ArrayList<FPTreeNode> mSibs = headerTable.get("m");
		assertEquals(2, mSibs.size());
		ArrayList<FPTreeNode> pSibs = headerTable.get("p");
		assertEquals(2, pSibs.size());

		ItemSupportList frequentTreeItems = tree.getFrequentItems();
		List<String> treeItems = frequentItems.getItems();
		assertEquals(6, treeItems.size());
		assertEquals("c", treeItems.get(0));
		assertEquals("f", treeItems.get(1));
		assertEquals("a", treeItems.get(2));
		assertEquals("b", treeItems.get(3));
		assertEquals("m", treeItems.get(4));
		assertEquals("p", treeItems.get(5));
		assertEquals(4.0, frequentTreeItems.getSupport("f"));
		assertEquals(4.0, frequentTreeItems.getSupport("c"));
		assertEquals(3.0, frequentTreeItems.getSupport("a"));
		assertEquals(3.0, frequentTreeItems.getSupport("b"));
		assertEquals(3.0, frequentTreeItems.getSupport("m"));
		assertEquals(3.0, frequentTreeItems.getSupport("p"));
		
		// Children of root
		FPTreeNode root = tree.getRoot();
		Collection<FPTreeNode> children = root.getChildren();
		assertEquals(2, children.size());
		FPTreeNode cChild = root.getChild("c");
		assertNotNull(cChild);
		assertEquals(4, cChild.getSupport());
		FPTreeNode fChild = root.getChild("f");
		assertNotNull(fChild);
		assertEquals(1, fChild.getSupport());
		FPTreeNode zChild = root.getChild("z");
		assertNull(zChild);
		
		// grandchildren of root
		children = fChild.getChildren();
		assertEquals(1, children.size());
		FPTreeNode bChild = fChild.getChild("b");
		assertNotNull(bChild);
		assertEquals(1, bChild.getSupport());
		assertNull(bChild.getChildren());

		children = cChild.getChildren();
		assertEquals(2, children.size());
		bChild = cChild.getChild("b");
		assertNotNull(bChild);
		fChild = cChild.getChild("f");
		assertEquals(3, fChild.getSupport());
		assertNotNull(fChild);
		
		// great-grandchildren of root
		children = fChild.getChildren();
		assertEquals(1, children.size());
		FPTreeNode aChild = fChild.getChild("a");
		assertNotNull(aChild);
		assertEquals(3, aChild.getSupport());
		assertEquals(2, aChild.getChildren().size());

		children = bChild.getChildren();
		assertEquals(1, children.size());
		assertNotNull(bChild.getChild("p"));
	}

	@Test
	public void testGeneratePatternB() {
		setUpTransactionsM1M4();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList patternB1 =
			miner.generatePatternB("testGeneratePatternB1", 2.0 , t2);
		List<String> items = patternB1.getItems();
		assertEquals(3, items.size());
		assertEquals("m1", items.get(0));
		assertEquals("m2", items.get(1));
		assertEquals("testGeneratePatternB1", items.get(2));
		assertEquals(2.0, patternB1.getSupport("m1"));
		assertEquals(2.0, patternB1.getSupport("m2"));
		assertEquals(2.0, patternB1.getSupport("testGeneratePatternB1"));

		ItemSupportList patternB2 =
			miner.generatePatternB("testGeneratePatternB2", 1.0, patternB1);
		items = patternB2.getItems();
		assertEquals(4, items.size());
		assertEquals("m1", items.get(0));
		assertEquals("m2", items.get(1));
		assertEquals("testGeneratePatternB1", items.get(2));
		assertEquals("testGeneratePatternB2", items.get(3));
		assertEquals(1.0, patternB2.getSupport("m1"));
		assertEquals(1.0, patternB2.getSupport("m2"));
		assertEquals(1.0, patternB2.getSupport("testGeneratePatternB1"));
		assertEquals(1.0, patternB2.getSupport("testGeneratePatternB2"));
//		System.out.println("testGeneratePatternB2 =\n" + patternB2);
	}
	
	@Test
	public void testGetItemsDecreasingFrequency() {
		setUpTransactionsM1M4();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems = miner.getItemsDecreasingFrequency(transactions);
//		System.out.println("frequentItems = " + frequentItems);
		List<String> members = frequentItems.getItems();
		assertEquals("m2", members.get(0));
		assertEquals(3.0, frequentItems.getSupport("m2"), 0.00001);
		assertEquals("m1", members.get(1));
		assertEquals(2.0, frequentItems.getSupport("m1"), 0.00001);
		assertEquals("m3", members.get(2));
		assertEquals(2.0, frequentItems.getSupport("m3"), 0.00001);
		assertEquals("m4", members.get(3));
		assertEquals(1.0, frequentItems.getSupport("m4"), 0.00001);
	}
	
	@Test
	public void testGetFrequentItems() {
		setUpTransactionsM1M4();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems =
			miner.getFrequentItems(transactions, 1);
//		System.out.println("frequentItems = " + frequentItems);
		List<String> members = frequentItems.getItems();
		assertEquals(4, members.size());
		assertEquals("m2", members.get(0));
		assertEquals(3.0, frequentItems.getSupport("m2"), 0.00001);
		assertEquals("m1", members.get(1));
		assertEquals(2.0, frequentItems.getSupport("m1"), 0.00001);
		assertEquals("m3", members.get(2));
		assertEquals(2.0, frequentItems.getSupport("m3"), 0.00001);
		assertEquals("m4", members.get(3));
		assertEquals(1.0, frequentItems.getSupport("m4"), 0.00001);

		frequentItems = miner.getFrequentItems(transactions, 3);
//		System.out.println("frequentItems = " + frequentItems);
		members = frequentItems.getItems();
		assertEquals(1, members.size());
		assertEquals("m2", members.get(0));

		frequentItems = miner.getFrequentItems(transactions, 2);
//		System.out.println("frequentItems = " + frequentItems);
		members = frequentItems.getItems();
		assertEquals(3, members.size());
		assertEquals("m2", members.get(0));
		assertEquals(3.0, frequentItems.getSupport("m2"), 0.00001);
		assertEquals("m1", members.get(1));
		assertEquals(2.0, frequentItems.getSupport("m1"), 0.00001);
		assertEquals("m3", members.get(2));
		assertEquals(2.0, frequentItems.getSupport("m3"), 0.00001);
	}
	
	@Test
	public void testConstructConditionalPattern() {
		setUpTransactionsHan();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems =
			miner.getFrequentItems(transactionsHan, 3);
		Comparator<String> comparator = frequentItems.getComparator();
		miner.comparator = comparator;
		FPTree tree =
			miner.buildFPTreeFromFrequentItems(transactionsHan, frequentItems);
		
		List<String> items = new ArrayList<String>();
		items.add("z");
		ItemSupportList patternZ =
			new ItemSupportList("patternZ", items , comparator);
		Collection<ItemSupportList> patterns =
			miner.constructConditionalPatternBase(tree, patternZ);
		assertEquals(0, patterns.size());
		
		items = new ArrayList<String>();
		items.add("c");
		ItemSupportList patternC =
			new ItemSupportList("patternC", items , comparator);
		patterns = miner.constructConditionalPatternBase(tree, patternC);
		assertEquals(1, patterns.size());
		ItemSupportList pattern1 = patterns.iterator().next();
		items = pattern1.getItems();
		assertEquals(0, items.size());
		
		items = new ArrayList<String>();
		items.add("p");
		ItemSupportList patternP =
			new ItemSupportList("patternP", items , comparator);
		patterns = miner.constructConditionalPatternBase(tree, patternP);
		assertEquals(2, patterns.size());
		Iterator<ItemSupportList> iterator = patterns.iterator();
		while (iterator.hasNext()) {
			pattern1 = iterator.next();
//			System.out.println("pattern1 = " + pattern1);
			items = pattern1.getItems();
			if (items.size() == 4) {
				assertEquals("c", items.get(0));
				assertEquals("f", items.get(1));
				assertEquals("a", items.get(2));
				assertEquals("m", items.get(3));
			} else if (items.size() == 2) {
				assertEquals("c", items.get(0));
				assertEquals("b", items.get(1));
			} else {
				fail("unexpected pattern " + pattern1);
			}
		}
	}
	
	@Test
	public void testPruneAndSortItems() {
		setUpTransactionsM1M4();
		FPGrowthMiner miner = new FPGrowthMiner();
		// Establish the ordering based on the frequencies
		// of occurrence of items
		miner.getItemsDecreasingFrequency(transactions);
/*		frequentItems = ItemSupportList
			m2:	3.0
			m1:	2.0
			m3:	2.0
			m4:	1.0		*/
		ItemSupportList frequentItems = miner.getFrequentItems(transactions, 0);
		ItemSupportList revisedT2 = miner.pruneAndSortItems(t2, frequentItems);
		List<String> members2 = revisedT2.getItems();
		assertEquals("m2", members2.get(0));
		assertEquals("m1", members2.get(1));
		
		ItemSupportList revisedT3 = miner.pruneAndSortItems(t3, frequentItems);
		List<String> members3 = revisedT3.getItems();
		assertEquals("m2", members3.get(0));
		assertEquals("m3", members3.get(1));
		
		ItemSupportList revisedT4 = miner.pruneAndSortItems(t4, frequentItems);
		List<String> members4 = revisedT4.getItems();
		assertEquals("m2", members4.get(0));
		assertEquals("m1", members4.get(1));
		assertEquals("m3", members4.get(2));
		assertEquals("m4", members4.get(3));

		frequentItems = miner.getFrequentItems(transactions, 3);
		revisedT2 = miner.pruneAndSortItems(t2, frequentItems);
		members2 = revisedT2.getItems();
		assertEquals(1, members2.size());
		assertEquals("m2", members2.get(0));
		
		revisedT3 = miner.pruneAndSortItems(t3, frequentItems);
		members3 = revisedT3.getItems();
		assertEquals(1, members3.size());
		assertEquals("m2", members3.get(0));
		
		revisedT4 = miner.pruneAndSortItems(t4, frequentItems);
		members4 = revisedT4.getItems();
		assertEquals(1, members4.size());
		assertEquals("m2", members4.get(0));
	}

	@Test
	public void testGetPatternsEndingWithItem () {
		setUpTransactionsHan();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems =
			miner.getFrequentItems(transactionsHan, 3);
		miner.comparator = frequentItems.getComparator();
		FPTree tree =
			miner.buildFPTreeFromFrequentItems(transactionsHan, frequentItems);
		
		Collection<ItemSupportList> patterns =
			miner.getPatternsEndingWithItem("z", tree);
		assertEquals(0, patterns.size());
		
		patterns = miner.getPatternsEndingWithItem("c", tree);
		assertEquals(1, patterns.size());
		ItemSupportList pattern1 = patterns.iterator().next();
		List<String> items = pattern1.getItems();
		assertEquals(1, items.size());
		String name = items.get(0);
		assertEquals("c", name);
		
		patterns = miner.getPatternsEndingWithItem("p", tree);
		assertEquals(2, patterns.size());
		Iterator<ItemSupportList> iterator = patterns.iterator();
		while (iterator.hasNext()) {
			pattern1 = iterator.next();
//			System.out.println("pattern1 = " + pattern1);
			items = pattern1.getItems();
			if (items.size() == 5) {
				assertEquals("c", items.get(0));
				assertEquals("f", items.get(1));
				assertEquals("a", items.get(2));
				assertEquals("m", items.get(3));
				assertEquals("p", items.get(4));
			} else if (items.size() == 3) {
				assertEquals("c", items.get(0));
				assertEquals("b", items.get(1));
				assertEquals("p", items.get(2));
			} else {
				fail("unexpected pattern " + pattern1);
			}
		}
	}
	
	@Test
	public void testGenerateCombinations() {
		List<String> prefix = new ArrayList<String>();
		List<String> items = new ArrayList<String>();
		items.add("a");
		items.add("b");
		items.add("c");
		items.add("d");
		List<List<String>> itemCombos = new ArrayList<List<String>>();
		FPGrowthMiner miner = new FPGrowthMiner();
		List<List<String>> combinations =
			miner.generateBranchCombinations(prefix, items, itemCombos);
//		System.out.println("generated combos = " + combinations);
		assertEquals(15, combinations.size());
		String string = combinations.toString();
		assertTrue(string.contains("[a]"));
		assertTrue(string.contains("[a, b]"));
		assertTrue(string.contains("[a, b, c]"));
		assertTrue(string.contains("[a, b, c, d]"));
		assertTrue(string.contains("[a, b, d]"));
		assertTrue(string.contains("[a, c]"));
		assertTrue(string.contains("[a, c, d]"));
		assertTrue(string.contains("[a, d]"));
		assertTrue(string.contains("[b]"));
		assertTrue(string.contains("[b, c]"));
		assertTrue(string.contains("[b, c, d]"));
		assertTrue(string.contains("[b, d]"));
		assertTrue(string.contains("[c]"));
		assertTrue(string.contains("[c, d]"));
		assertTrue(string.contains("[d]"));
	}

	@Test
	public void testGenerateCombinationsTree() {
		setUpTransactionsM1M4();
		ArrayList<ItemSupportList> transactions = new ArrayList<ItemSupportList>();
		transactions.add(t2);
		transactions.add(t4);
		FPGrowthMiner miner = new FPGrowthMiner();
		FPTree tree = miner.buildFPTree(transactions, 1);
		Collection<ItemSupportList> combos = miner.generateBranchPatterns(tree, 1);
//		System.out.println("Combos =\n" + combos);
		assertEquals(15, combos.size());
	}

	@Test
	public void testMine() {
		setUpTransactionsHan();
		FPGrowthMiner miner = new FPGrowthMiner();
		Collection<ItemSupportList> combos = miner.mine(transactionsHan, 3);
		String resultString = patternsToString(combos);
//		System.out.println("Combos =\n" + resultString);
		assertEquals(18, combos.size());
		assertTrue(resultString.contains("[p]: 3.0"));
		assertTrue(resultString.contains("[c, p]: 3.0"));
		assertTrue(resultString.contains("[m]: 3.0"));
		assertTrue(resultString.contains("[a, m]: 3.0"));
		assertTrue(resultString.contains("[c, a, m]: 3.0"));
		assertTrue(resultString.contains("[c, f, a, m]: 3.0"));
		assertTrue(resultString.contains("[f, a, m]: 3.0"));
		assertTrue(resultString.contains("[c, m]: 3.0"));
		assertTrue(resultString.contains("[c, f, m]: 3.0"));
		assertTrue(resultString.contains("[f, m]: 3.0"));
		assertTrue(resultString.contains("[b]: 3.0"));
		assertTrue(resultString.contains("[a]: 3.0"));
		assertTrue(resultString.contains("[c, a]: 3.0"));
		assertTrue(resultString.contains("[c, f, a]: 3.0"));
		assertTrue(resultString.contains("[f, a]: 3.0"));
		assertTrue(resultString.contains("[f]: 4.0"));
		assertTrue(resultString.contains("[c, f]: 3.0"));
		assertTrue(resultString.contains("[c]: 4.0"));
		
		combos = miner.mine(transactionsHan, 4);
		resultString = patternsToString(combos);
		System.out.println("Combos =\n" + resultString);
		assertEquals(2, combos.size());
		assertTrue(resultString.contains("[f]: 4.0"));
		assertTrue(resultString.contains("[c]: 4.0"));
	}

	/**
	 * Creates a string representation for a collection of frequent item lists.
	 * All items in the frequent item lists are assumed to have the same quantity.
	 * @param combos a collection of frequent item lists.
	 * @return a String with one frequent item list per line
	 */
	private static String patternsToString(Collection<ItemSupportList> combos) {
		StringBuffer buf = new StringBuffer();
		for (ItemSupportList combo : combos) {
			List<String> items = combo.getItems();
			buf.append(items + ": " + combo.getSupport(items.get(0)) + "\n");
		}
		String resultString = buf.toString();
		return resultString;
	}
}
