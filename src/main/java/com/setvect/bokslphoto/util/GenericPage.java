package com.setvect.bokslphoto.util;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Page에서 검색된 객체들의 타입을 지정함
 *
 * @param <T>
 *            아이템 객체
 */
public class GenericPage<T> {

	/** 목록 객체 */
	private List<T> objects;
	/** 시작 항목(1부터 시작) */
	private int startCursor;
	/** 전체 항목 수 */
	private int totalCount;
	/** 한 페이지당 가져올 항목 수 */
	private int returnCount;

	/**
	 * @param objects
	 *            리스트
	 * @param startCursor
	 *            시작 항목(1부터 시작)
	 * @param totalCount
	 *            전체 항목 수
	 */
	public GenericPage(final List<T> objects, final int startCursor, final int totalCount) {
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
	 * @param returnCount
	 *            한 페이지당 가져올 항목 수
	 */
	public GenericPage(final List<T> objects, final int startCursor, final int totalCount, final int returnCount) {
		this.objects = objects;
		this.startCursor = startCursor;
		this.totalCount = totalCount;
		this.returnCount = returnCount;
	}

	/**
	 * @return 목록 값
	 */
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