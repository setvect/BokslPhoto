package com.setvect.bokslphoto.vo;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.setvect.bokslphoto.BokslPhotoConstant;

/**
 * 사진 항목
 */
@Entity
@Table(name = "TBBA_PHOTO", indexes = { @Index(name = "INDEX_SHOT_DATE", columnList = "SHOT_DATE", unique = false) })
public class PhotoVo {
	/** MD5 값 */
	@Id
	@Column(name = "PHOTO_ID", length = 32)
	private String photoId;

	/** 파일경로 */
	@Column(name = "DIRECTORY", nullable = false, length = 500)
	private String directory;

	/** 파일이름 */
	@Column(name = "NAME", nullable = false, length = 500)
	private String name;

	/** 촬영일 */
	@Column(name = "SHOT_DATE", nullable = true)
	private Date shotDate;

	/** 촬영일 데이터 형태 */
	@Column(name = "SHOT_DATE_TYPE", nullable = true, length = 10)
	@Enumerated(EnumType.STRING)
	private ShotDateType shotDataType;

	/** 메모 */
	@Column(name = "MEMO", nullable = true, length = 500)
	private String memo;

	/** 위도 */
	@Column(name = "LATITUDE", nullable = true)
	private Double latitude;

	/** 경도 */
	@Column(name = "LONGITUDE", nullable = true)
	private Double longitude;

	/** 보호 이미지 */
	@Column(name = "PROTECT_F", nullable = false, length = 1)
	@Type(type = "yes_no")
	private boolean protectF;

	/** 등록일 */
	@Column(name = "REG_DATE", nullable = false)
	private Date regData;

	/** 이미지를 볼 수 없는 경우 true. 비공개 이미지 이면서 허가된 IP로 접속되지 않는 경우 보이지 않음. */
	@Transient
	private boolean deny;

	/** 해당 사진이 속한 폴더 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH })
	@JoinTable(name = "TBBC_MAPPING", joinColumns = @JoinColumn(name = "PHOTO_ID"), inverseJoinColumns = @JoinColumn(name = "FOLDER_SEQ"))
	private Set<FolderVo> folders;

	/**
	 * 촬영일 입력 데이터 형식
	 */
	public enum ShotDateType {
		/** 메타 정보 추출 */
		META,
		/** 사용자 입력 */
		MANUAL
	}

	/**
	 * @return MD5 값
	 */
	public String getPhotoId() {
		return photoId;
	}

	/**
	 * @param photoId
	 *            MD5 값
	 */
	public void setPhotoId(final String photoId) {
		this.photoId = photoId;
	}

	/**
	 * @return 파일경로
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * @param path
	 *            파일경로
	 */
	public void setDirectory(final String path) {
		this.directory = path;
	}

	/**
	 * @return 파일이름
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            파일이름
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return 촬영일
	 */
	public Date getShotDate() {
		return shotDate;
	}

	/**
	 * @param shotDate
	 *            촬영일
	 */
	public void setShotDate(final Date shotDate) {
		this.shotDate = shotDate;
	}

	/**
	 * @return 촬영일 데이터 형태
	 */
	public ShotDateType getShotDataType() {
		return shotDataType;
	}

	/**
	 * @param shotDataType
	 *            촬영일 데이터 형태
	 */
	public void setShotDataType(final ShotDateType shotDataType) {
		this.shotDataType = shotDataType;
	}

	/**
	 * @return 메모
	 */
	public String getMemo() {
		return memo;
	}

	/**
	 * @param memo
	 *            메모
	 */
	public void setMemo(final String memo) {
		this.memo = memo;
	}

	/**
	 * @return 보호 이미지
	 */
	public boolean isProtectF() {
		return protectF;
	}

	/**
	 * @param protectF
	 *            보호 이미지
	 */
	public void setProtectF(final boolean protectF) {
		this.protectF = protectF;
	}

	/**
	 * @return 등록일
	 */
	public Date getRegData() {
		return regData;
	}

	/**
	 * @param regData
	 *            등록일
	 */
	public void setRegData(final Date regData) {
		this.regData = regData;
	}

	/**
	 * @return 해당 사진이 속한 폴더
	 */
	public Set<FolderVo> getFolders() {
		return folders;
	}

	/**
	 * @param folders
	 *            해당 사진이 속한 폴더
	 */
	public void setFolders(final Set<FolderVo> folders) {
		this.folders = folders;
	}

	/**
	 * @param folder
	 *            폴더 등록
	 */
	public void addFolder(final FolderVo folder) {
		if (folders == null) {
			folders = new HashSet<>();
		}
		folders.add(folder);
	}

	/**
	 * @param f
	 *            삭제 할 폴더
	 */
	public void removeFolder(final FolderVo f) {
		if (folders == null) {
			return;
		}
		folders.remove(f);
	}

	/**
	 * @return 위도
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            위도
	 */
	public void setLatitude(final Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return 경도
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            경도
	 */
	public void setLongitude(final Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * 비공개 이미지 이면서 허가된 IP로 접속되지 않는 경우 보이지 않음. *
	 *
	 * @return 이미지를 볼 수 없는 경우 true.
	 */
	public boolean isDeny() {
		return deny;
	}

	/**
	 * 비공개 이미지 이면서 허가된 IP로 접속되지 않는 경우 보이지 않음. *
	 *
	 * @param deny
	 *            이미지를 볼 수 없는 경우 true.
	 */
	public void setDeny(boolean deny) {
		this.deny = deny;
	}

	/**
	 * @return 사진 경로. OS 기준 경로임.
	 */
	@JsonIgnore
	public File getFullPath() {
		File path = new File(BokslPhotoConstant.Photo.BASE_DIR, this.directory);
		return new File(path, this.name);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}