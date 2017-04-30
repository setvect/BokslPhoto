package com.setvect.bokslphoto.vo;

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

	@Column(name = "PATH", nullable = false, length = 500)
	private String path;

	@Column(name = "SHOT_DATE", nullable = true)
	private Date shotDate;

	@Column(name = "SHOT_DATE_TYPE", nullable = true)
	@Enumerated(EnumType.STRING)
	private ShotDateType shotDataType;

	@Column(name = "MEMO", nullable = true, length = 500)
	private String memo;

	@Column(name = "PROTECT_F", nullable = false, length = 1)
	@Type(type = "yes_no")
	private boolean protectF;

	@Column(name = "REG_DATE", nullable = false)
	private Date regData;

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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}