package com.setvect.bokslphoto.controller;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 날짜 그룹핑 건수.
 */
public class GroupByDate {
	/** 시작 날짜: yyyyMMdd */
	private String from;

	/** 시작 날짜: yyyyMMdd */
	private String to;

	/** 건수 */
	private int count;

	/**
	 * @return 시작일
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from
	 *            시작일
	 */
	public void setFrom(final String from) {
		this.from = from;
	}

	/**
	 * @return 종요일
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to
	 *            종요일
	 */
	public void setTo(final String to) {
		this.to = to;
	}

	/**
	 * @return 건수
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count
	 *            건수
	 */
	public void setCount(final int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
