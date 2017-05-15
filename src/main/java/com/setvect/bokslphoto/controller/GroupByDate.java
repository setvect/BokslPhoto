package com.setvect.bokslphoto.controller;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 날짜 그룹핑 건수
 */
public class GroupByDate {
	/** 시작 날짜: yyyyMMdd */
	public String from;

	/** 시작 날짜: yyyyMMdd */
	public String to;

	/** 건수 */
	public int count;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
