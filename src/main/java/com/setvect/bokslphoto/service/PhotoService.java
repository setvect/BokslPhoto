package com.setvect.bokslphoto.service;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import com.setvect.bokslphoto.util.DateRange;
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
	 * 이미지 탐색 후 저장
	 */
	public void retrievalPhotoAndSave() {
		List<File> imageFiles = findImageFiles();

		imageFiles.stream().peek(action -> {
			logger.info(action.toString());
		}).forEach(p -> {
			savePhoto(p);
		});
	}

	/**
	 * 날짜별 이미지 건수
	 *
	 * @param groupType
	 *            그룹핑 유형
	 * @return
	 */
	public Map<DateRange, Integer> groupByDate(DateGroup groupType) {
		List<ImmutablePair<Date, Integer>> countByDate = photoRepository.getGroupShotDate();
		Map<DateRange, Integer> result = new TreeMap<>();
		for (ImmutablePair<Date, Integer> pair : countByDate) {
			Date date = pair.getKey();
			DateRange range = makeDateRange(date, groupType);
			Integer c = result.get(range);
			if (c == null) {
				c = 0;
			}
			c += pair.getValue();
			result.put(range, c);
		}

		// countByDate.stream().map(mapper)

		return result;
	}

	/**
	 * 날짜 그룹핑 영역의 시작과 종료 범위를 구함
	 *
	 * @param base
	 *            기준 날짜
	 * @param groupType
	 * @return
	 */
	private DateRange makeDateRange(Date base, DateGroup groupType) {
		if (base == null) {
			return new DateRange(new Date(0), new Date(0));
		}

		Calendar s = Calendar.getInstance();
		s.setTime(base);
		s.set(Calendar.HOUR, 0);
		s.set(Calendar.MINUTE, 0);
		s.set(Calendar.SECOND, 0);
		s.set(Calendar.MILLISECOND, 0);

		if (groupType == DateGroup.MONTH) {
			s.set(Calendar.DATE, 1);
		} else if (groupType == DateGroup.YEAR) {
			s.set(Calendar.DATE, 1);
			s.set(Calendar.MONTH, 0);
		}

		Calendar e = (Calendar) s.clone();
		if (groupType == DateGroup.DATE) {
			e.add(Calendar.DATE, 1);
		} else if (groupType == DateGroup.MONTH) {
			e.add(Calendar.MONTH, 1);
		} else if (groupType == DateGroup.YEAR) {
			e.add(Calendar.YEAR, 1);
		}
		e.add(Calendar.SECOND, -1);

		DateRange range = new DateRange(s.getTime(), e.getTime());
		return range;
	}

	/**
	 * 서로 다른 경로에 저장된 파일 내용이 2건 이상 같은 경우를 찾음
	 *
	 * @return Key: 파일에 대한 MD5 값,
	 */
	public Map<String, List<File>> findDuplicate() {
		List<File> imageFiles = findImageFiles();
		// 전 파일을 스캔하여 Key에 해당하는 파일 넣기
		Map<String, List<File>> keyAndFiles = imageFiles.stream()
				.collect(Collectors.groupingBy(ApplicationUtil::getMd5));

		// 하나의 key(MD5)에 두개 이상의 파일이 있는 경우 찾기
		Map<String, List<File>> result = keyAndFiles.entrySet().stream().filter(entry -> entry.getValue().size() > 1)
				.collect(Collectors.toMap(entry -> entry.getKey(), p -> p.getValue()));

		return result;
	}

	/**
	 * 서로다른 경로에 중복된 파일을 삭제함.<br>
	 * DB에 저장된 파일만 남기고 나머지는 삭제함
	 */
	public List<File> deleteDuplicate() {
		Map<String, List<File>> duplicateFile = findDuplicate();
		List<File> deleteFile = new ArrayList<>();

		duplicateFile.entrySet().stream().forEach(photoEntry -> {
			String key = photoEntry.getKey();
			PhotoVo photoDbSave = photoRepository.findOne(key);
			if (photoDbSave == null) {
				logger.info("Not Saved to DB. Key: {}", key);
				return;
			}

			photoEntry.getValue().stream().filter(photoFile -> !photoFile.equals(photoDbSave.getFullPath()))
					.forEach(photoFile -> {
						deleteFile.add(photoFile);
						photoFile.delete();
						logger.info("Delete Photo File: {}", photoFile);
					});
		});

		return deleteFile;
	}

	/**
	 * 이미지 파일을 찾음.
	 *
	 * @return
	 */
	private List<File> findImageFiles() {
		List<File> path = ApplicationUtil.listFiles(BokslPhotoConstant.Photo.BASE_DIR).filter(p -> {
			String name = p.getName();
			String ext = FilenameUtils.getExtension(name).toLowerCase();
			return BokslPhotoConstant.Photo.ALLOW.contains(ext);
		}).collect(toList());
		return path;
	}

	/**
	 * 사진 파일 저장
	 *
	 * @param imageFile
	 */
	public void savePhoto(File imageFile) {
		savePhoto(imageFile, true);
	}

	/**
	 * 사진 파일 저장
	 *
	 * @param imageFile
	 * @param overwrite
	 *            true 동일한 md5를 같는 파일이 있으면 현재 업로드 파일로 기준으로 교체. 기존 파일은 지우지 않음<br>
	 *            false 동일한 md5를 같는 파일이 있으면 현재 업로드 취소. 업로드 파일은 삭제함.
	 */
	public boolean savePhoto(File imageFile, boolean overwrite) {
		File baseFile = BokslPhotoConstant.Photo.BASE_DIR;

		File dirFile = imageFile.getParentFile();
		String dir = ApplicationUtil.getRelativePath(baseFile, dirFile);
		dir = "/" + dir;

		PhotoVo photo = new PhotoVo();
		Date shotDate = getShotDate(imageFile);
		String photoId = ApplicationUtil.getMd5(imageFile);
		if (!overwrite) {
			PhotoVo before = photoRepository.findOne(photoId);
			if (before != null) {
				logger.info("Already have the same file.({})", before.getFullPath().getAbsolutePath());
				imageFile.delete();
				return false;
			}
		}

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
		return true;
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