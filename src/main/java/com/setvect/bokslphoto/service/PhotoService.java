package com.setvect.bokslphoto.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.setvect.bokslphoto.ApplicationRuntimeException;
import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.BokslPhotoConstant.ImageMeta;
import com.setvect.bokslphoto.BokslPhotoConstant.RegexPattern;
import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.util.DateRange;
import com.setvect.bokslphoto.util.TreeNode;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoDirectory;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.PhotoVo.ShotDateType;

import net.coobird.thumbnailator.Thumbnails;

/**
 * 포토 로직
 */
@Service
public class PhotoService {
	/** 포토 관련 DB */
	@Autowired
	private PhotoRepository photoRepository;

	/** 분류 폴더 */
	@Autowired
	private FolderRepository folderRepository;

	/** 개체 refresh 하기위해 */
	@Autowired
	private EntityManager entityManager;

	/** 로깅 */
	private static Logger logger = LoggerFactory.getLogger(PhotoService.class);

	/**
	 * 이미지 업로드 유형
	 */
	public enum StoreType {
		/** 같이 이미지가 이미 있으면 기존거 지우고 새롭게 등록. */
		OVERWRITE,
		/**
		 * 같은 이미지가 이미 있으면 이전 이미지 파일 삭제하고 업데이트. 즉 이전 데이터(메모, 맵핑 폴더)를 그대로 유지하고 이미지 경로만 새롭게
		 * 업데이트 함.
		 */
		UPDATE,
		/** 같은 이미지가 이미 있으면 업로드 취소. 현재 등록된 이미지 파일 삭제 */
		NO_OVERWRITE;
	}

	// ============== 조회 관련 ==============

	/**
	 * 포토 기본 경로에서 사진 파일을 찾음.
	 *
	 * @return 이미지 파일
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
	 * 날짜별 이미지 건수
	 *
	 * @param searchParam
	 *            그룹핑 유형
	 * @return key: 날짜 범위, value: 건수
	 */
	public Map<DateRange, Integer> groupByDate(final PhotoSearchParam searchParam) {
		List<ImmutablePair<Date, Integer>> countByDate = photoRepository.getGroupShotDate(searchParam);

		Map<DateRange, Integer> result = countByDate.stream()
				.collect(Collectors.groupingBy((ImmutablePair<Date, Integer> pair) -> {
					Date date = pair.getKey();
					DateRange range = makeDateRange(date, searchParam.getSearchDateGroup());
					return range;
				}, () -> new LinkedHashMap<>(), Collectors.summingInt((ImmutablePair<Date, Integer> pair) -> {
					return pair.getValue();
				})));

		return result;
	}

	/**
	 * 날짜 그룹핑 영역의 시작과 종료 범위를 구함
	 *
	 * @param base
	 *            기준 날짜
	 * @param groupType
	 *            그루핑 유형
	 * @return 날짜 그룹핑 영역의 시작과 종료 범위
	 */
	private DateRange makeDateRange(final Date base, final DateGroup groupType) {
		// 날짜가 없는 경우 timestamp 0으로 함.
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
	 * 이미지가 저장된 물리적인 경로를 폴더 구조로 반환. <br>
	 * 물리적인 경로는 com.setvect.photo.base를 기준으로 시작함
	 *
	 * @see BokslPhotoConstant.Photo#BASE_DIR
	 * @return 이미지 경로를 재귀적(부모/자식)으로 저장
	 */
	public TreeNode<PhotoDirectory> getDirectoryTree() {
		Map<String, Integer> photoPathAndCount = photoRepository.getPhotoDirectoryList();
		Set<String> dirs = photoPathAndCount.keySet();

		Integer photoCount = getPhotoCount(photoPathAndCount, "/");
		TreeNode<PhotoDirectory> rootNode = new TreeNode<>(new PhotoDirectory("/", photoCount));

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

	/**
	 * @param photoPathAndCount
	 *            key: 경로, Value: 건수
	 * @param path
	 *            이미지 경로
	 * @return 해당 폴더내 이미지 건수
	 */
	private int getPhotoCount(final Map<String, Integer> photoPathAndCount, final String path) {
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
	 *            이미지 파일
	 * @return 촬영일
	 */
	private static Date getShotDate(final File imageFile) {
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
			throw new RuntimeException(e);
		}
		return date;
	}

	/**
	 * 메타 정보
	 *
	 * @param imageFile
	 *            이미지 파일
	 * @return Key: 메타 이름, Value: 값
	 */
	public static Map<String, String> getImageMeta(final File imageFile) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

			Map<String, String> result = StreamSupport.stream(metadata.getDirectories().spliterator(), false)
					.flatMap(p -> p.getTags().stream()).filter(Objects::nonNull).collect(Collectors.toMap(p -> {
						return "[" + p.getDirectoryName() + "]" + p.getTagName();
					}, p -> {
						return p.getDescription() == null ? "" : p.getDescription();
					}, (v1, v2) -> v1, TreeMap::new));

			return result;
		} catch (Exception e) {
			logger.error(e.getMessage() + ": " + imageFile.getAbsolutePath(), e);
			return null;
		}
	}

	/**
	 * GEO 좌표
	 *
	 * @param metadata
	 *            이미지 파일
	 * @return GEO 좌표
	 */
	public static GeoCoordinates getGeo(final Metadata metadata) {
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
		return null;
	}

	/**
	 * @return 분류 폴더 구조
	 */
	public TreeNode<FolderVo> getFolderTree() {
		List<FolderVo> folderAll = folderRepository.findAll();

		// 일련번호와 부모 아이디가 같은 경우는 root 폴더.
		Optional<FolderVo> data = folderAll.stream().filter(f -> isRootFolder(f)).findAny();
		FolderVo rootData = data.get();

		Map<Integer, List<FolderVo>> folderListByParentId = folderAll.stream()
				.filter(f -> f.getFolderSeq() != rootData.getFolderSeq())
				.collect(Collectors.groupingBy(FolderVo::getParentId, Collectors.toList()));

		TreeNode<FolderVo> rootNode = new TreeNode<>(rootData);

		findSubFolder(rootNode, folderListByParentId);
		return rootNode;
	}

	/**
	 * 전체 분류 폴더와, 해당 이미지가 포함된 폴더 정보를 반환
	 *
	 * @param photoId
	 *            사진 아이디
	 * @return 부가정보가 포함된 모든 분류 폴더. 최상위 루트 폴더 제외
	 */
	public List<FolderAddtion> getFolderAddtionList(final String photoId) {
		TreeNode<FolderVo> folder = getFolderTree();

		// 해당 사진에 포함될 폴더 아이디 확보
		PhotoVo photo = photoRepository.findOne(photoId);
		// 폴더(릴레이션) 정보를 가져오기 위함
		entityManager.refresh(photo);
		Set<FolderVo> selectFolder = photo.getFolders();
		Set<Integer> selectFolderSeq = selectFolder.stream().map(f -> f.getFolderSeq()).collect(toSet());

		List<TreeNode<FolderVo>> folderList = folder.exploreTree();
		List<FolderAddtion> result = folderList.stream().filter(f -> !f.getData().isRoot()).map(f -> {
			boolean select = selectFolderSeq.contains(f.getData().getFolderSeq());
			FolderAddtion folderInfo = new FolderAddtion(f.getData(), f.getLevel(), select);
			return folderInfo;
		}).collect(toList());
		return result;
	}

	/**
	 * 원본이미지 정보. 디카 이미지 메타 정보를 파악해 회전 정보를 보정해서 반환
	 *
	 * @param photo
	 *            포토
	 * @return 이미지 바이너리 정보
	 * @throws IOException
	 *             예외
	 */
	public byte[] getImageOrg(final PhotoVo photo) throws IOException {
		File photoFile;

		if (photo.isRotate()) {
			photoFile = rotateCorrection(photo);
		} else {
			photoFile = photo.getFullPath();
		}

		try (InputStream in = new FileInputStream(photoFile);) {
			return IOUtils.toByteArray(in);
		}
	}

	/**
	 * 섬네일 이미지 만들기
	 *
	 * @param photo
	 *            이미지
	 * @param width
	 *            최대 넓이
	 * @param height
	 *            최대 높이
	 * @return 섬네일 이미지 byte
	 * @throws IOException
	 *             예외
	 */
	public byte[] makeThumbimage(final PhotoVo photo, final int width, final int height) throws IOException {
		// 입력값이 재대로 입력되지 않으면 그냥 리턴
		if (photo == null || width == 0 || height == 0) {
			logger.warn("thumbnail error.");
			return null;
		}

		File photoFile = photo.getFullPath();

		// 파일이 존재 하지 않으면 그냥 종료
		if (!photoFile.exists()) {
			logger.warn("{} not exist.", photoFile);
			return null;
		}

		// 섬네일 이미지 파일이름 만들기
		// e.g) imagename_w33_h44.jpg
		String name = photoFile.getName();
		String tempImg = photo.getPhotoId() + "_w" + width + "_h" + height + "." + FilenameUtils.getExtension(name);

		if (!BokslPhotoConstant.Photo.THUMBNAIL_DIR.exists()) {
			BokslPhotoConstant.Photo.THUMBNAIL_DIR.mkdirs();
			logger.info("make thumbnail directory: ", BokslPhotoConstant.Photo.THUMBNAIL_DIR.getAbsolutePath());
		}

		// 섬네일 버전된 경로
		File toThumbnailFile = new File(BokslPhotoConstant.Photo.THUMBNAIL_DIR, tempImg);
		boolean thumbnailExist = toThumbnailFile.exists();
		boolean oldThumbnail = toThumbnailFile.lastModified() < photoFile.lastModified();

		// 기존에 섬네일로 변환된 파일이 있는냐?
		// 섬네일로 변환된 파일이 없거나, 파일이 수정되었을 경우 섬네일 다시 만들기
		if (!thumbnailExist || oldThumbnail) {
			ThumbnailImageConvert.makeThumbnail(photoFile, toThumbnailFile, width, height);
		}

		try (InputStream in = new FileInputStream(toThumbnailFile);) {
			return IOUtils.toByteArray(in);
		}
	}

	/**
	 * 이미지 회전 보정
	 *
	 * @param photo
	 *            포토
	 * @return 회전 된 파일 이름
	 * @throws IOException
	 *             예외
	 */
	private File rotateCorrection(final PhotoVo photo) throws IOException {
		if (!BokslPhotoConstant.Photo.ROTATE_DIR.exists()) {
			BokslPhotoConstant.Photo.ROTATE_DIR.mkdirs();
			logger.info("make rotate directory: ", BokslPhotoConstant.Photo.ROTATE_DIR.getAbsolutePath());
		}

		File photoFile = photo.getFullPath();
		String ext = FilenameUtils.getExtension(photoFile.getName());
		File toRotate = new File(BokslPhotoConstant.Photo.ROTATE_DIR, photo.getPhotoId() + "." + ext);

		boolean rotateExist = toRotate.exists();
		boolean oldImage = toRotate.lastModified() < photoFile.lastModified();

		// 기존에 회전 보정 변환된 파일이 있는냐?
		// 회전 보정 변환된 파일이 없거나, 파일이 수정되었을 경우 회전 보정 다시 만들기
		if (!rotateExist || oldImage) {
			Thumbnails.of(photoFile).scale(1).toFile(toRotate);
			logger.info(String.format("rotate corrention: %s -> %s", photoFile.getName(), toRotate.getName()));
			return toRotate;
		}
		return toRotate;
	}

	/**
	 * 전체 폴더에서 rootNode의 자식을 찾음
	 *
	 * @param rootNode
	 *            트리구조 노드
	 * @param folderListByParentId
	 *            전체 폴더. Key: 부모 아이디, Value: 가르키는 부모 아이디가 같은 폴더
	 */
	private void findSubFolder(final TreeNode<FolderVo> rootNode,
			final Map<Integer, List<FolderVo>> folderListByParentId) {
		int id = rootNode.getData().getFolderSeq();
		List<FolderVo> children = folderListByParentId.get(id);
		if (children == null) {
			return;
		}
		children.stream().forEach(c -> {
			TreeNode<FolderVo> currentNode = rootNode.addChild(c);
			findSubFolder(currentNode, folderListByParentId);
		});
	}

	// ============== 데이터 저장 관련 ==============

	/**
	 * DB와 물리적인 이미지 저장값과 동기화.<br>
	 * 이미지가 있고 DB에 없는 경우 -> 등록<br>
	 * 이미지가 있고 DB도 있는 경우 -> 저장 경로 업데이트<br>
	 * 이미지 없고 DB가 있는 경우 -> DB에 있는 내용 삭제
	 *
	 * @param storeType
	 *            저장 형식
	 */
	public void syncPhotoAndSave(final StoreType storeType) {
		List<File> imageFiles = findImageFiles();

		imageFiles.stream().peek(action -> {
			logger.info(action.toString());
		}).forEach(p -> {
			savePhoto(p, storeType);
		});
	}

	/**
	 * 사진 파일 저장
	 *
	 * @param imageFile
	 *            이미지 파일
	 */
	public void savePhoto(final File imageFile) {
		savePhoto(imageFile, StoreType.UPDATE);
	}

	/**
	 * 사진 파일 저장
	 *
	 * @param imageFile
	 *            이미지 파일
	 * @param storeType
	 *            이미지 저장 유형
	 * @return 저장 성공여부
	 */
	public boolean savePhoto(final File imageFile, final StoreType storeType) {
		File baseFile = BokslPhotoConstant.Photo.BASE_DIR;

		File dirFile = imageFile.getParentFile();
		String dir = ApplicationUtil.getRelativePath(baseFile, dirFile);
		dir = "/" + dir;

		if (!imageFile.exists()) {
			logger.warn("Skip. Image not exist.({})", imageFile.getAbsolutePath());
			return false;
		}

		PhotoVo photo = new PhotoVo();
		Date shotDate = getShotDate(imageFile);
		String photoId = ApplicationUtil.getMd5(imageFile);
		PhotoVo before = photoRepository.findOne(photoId);

		File beforeFile = null;
		if (before != null) {
			beforeFile = before.getFullPath();
		}

		// 기존에 이미지가 있으면 새로운 이미지 제 업로드 하지 않음.
		if (storeType == StoreType.NO_OVERWRITE && before != null) {
			logger.info("Skip. Already have the same image.({})", before.getPhotoId());

			// 업로드된 이미지와 기존 저장된 이미지가 다르면 업로드된 이미지 삭제
			if (!imageFile.equals(beforeFile)) {
				deleteImageFile(imageFile);
			}
			return false;
		}

		if (storeType == StoreType.OVERWRITE && before != null) {
			// 기존 이미지 삭제
			logger.info("Already have the same file. delete. ({})", beforeFile.getAbsolutePath());

			// 업로드된 이미지와 기존 저장된 이미지가 다르면 기존 이미지 삭제
			if (!imageFile.equals(beforeFile)) {
				deleteImageFile(beforeFile);
			}
		} else if (storeType == StoreType.UPDATE && before != null) {
			logger.info("Already have the same file. ({})", beforeFile.getAbsolutePath());
			// 업로드된 이미지와 기존 저장된 이미지가 다르면 기존 이미지 삭제
			if (!imageFile.equals(beforeFile)) {
				deleteImageFile(beforeFile);
			}
			// 디렉토리 경로만 변경
			before.setDirectory(dir);
			before.setName(imageFile.getName());
			entityManager.merge(before);
			return true;
		}

		photo.setPhotoId(photoId);
		photo.setDirectory(dir);
		photo.setName(imageFile.getName());
		photo.setShotDate(shotDate);
		photo.setShotDateType(shotDate != null ? ShotDateType.META : ShotDateType.MANUAL);
		photo.setRegData(new Date());

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
			GeoCoordinates geo = getGeo(metadata);
			if (geo != null) {
				photo.setLatitude(geo.getLatitude());
				photo.setLongitude(geo.getLongitude());
			}
			int orientationValue = getOrientation(metadata);
			photo.setOrientation(orientationValue);
		} catch (ImageProcessingException | IOException e) {
			logger.error(e.getMessage() + ": " + imageFile.getAbsolutePath(), e);
		}
		photoRepository.save(photo);
		return true;
	}

	/**
	 * @param metadata
	 *            이미지 메타 테그
	 * @return 회원 정보. 추출하지 못하면 0을 반환
	 */
	private int getOrientation(final Metadata metadata) {
		Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (directory != null) {
			try {
				int result = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
				return result;
			} catch (MetadataException e) {
				logger.warn("fail extract TAG_ORIENTATION.");
				return 0;
			}
		}
		return 0;
	}

	/**
	 * 이미지 파일 삭제
	 *
	 * @param imageFile
	 *            이미지 파일
	 */
	private void deleteImageFile(final File imageFile) {
		boolean delete = imageFile.delete();
		logger.info("file delete. ({}) ({})", delete, imageFile.getAbsolutePath());
	}

	// ============== 데이터 수정 관련 ==============

	// ============== 데이터 삭제 관련 ==============

	/**
	 * 서로다른 경로에 중복된 파일을 삭제함.<br>
	 * DB에 저장된 파일만 남기고 나머지는 삭제함
	 *
	 * @return 삭제한 파일
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
	 * 분류 폴더 삭제<br>
	 * 하위 분류까지 삭제 함
	 *
	 * @param folderSeq
	 *            분류 폴더 아이디
	 */
	public void deleteFolder(final int folderSeq) {
		FolderVo folder = folderRepository.findOne(folderSeq);
		if (isRootFolder(folder)) {
			throw new ApplicationRuntimeException("Deleting the root folder is prohibited.");
		}
		// 내부에서 재귀적으로 호출하여 하위 폴더 삭제함.
		folderRepository.delete(folder);
		folderRepository.flush();
	}

	/**
	 * DB에 있는데 물리적인 파일이 없는 경우 DB에 있는 내용 삭제
	 */
	public void deleteNonexistPhoto() {
		List<PhotoVo> allPhoto = photoRepository.findAll();
		List<PhotoVo> noExistPhoto = allPhoto.stream().filter(p -> !p.getFullPath().exists()).collect(toList());
		photoRepository.delete(noExistPhoto);
		logger.info("Delete nonexistent pictures: {}", noExistPhoto.size());
	}

	/**
	 * @param folder
	 *            비교 폴더
	 * @return 루트 폴더면 true, 아니면 false
	 */
	private boolean isRootFolder(final FolderVo folder) {
		return folder.getFolderSeq() == folder.getParentId();
	}
}