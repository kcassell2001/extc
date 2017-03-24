package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.junit.Test;

public class FPTreeNodeTest {

	@Test
	public void testConstructor() {
		FPTreeNode root = new FPTreeNode("root", 1, null);
		FPTreeNode child = new FPTreeNode("child", 2, root);
		assertNull(root.getParentNode());
		Collection<FPTreeNode> rootChildren = root.getChildren();
		assertEquals(1, rootChildren.size());
		assertEquals(1, root.getSupport());
		assertEquals("root", root.getItemName());
		FPTreeNode node = root.getChild("child");
		assertNotNull(node);
		assertEquals("child", node.getItemName());
		node = root.getChild("nowhwre");
		
		FPTreeNode parentNode = child.getParentNode();
		assertNotNull(parentNode);
		assertEquals("root", parentNode.getItemName());
		assertNull(child.getChildren());
		assertNull(child.getChild("nowhwre"));
		assertEquals(2, child.getSupport());
		assertEquals("child", child.getItemName());

		FPTreeNode grandchild = new FPTreeNode("grandchild", 666, child);
		parentNode = grandchild.getParentNode();
		assertNotNull(parentNode);
		assertEquals("child", parentNode.getItemName());
		assertNull(grandchild.getChildren());
		assertNull(grandchild.getChild("nowhwre"));
		assertEquals(666, grandchild.getSupport());
		assertEquals("grandchild", grandchild.getItemName());

		assertNotNull(child.getChildren());
		assertNotNull(child.getChild("grandchild"));
		assertNull(child.getChild("nowhwre"));
		assertEquals(2, child.getSupport());
		assertEquals("child", child.getItemName());
	}

}
