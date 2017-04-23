package com.setvect.bokslphoto.service;

import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.vo.PhotoVo;

@Service
public class PhotoService {
	@Autowired
	private PhotoRepository photoRepository;

	/**
	 * 이미지 탐색
	 */
	public void retrievalPhoto() {
		ApplicationUtil.listFiles(BokslPhotoConstant.Photo.BASE_DIR).filter(p -> {
			String name = p.getFileName().toString();
			String ext = FilenameUtils.getExtension(name);
			return BokslPhotoConstant.Photo.ALLOW.contains(ext);
		}).forEach(p -> {
			PhotoVo photo = new PhotoVo();
			String photoId = ApplicationUtil.getMd5(p.toFile());
			photo.setPhotoId(photoId);
			photo.setPath(p.toString());
			photo.setRegData(new Date());
			photoRepository.save(photo);
		});
	}
}
