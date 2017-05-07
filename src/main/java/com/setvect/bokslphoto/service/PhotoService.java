package com.setvect.bokslphoto.service;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.BokslPhotoConstant.ImageMeta;
import com.setvect.bokslphoto.BokslPhotoConstant.RegexPattern;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.util.TreeNode;
import com.setvect.bokslphoto.vo.PhotoDirectory;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.PhotoVo.ShotDateType;

@Service
public class PhotoService {
	@Autowired
	private PhotoRepository photoRepository;
	private static Logger logger = LoggerFactory.getLogger(PhotoService.class);

	/**
	 * 이미지 탐색
	 */
	public void retrievalPhoto() {
		List<Path> path = ApplicationUtil.listFiles(BokslPhotoConstant.Photo.BASE_DIR).filter(p -> {
			String name = p.getFileName().toString();
			String ext = FilenameUtils.getExtension(name);
			return BokslPhotoConstant.Photo.ALLOW.contains(ext);
		}).collect(toList());

		path.stream().peek(action -> {
			logger.info(action.toString());
		}).forEach(p -> {
			File imageFile = p.toFile();
			savePhoto(imageFile);
		});
	}

	/**
	 * 파일 저장
	 *
	 * @param imageFile
	 */
	public void savePhoto(File imageFile) {
		File baseFile = BokslPhotoConstant.Photo.BASE_DIR.toFile();

		File dirFile = imageFile.getParentFile();
		String dir = baseFile.toURI().relativize(dirFile.toURI()).getPath();
		dir = "/" + dir;

		PhotoVo photo = new PhotoVo();
		Date shotDate = getShotDate(imageFile);
		String photoId = ApplicationUtil.getMd5(imageFile);
		photo.setPhotoId(photoId);
		photo.setDirectory(dir);
		photo.setName(imageFile.getName());
		photo.setShotDate(shotDate);
		photo.setShotDataType(ShotDateType.MANUAL);
		photo.setRegData(new Date());
		GeoCoordinates geo = getGeo(imageFile);
		if (geo != null) {
			photo.setLatitude(geo.getLatitude());
			photo.setLongitude(geo.getLongitude());
		}

		photoRepository.save(photo);
	}

	/**
	 * 이미지가 저장된 물리적인 경로를 폴더 구조로 반환. <br>
	 * 물리적인 경로는 com.setvect.photo.base를 기준으로 시작함
	 *
	 * @see BokslPhotoConstant.Photo#BASE_DIR
	 * @return
	 */
	public TreeNode<PhotoDirectory> getDirectoryTree() {
		Map<String, Integer> photoPathAndCount = photoRepository.getPhotoDirectoryList();
		Set<String> dirs = photoPathAndCount.keySet();

		Integer photoCount = getPhotoCount(photoPathAndCount, "/");
		TreeNode<PhotoDirectory> rootNode = new TreeNode<PhotoDirectory>(new PhotoDirectory("/", photoCount));

		for (String dir : dirs) {
			List<String> pathAppend = new ArrayList<>();
			File path = new File(dir);
			while (true) {
				String pathString = path.getPath().replace("\\", "/") + "/";
				path = path.getParentFile();
				if (path == null) {
					break;
				}
				pathAppend.add(pathString);
			}

			Collections.reverse(pathAppend);

			TreeNode<PhotoDirectory> currentNode = rootNode;

			for (String pathString : pathAppend) {
				photoCount = getPhotoCount(photoPathAndCount, pathString);
				PhotoDirectory photoDir = new PhotoDirectory(pathString, photoCount);
				TreeNode<PhotoDirectory> node = rootNode.getTreeNode(photoDir);

				if (node == null) {
					currentNode = currentNode.addChild(photoDir);
				} else {
					currentNode = node;
				}
			}
		}

		return rootNode;
	}

	private int getPhotoCount(Map<String, Integer> photoPathAndCount, String path) {
		Integer photoCount = photoPathAndCount.get(path);
		if (photoCount == null) {
			photoCount = 0;
		}
		return photoCount;
	}

	/**
	 * 사진 촬영일
	 *
	 * @param imageFile
	 * @return
	 */
	private static Date getShotDate(File imageFile) {
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

	/**
	 * GEO 좌표
	 *
	 * @param file
	 * @return
	 */
	public static GeoCoordinates getGeo(File file) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			GpsDirectory meta = metadata.getFirstDirectoryOfType(GpsDirectory.class);

			if (meta == null) {
				return null;
			}
			Pattern regex = Pattern.compile(RegexPattern.GPS);

			// 37° 28' 46.12"
			Map<String, Double> geo = meta.getTags().stream()
					.filter(tag -> tag.getTagName().equals(ImageMeta.GPS_LATITUDE)
							|| tag.getTagName().equals(ImageMeta.GPS_LONGITUDE))
					.filter(tag -> tag.getDescription() != null).collect(Collectors.toMap(p -> p.getTagName(), p -> {
						String coordinates = p.getDescription();
						Matcher matcher = regex.matcher(coordinates);
						if (!matcher.find()) {
							return null;
						}

						double degree = Double.parseDouble(matcher.group(1));
						double minutes = Double.parseDouble(matcher.group(2));
						double seconds = Double.parseDouble(matcher.group(3));
						double value = degree + minutes / 60 + seconds / 3600;
						return value;
					}));
			if (geo.size() == 2) {
				Double latitude = geo.get(ImageMeta.GPS_LATITUDE);
				Double longitude = geo.get(ImageMeta.GPS_LONGITUDE);

				if (latitude == null || longitude == null) {
					return null;
				}
				return new GeoCoordinates(latitude, longitude);
			}
		} catch (Exception e) {
			logger.error(e.getMessage() + ": " + file.getAbsolutePath(), e);
			return null;
		}
		return null;
	}

}