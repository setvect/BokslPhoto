package com.setvect.bokslphoto.vo;

import java.io.File;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 사진 물리적인 경로
 */
public class PhotoDirectory {
	/** 기준 경로로 부터 전체 패스 */
	private final String fullPath;

	/** 마지막 경로 이름 */
	private final String name;

	/** 이미지 갯수 */
	private final int photoCount;

	/**
	 * @param fullPath
	 *            기준 경로로 부터 전체 패스
	 * @param photoCount
	 *            경로에 속한 사진 갯수
	 */
	public PhotoDirectory(final String fullPath, final int photoCount) {
		this.fullPath = fullPath;
		File f = new File(fullPath);
		this.name = f.getName();
		this.photoCount = photoCount;
	}

	/**
	 * @return 기준 경로로 부터 전체 패스
	 */
	public String getFullPath() {
		return fullPath;
	}

	/**
	 * @return 마지막 경로 이름
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return 해당 경로에 속한 사진 갯수
	 */
	public int getPhotoCount() {
		return photoCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PhotoDirectory other = (PhotoDirectory) obj;
		if (fullPath == null) {
			if (other.fullPath != null) {
				return false;
			}
		} else if (!fullPath.equals(other.fullPath)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
