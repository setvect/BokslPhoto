package com.setvect.bokslphoto.service;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.setvect.bokslphoto.util.SearchListParam;

/**
 * 포토 검색 조건
 */
public class PhotoSearchParam extends SearchListParam {

	/** 시작 날짜 */
	private Date searchFrom;

	/** 끝 날짜 */
	private Date searchTo;

	/** 메모 조건 */
	private String searchMemo;

	/** 디레토리 조건 */
	private String searchDirectory;

	public PhotoSearchParam(int startCursor, int returnCount) {
		super(startCursor, returnCount);
	}

	public Date getSearchFrom() {
		return searchFrom;
	}

	public void setSearchFrom(Date searchFrom) {
		this.searchFrom = searchFrom;
	}

	public Date getSearchTo() {
		return searchTo;
	}

	public void setSearchTo(Date searchTo) {
		this.searchTo = searchTo;
	}

	public String getSearchMemo() {
		return searchMemo;
	}

	public void setSearchMemo(String searchMemo) {
		this.searchMemo = searchMemo;
	}

	public String getSearchDirectory() {
		return searchDirectory;
	}

	public void setSearchDirectory(String searchDirectory) {
		this.searchDirectory = searchDirectory;
	}

	/**
	 * 날짜 범위 검색 여부
	 *
	 * @return
	 */
	public boolean isDateBetween() {
		return searchFrom != null && searchTo != null;
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
