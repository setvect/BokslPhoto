package com.setvect.bokslphoto.vo;

import java.util.HashSet;

import javax.persistence.EntityManager;
import javax.persistence.PreRemove;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 폴더 CRUD 이벤트 리슨어
 */
@Component
public class FolderListener {

	/** 엔티티의 refrash, merge 등을 관리하기 위해 */
	@Autowired
	private static EntityManager entityManager;

	/**
	 * @param entityManager
	 *            엔티티의 refrash, merge 등을 관리하기 위해
	 */
	@Autowired
	public void init(final EntityManager entityManager) {
		FolderListener.entityManager = entityManager;
	}

	/**
	 * 폴더에 소속된 관련 이미지 제거
	 *
	 * @param folderForDelete
	 *            삭제 될 폴더
	 */
	@PreRemove
	private void beforeAnyOperation(final FolderVo folderForDelete) {
		entityManager.refresh(folderForDelete);
		folderForDelete.getPhotos().forEach(p -> {
			p.setFolders(new HashSet<>());
		});
	}
}
