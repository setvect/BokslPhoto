package com.setvect.bokslphoto.vo;

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
	private List<PhotoVo> products;

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

	@Override
	public String toString() {
		return "FolderVo [folderSeq=" + folderSeq + ", parentId=" + parentId + ", name=" + name + "]";
	}

}
