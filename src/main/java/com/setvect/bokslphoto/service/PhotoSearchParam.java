package com.setvect.bokslphoto.service;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.setvect.bokslphoto.BokslPhotoConstant;
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

	/** 분류 조건 */
	private int searchFolderSeq;

	/** 그룹 분류 */
	private DateGroup searchDateGroup;

	/**
	 *
	 */
	public PhotoSearchParam() {
		super(0, BokslPhotoConstant.WEB.DEFAULT_PAGE_SIZE);
	}

	/**
	 * @param startCursor
	 *            시작 지점 (0부터 시작)
	 * @param returnCount
	 *            가져올 항목 갯수
	 */
	public PhotoSearchParam(final int startCursor, final int returnCount) {
		super(startCursor, returnCount);
	}

	/**
	 * @return 시작일
	 */
	public Date getSearchFrom() {
		return searchFrom;
	}

	/**
	 * @param searchFrom
	 *            시작일
	 */
	public void setSearchFrom(final Date searchFrom) {
		this.searchFrom = searchFrom;
	}

	/**
	 * @return 종료일
	 */
	public Date getSearchTo() {
		return searchTo;
	}

	/**
	 * @param searchTo
	 *            종료일
	 */
	public void setSearchTo(final Date searchTo) {
		this.searchTo = searchTo;
	}

	/**
	 * @return 메모 검색 조건
	 */
	public String getSearchMemo() {
		return searchMemo;
	}

	/**
	 * @param searchMemo
	 *            메모 검새 조건
	 */
	public void setSearchMemo(final String searchMemo) {
		this.searchMemo = searchMemo;
	}

	/**
	 * @return 디렉토리 검색 조건
	 */
	public String getSearchDirectory() {
		return searchDirectory;
	}

	/**
	 * @param searchDirectory
	 *            디렉토리 검색 조건
	 */
	public void setSearchDirectory(final String searchDirectory) {
		this.searchDirectory = searchDirectory;
	}

	/**
	 * @return 날짜 범위 검색 여부
	 */
	public boolean isDateBetween() {
		return searchFrom != null && searchTo != null;
	}

	/**
	 * @return 촬영 날짜가 없는 사진 검색
	 */
	public boolean isDateNoting() {
		// 날짜 범위가 없는 값(1970-01-01 00:00:00)이면 촬영 날짜 선택이 안되었다고 판단
		Calendar cal = Calendar.getInstance();
		int offset = cal.getTimeZone().getRawOffset();
		boolean dateNoting = (searchFrom.getTime() + offset) == 0 && (searchTo.getTime() + offset) == 0;
		return dateNoting;
	}

	/**
	 * @return 검색 폴더 일련번호
	 */
	public int getSearchFolderSeq() {
		return searchFolderSeq;
	}

	/**
	 * @param searchFolderSeq
	 *            검색 폴더 일련번호
	 */
	public void setSearchFolderSeq(final int searchFolderSeq) {
		this.searchFolderSeq = searchFolderSeq;
	}

	/**
	 * @return 데이터 그룹핑 조건
	 */
	public DateGroup getSearchDateGroup() {
		return searchDateGroup;
	}

	/**
	 * @param searchDateGroup
	 *            데이터 그룹핑 조건
	 */
	public void setSearchDateGroup(final DateGroup searchDateGroup) {
		this.searchDateGroup = searchDateGroup;
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
