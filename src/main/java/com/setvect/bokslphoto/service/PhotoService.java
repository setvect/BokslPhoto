package com.setvect.bokslphoto.service;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private Logger logger = LoggerFactory.getLogger(PhotoService.class);

	/**
	 * 이미지 탐색
	 */
	public void retrievalPhoto() {
		List<Path> path = ApplicationUtil.listFiles(BokslPhotoConstant.Photo.BASE_DIR).filter(p -> {
			String name = p.getFileName().toString();
			String ext = FilenameUtils.getExtension(name);
			return BokslPhotoConstant.Photo.ALLOW.contains(ext);
		}).collect(toList());

		// path.stream().parallel().forEach(p -> {
		path.stream().peek(action -> {
			logger.info(action.toString());
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
