package com.setvect.bokslphoto.test.etc;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.setvect.bokslphoto.util.TreeNode;

public class TreeNodeTestCase {
	@Test
	public void test() {
		// TODO Auto-generated method stub
		TreeNode<String> root = new TreeNode<String>("root");
		TreeNode<String> node1 = root.addChild("node1");
		TreeNode<String> node2 = root.addChild("node2");
		TreeNode<String> node3 = root.addChild("node3");
		TreeNode<String> node4 = node1.addChild("node4");
		TreeNode<String> node5 = node1.addChild("node5");
		TreeNode<String> node6 = node3.addChild("node6");

		List<String> path = node6.getPath();
		String s = StringUtils.join(path, " > ");
		System.out.println(s);

		TreeNode<String> findNode = root.getTreeNode("node3");

		Assert.assertThat(findNode.getData(), CoreMatchers.is("node3"));
		findNode = node4.getTreeNode("node3");

		Assert.assertNull(findNode);

		Iterator<TreeNode<String>> iter = root.iterator();

		while (iter.hasNext()) {
			TreeNode<String> next = iter.next();

			String depthPadding = String.join("", Collections.nCopies(next.getLevel(), "--"));

			System.out.println(depthPadding + next.getData());
		}
		System.out.println("끝.");
	}
}
