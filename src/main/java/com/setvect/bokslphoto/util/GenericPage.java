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
	private List<T> list;
	/** 시작 항목(1부터 시작) */
	private int startCursor;
	/** 전체 항목 수 */
	private int totalCount;

	/**
	 * @param list
	 *            리스트
	 * @param startCursor
	 *            시작 항목(1부터 시작)
	 * @param totalCount
	 *            전체 항목 수
	 */
	public GenericPage(final List<T> list, final int startCursor, final int totalCount) {
		this.list = list;
		this.startCursor = startCursor;
		this.totalCount = totalCount;
	}

	/**
	 * @return 목록 값
	 */
	public List<T> getList() {
		return list;
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