package com.setvect.bokslphoto.vo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 사진 분류 폴더
 */
@Entity
@Table(name = "TBBB_FOLDER")
public class FolderVo {
	@Id
	@Column(name = "FOLDER_SEQ", nullable = false)
	@GenericGenerator(name = "hibernate-increment", strategy = "increment")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "hibernate-increment")
	private int folderSeq;

	@Column(name = "PARENT_ID", nullable = false)
	private int parentId;

	@Column(name = "NAME", nullable = false, length = 50)
	private String name;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "folders")
	private List<PhotoVo> photos;

	public int getFolderSeq() {
		return folderSeq;
	}

	public void setFolderSeq(int folderSeq) {
		this.folderSeq = folderSeq;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PhotoVo> getPhotos() {
		return photos;
	}

	public void setPhotos(List<PhotoVo> photos) {
		this.photos = photos;
	}

	public void addPhoto(PhotoVo photo) {
		if (photos == null) {
			photos = new ArrayList<>();
		}
		photos.add(photo);
	}

	public int getPhotoCount() {
		if (getPhotos() == null) {
			return 0;
		}
		return photos.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + folderSeq;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FolderVo other = (FolderVo) obj;
		if (folderSeq != other.folderSeq)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FolderVo [folderSeq=" + folderSeq + ", parentId=" + parentId + ", name=" + name + "]";
	}

}
