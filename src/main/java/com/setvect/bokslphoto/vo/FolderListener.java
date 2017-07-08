package com.setvect.bokslphoto.vo;

import java.util.HashSet;

import javax.persistence.PreRemove;

/**
 * 폴더 CRUD 이벤트 리슨어
 */
public class FolderListener {
	/**
	 * 폴더에 소속된 관련 이미지 제거
	 *
	 * @param folderForDelete
	 *            삭제 될 폴더
	 */
	@PreRemove
	private void beforeAnyOperation(final FolderVo folderForDelete) {
		folderForDelete.getPhotos().forEach(p -> {
			p.setFolders(new HashSet<>());
		});
	}
}
