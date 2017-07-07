package com.setvect.bokslphoto.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 사진 분류 폴더
 */
@Entity
@Table(name = "TBBB_FOLDER")
public class FolderVo implements Serializable {
	/** */
	private static final long serialVersionUID = -2233365039548913827L;

	/** 일련번호 */
	@Id
	@Column(name = "FOLDER_SEQ", nullable = false)
	@GenericGenerator(name = "hibernate-increment", strategy = "increment")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "hibernate-increment")
	private int folderSeq;

	/** 부모 일련번호 */
	@Column(name = "PARENT_ID", nullable = false)
	private int parentId;

	/**
	 * 부모 <br>
	 */
	@ManyToOne
	@JoinColumn(name = "PARENT_ID", insertable = false, updatable = false)
	@JsonIgnore
	private FolderVo parent;

	/**
	 * 자식<br>
	 */
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<FolderVo> children;

	/** 폴더 이름 */
	@Column(name = "NAME", nullable = false, length = 50)
	private String name;

	/** 현재 분류에 소속된 사진 */
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "folders", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.DETACH })
	@JsonIgnore
	private List<PhotoVo> photos;

	/**
	 * @return 일련번호
	 */
	public int getFolderSeq() {
		return folderSeq;
	}

	/**
	 * @param folderSeq
	 *            일련번호
	 */
	public void setFolderSeq(final int folderSeq) {
		this.folderSeq = folderSeq;
	}

	/**
	 * @return 부모 일련번호
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            부모 일련번호
	 */
	public void setParentId(final int parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return 폴더 이름
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            폴더 이름
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return 현재 분류에 소속된 사진
	 */
	public List<PhotoVo> getPhotos() {
		return photos;
	}

	/**
	 * @param photos
	 *            현재 분류에 소속된 사진
	 */
	public void setPhotos(final List<PhotoVo> photos) {
		this.photos = photos;
	}

	/**
	 * @param photo
	 *            현재 분류에 소속된 사진
	 */
	public void addPhoto(final PhotoVo photo) {
		if (photos == null) {
			photos = new ArrayList<>();
		}
		photos.add(photo);
	}

	/**
	 * @return 현재 폴더에 속한 사진 갯수
	 */
	public int getPhotoCount() {
		if (getPhotos() == null) {
			return 0;
		}
		return photos.size();
	}

	/**
	 * @return
	 */
	public FolderVo getParent() {
		return parent;
	}

	/**
	 * @param parent
	 */
	public void setParent(FolderVo parent) {
		this.parent = parent;
	}

	/**
	 * @return
	 */
	public List<FolderVo> getChildren() {
		return children;
	}

	/**
	 * @param children
	 */
	public void setChildren(List<FolderVo> children) {
		this.children = children;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + folderSeq;
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
		FolderVo other = (FolderVo) obj;
		if (folderSeq != other.folderSeq) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "FolderVo [folderSeq=" + folderSeq + ", parentId=" + parentId + ", name=" + name + "]";
	}

}
