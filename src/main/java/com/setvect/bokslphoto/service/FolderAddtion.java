package com.setvect.bokslphoto.service;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.setvect.bokslphoto.vo.FolderVo;

/**
 * 분류 폴더에 부가적인 설정 값을 추가해 UI에서 이용
 */
public class FolderAddtion {
	/** 분류 폴더 */
	private final FolderVo folder;

	/** 트리 레벨. 0부터 시작 */
	private final int level;

	/** 선택 여부 */
	private final boolean select;

	/**
	 * @param folder
	 *            분류 폴더
	 * @param level
	 *            트리 레벨. 0부터 시작
	 * @param select
	 *            선택 여부
	 */
	public FolderAddtion(final FolderVo folder, final int level, final boolean select) {
		this.folder = folder;
		this.level = level;
		this.select = select;
	}

	/**
	 * @return the folder
	 */
	public FolderVo getFolder() {
		return folder;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return the select
	 */
	public boolean isSelect() {
		return select;
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
