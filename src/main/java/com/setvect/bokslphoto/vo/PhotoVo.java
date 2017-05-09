package com.setvect.bokslphoto.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Index;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Type;

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

	/** */
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "TBBC_MAPPING", joinColumns = @JoinColumn(name = "PHOTO_ID"), inverseJoinColumns = @JoinColumn(name = "FOLDER_SEQ"))
	private List<FolderVo> folders;

	/**
	 * 촬영일 입력 데이터 형식
	 */
	public enum ShotDateType {
		/** 메타 정보 추출 */
		META,
		/** 사용자 입력 */
		MANUAL
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String path) {
		this.directory = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getShotDate() {
		return shotDate;
	}

	public void setShotDate(Date shotDate) {
		this.shotDate = shotDate;
	}

	public ShotDateType getShotDataType() {
		return shotDataType;
	}

	public void setShotDataType(ShotDateType shotDataType) {
		this.shotDataType = shotDataType;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public boolean isProtectF() {
		return protectF;
	}

	public void setProtectF(boolean protectF) {
		this.protectF = protectF;
	}

	public Date getRegData() {
		return regData;
	}

	public void setRegData(Date regData) {
		this.regData = regData;
	}

	public List<FolderVo> getFolders() {
		return folders;
	}

	public void setFolders(List<FolderVo> folders) {
		this.folders = folders;
	}

	public void addFolder(FolderVo folder) {
		if (folders == null) {
			folders = new ArrayList<>();
		}
		folders.add(folder);
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 *
	 * @return 사진 경로. OS 기준 경로임.
	 */
	public File getFullPath() {
		File path = new File(BokslPhotoConstant.Photo.BASE_DIR, this.directory);
		return new File(path, this.name);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}