package com.setvect.bokslphoto.util;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Page에서 검색된 객체들의 타입을 지정함
 */
public class GenericPage<T> {

	private List<T> objects;
	private int startCursor;
	private int totalCount;
	private int returnCount;

	/**
	 * @param objects
	 *            리스트
	 * @param startCursor
	 *            시작 항목(1부터 시작)
	 * @param totalCount
	 *            전체 항목 수
	 */
	public GenericPage(List<T> objects, int startCursor, int totalCount) {
		this.objects = objects;
		this.startCursor = startCursor;
		this.totalCount = totalCount;
	}

	/**
	 * @param objects
	 *            리스트
	 * @param startCursor
	 *            시작 항목(1부터 시작)
	 * @param totalCount
	 *            전체 항목 수
	 */
	public GenericPage(List<T> objects, int startCursor, int totalCount, int returnCount) {
		this.objects = objects;
		this.startCursor = startCursor;
		this.totalCount = totalCount;
		this.returnCount = returnCount;
	}

	public List<T> getList() {
		return objects;
	}

	/**
	 * @return the startCursor
	 */
	public int getStartCursor() {
		return startCursor;
	}

	/**
	 * @return the totalCount
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * @return the returnCount
	 */
	public int getReturnCount() {
		return returnCount;
	}

	/**
	 * 전체 페이지 개수
	 *
	 * @return the returnCount
	 */
	public int getPageCount() {
		if (returnCount == 0) {
			return 0;
		}
		int page = (int) Math.ceil((double) totalCount / returnCount);
		return page;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}