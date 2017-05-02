package com.setvect.bokslphoto.service;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.PhotoVo.ShotDateType;

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

			Date shotDate = null;
			File imageFile = p.toFile();

			shotDate = getShotDate(imageFile);

			String photoId = ApplicationUtil.getMd5(imageFile);
			photo.setPhotoId(photoId);
			photo.setPath(p.toString());
			photo.setShotDate(shotDate);
			photo.setShotDataType(ShotDateType.MANUAL);
			photo.setRegData(new Date());
			photoRepository.save(photo);
		});
	}

	/**
	 * 사진 촬영일
	 *
	 * @param imageFile
	 * @return
	 */
	private Date getShotDate(File imageFile) {
		Date date = null;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
			ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if (exifSubIFDDirectory == null) {
				return null;
			}

			Optional<LocalDateTime> shotDate = exifSubIFDDirectory.getTags().stream()
					.filter(tag -> tag != null
							&& tag.getTagName().equals(BokslPhotoConstant.ImageMeta.DATE_TIME_ORIGINAL))
					.map(tag -> tag.getDescription()).findAny()
					.map(dateStr -> LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));

			if (shotDate.isPresent()) {
				LocalDateTime value = shotDate.get();
				date = ApplicationUtil.getDate(value);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return date;
	}
}