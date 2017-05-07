package com.setvect.bokslphoto.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Tree 자료구조
 */
public class TreeNode<T> implements Iterable<TreeNode<T>> {
	private T data;
	@JsonIgnore
	private TreeNode<T> parent;
	private List<TreeNode<T>> children;

	/**
	 * 최초 루트 만들 때
	 *
	 * @param data
	 */
	public TreeNode(T data) {
		this.data = data;
		this.children = new ArrayList<TreeNode<T>>();
	}

	public TreeNode<T> addChild(T child) {
		TreeNode<T> childNode = new TreeNode<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}

	public List<TreeNode<T>> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public T getData() {
		return data;
	}

	/**
	 * 트리 레벨. 0부터 시작
	 *
	 * @return
	 */
	@JsonIgnore
	public int getLevel() {
		return getPath().size() - 1;
	}

	/**
	 * 루트 카테고리리 부터 현재 카테고리까지 경로를 구한다.
	 *
	 * @return 카테고리 경로<br>
	 *         root > depth1 > depth2 ...
	 */
	@JsonIgnore
	public List<T> getPath() {
		List<T> result = new ArrayList<>();

		TreeNode<T> current = this;
		result.add(current.data);
		while (!current.isRoot()) {
			current = current.parent;
			result.add(current.data);
		}
		Collections.reverse(result);
		return result;
	}

	/**
	 * @return 최상단 자료이면 true, 아니면 false
	 */
	@JsonIgnore
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * @param data
	 * @return
	 */
	public TreeNode<T> getTreeNode(T data) {
		List<TreeNode<T>> list = exploreTree();
		Optional<TreeNode<T>> result = list.stream().filter(p -> p.data.equals(data)).findAny();
		return result.orElse(null);
	}

	/**
	 * 전위 순회 방식으로 제공
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<TreeNode<T>> iterator() {
		List<TreeNode<T>> nodeList = exploreTree();
		return nodeList.iterator();
	}

	public String printData() {
		StringBuffer result = new StringBuffer();
		Iterator<TreeNode<T>> iter = iterator();
		while (iter.hasNext()) {
			TreeNode<T> next = iter.next();
			String depthPadding = String.join("", Collections.nCopies(next.getLevel(), "--"));
			result.append(depthPadding + next.getData() + "\n");
		}
		return result.toString();
	}

	/**
	 * 트리 전체를 탐색
	 *
	 * @return
	 */
	public List<TreeNode<T>> exploreTree() {
		TreeNode<T> currentNode = TreeNode.this;
		List<TreeNode<T>> nodeList = new ArrayList<>();
		addNode(currentNode, nodeList);
		return nodeList;
	}

	private void addNode(TreeNode<T> node, List<TreeNode<T>> nodeList) {
		nodeList.add(node);
		node.children.stream().forEach(p -> addNode(p, nodeList));
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
