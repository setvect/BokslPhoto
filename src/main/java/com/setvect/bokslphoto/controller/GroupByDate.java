package com.setvect.bokslphoto.controller;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 날짜 그룹핑 건수.
 */
public class GroupByDate {
	/** 시작 날짜*/
	private Date from;

	/** 시작 날짜*/
	private Date to;

	/** 건수 */
	private int count;

	/**
	 * @return 시작일
	 */
	public Date getFrom() {
		return from;
	}

	/**
	 * @param from
	 *            시작일
	 */
	public void setFrom(final Date from) {
		this.from = from;
	}

	/**
	 * @return 종료일
	 */
	public Date getTo() {
		return to;
	}

	/**
	 * @param to
	 *            종요일
	 */
	public void setTo(final Date to) {
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
