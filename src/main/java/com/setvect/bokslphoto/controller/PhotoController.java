package com.setvect.bokslphoto.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.service.PhotoService;
import com.setvect.bokslphoto.service.ThumbnailImageConvert;
import com.setvect.bokslphoto.util.DateRange;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.util.TreeNode;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoDirectory;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.UserVo;

/**
 * 사진 관리 컨트롤. 대부분의 UI 요청 처리.
 */
@Controller
public class PhotoController {
	/** 사용자 DB */
	@Autowired
	private UserRepository userRepository;

	/** 포토 DB */
	@Autowired
	private PhotoRepository photoRepository;

	/** 폴더 DB */
	@Autowired
	private FolderRepository folderRepository;

	/** 포토 서비스 처리. 로직 부분 */
	@Autowired
	private PhotoService photoService;

	/** 로깅 */
	private static Logger logger = LoggerFactory.getLogger(PhotoController.class);

	// ============== 뷰 페이지 오픈 ==============

	/**
	 * @param request
	 *            servlet
	 * @return view 페이지
	 */
	@RequestMapping(value = "/")
	public String index(final HttpServletRequest request) {
		// TODO 강제 로그인. 추후 변경
		constraintLogin(request);

		return "redirect:/photo";
	}

	/**
	 * 로그인 화면. SecurityConfig 설정과 연계
	 */
	@RequestMapping("/login.do")
	public void login() {
	}

	/**
	 * @return 사진관리 메인 view 페이지 오픈
	 */
	@RequestMapping("/photo")
	public String admin() {
		return "photo/photo";
	}

	/**
	 * @param request
	 * @return 사진 목록 보기 페이지
	 */
	@RequestMapping("/photo/list.do")
	public String list() {
		return "photo/photo_list";
	}

	/**
	 * @return 사진 업로드 페이지
	 */
	@RequestMapping("/photo/upload.do")
	public String upload() {
		return "photo/photo_upload";
	}

	/**
	 * 강제 로그인<br>
	 * 개발 과정을 편리하게 하기 위함.
	 *
	 * @param request
	 *            servletRequest
	 */
	private void constraintLogin(final HttpServletRequest request) {
		UserVo userinfo = userRepository.findOne("admin");
		List<GrantedAuthority> roles = ApplicationUtil.buildUserAuthority(userinfo.getUserRole());

		Authentication authentication = new UsernamePasswordAuthenticationToken(userinfo, userinfo.getPassword(),
				roles);
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);

		// 세션에 spring security context 넣음
		HttpSession session = request.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
	}

	// ============== 데이터 조회 ==============

	/**
	 * 날짜별 건수
	 *
	 * @param searchParam
	 *            조회 조건
	 * @return 날짜별 사진 건수
	 */
	@RequestMapping("/photo/groupByDate.json")
	@ResponseBody
	public List<GroupByDate> groupByDate(final PhotoSearchParam searchParam) {
		Map<DateRange, Integer> dateCountMap = photoService.groupByDate(searchParam);

		List<GroupByDate> reseult = dateCountMap.entrySet().stream().map(entry -> {
			GroupByDate g = new GroupByDate();
			g.setFrom(entry.getKey().getStart());
			g.setTo(entry.getKey().getEnd());
			g.setCount(entry.getValue());
			return g;
		}).collect(Collectors.toList());

		return reseult;
	}

	/**
	 * 검색 조건에 맞는 사진 목록을 반환
	 *
	 * @param pageCondition
	 *            검색 조건
	 * @param request
	 *            servletRequest
	 * @return 페이징 정보
	 */
	@RequestMapping("/photo/list.json")
	@ResponseBody
	public GenericPage<PhotoVo> list(final PhotoSearchParam pageCondition, final HttpServletRequest request) {
		GenericPage<PhotoVo> page = photoRepository.getPhotoPagingList(pageCondition);
		String clientIp = request.getRemoteAddr();
		// 허가된 아이피가 아니면 비공개 이미지의 경로 정보를 제거함
		if (!isAllowAccessProtected(clientIp)) {
			page.getList().stream().filter(PhotoVo::isProtectF).forEach(p -> {
				p.setName(null);
				p.setDeny(true);
			});
		}

		return page;
	}

	/**
	 * @return 모든 저장 경로를 폴더 구조로 반환.
	 */
	@RequestMapping("/photo/directory.json")
	@ResponseBody
	public ResponseEntity<TreeNode<PhotoDirectory>> getDiretory() {
		TreeNode<PhotoDirectory> dir = photoService.getDirectoryTree();
		return new ResponseEntity<>(dir, HttpStatus.OK);
	}

	/**
	 * @return 모든 분류 폴더 정보 반환
	 */
	@RequestMapping("/photo/folder.json")
	@ResponseBody
	public ResponseEntity<TreeNode<FolderVo>> getFolder() {
		TreeNode<FolderVo> folder = photoService.getFolderTree();
		return new ResponseEntity<>(folder, HttpStatus.OK);
	}

	/**
	 * @param folderSeq
	 *            폴더 아이디
	 * @param includeRoot
	 *            TODO
	 * @return 현재 분류폴더 까지 경로 반환 테스트 케이스<br>
	 *         ex)홈 > 추억 > 고등학교
	 */
	@RequestMapping("/photo/folderPath.json")
	@ResponseBody
	public ResponseEntity<List<FolderVo>> getFolderPath(@RequestParam("folderSeq") final int folderSeq,
			@RequestParam("includeRoot") final boolean includeRoot) {
		TreeNode<FolderVo> folder = photoService.getFolderTree();
		FolderVo findFolder = new FolderVo();
		findFolder.setFolderSeq(folderSeq);
		TreeNode<FolderVo> baseFolder = folder.getTreeNode(findFolder);
		List<FolderVo> path = baseFolder.getPath();

		// 루트 경로 제거
		if (!includeRoot) {
			path.remove(0);
		}

		return new ResponseEntity<>(path, HttpStatus.OK);
	}

	/**
	 * 원본 사진 정보를 byte로 전송
	 *
	 * @param photoId
	 *            사진 아이디
	 * @param request
	 *            servletRequest
	 * @return 이미지 byte
	 * @throws IOException
	 *             파일 처리 오류
	 */
	@ResponseBody
	@RequestMapping(value = "/photo/getImageOrg.do", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getImageOrg(@RequestParam("photoId") final String photoId, final HttpServletRequest request)
			throws IOException {
		PhotoVo photo = photoRepository.findOne(photoId);

		String clientIp = request.getRemoteAddr();
		// 허가된 아이피가 아니면 비공개 이미지의 경로 정보를 제거함
		if (!isAllowAccessProtected(clientIp) && photo.isProtectF()) {
			logger.warn("deny access image. client ip:{}", clientIp);
			return null;
		}
		File photoFile = photo.getFullPath();

		try (InputStream in = new FileInputStream(photoFile);) {
			return IOUtils.toByteArray(in);
		}
	}

	/**
	 * 썸네일 사진 정보를 byte로 전송
	 *
	 * @param photoId
	 *            사진 아이디
	 * @param width
	 *            넓이 픽셀
	 * @param height
	 *            높이 픽셀
	 * @param request
	 *            servletRequest
	 * @return 섬네일 이미지 byte
	 * @throws IOException
	 *             파일 처리 오류
	 */
	@ResponseBody
	@RequestMapping("/photo/getImage.do")
	public byte[] getImage(@RequestParam("photoId") final String photoId, @RequestParam("w") final int width,
			@RequestParam("h") final int height, final HttpServletRequest request) throws IOException {
		PhotoVo photo = photoRepository.findOne(photoId);

		String clientIp = request.getRemoteAddr();
		// 허가된 아이피가 아니면 비공개 이미지의 경로 정보를 제거함
		if (!isAllowAccessProtected(clientIp) && photo.isProtectF()) {
			logger.warn("deny access image. client ip:{}", clientIp);
			return null;
		}

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
		String tempImg = FilenameUtils.getBaseName(name) + "_w" + width + "_h" + height + "."
				+ FilenameUtils.getExtension(name);

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
	 * @param photoId
	 *            사진 아이디
	 * @return 이미지 메타 정보
	 */
	@ResponseBody
	@RequestMapping("/photo/getMeta.do")
	public ResponseEntity<Map<String, String>> getImage(@RequestParam("photoId") final String photoId) {
		PhotoVo photo = photoRepository.findOne(photoId);
		Map<String, String> meta = PhotoService.getImageMeta(photo.getFullPath());
		return new ResponseEntity<>(meta, HttpStatus.OK);
	}

	// ============== 데이터 등록 ==============

	/**
	 * 사진 업로드
	 *
	 * @param request
	 *            파일 업로드 위한 multipart
	 * @return 처리 결과
	 * @throws IOException
	 *             첨부파일 처리 오류 시
	 */
	@RequestMapping("/photo/uploadProc.do")
	@ResponseBody
	public ResponseEntity<Boolean> uploadPhotoProc(final MultipartHttpServletRequest request) throws IOException {
		Iterator<String> itr = request.getFileNames();
		while (itr.hasNext()) {
			String uploadedFile = itr.next();
			MultipartFile uploadFile = request.getFile(uploadedFile);

			String name = uploadFile.getOriginalFilename();
			String ext = FilenameUtils.getExtension(name);
			// prefix가 최소 3자 이상 되어야 함.
			String nameWithOutExt = FilenameUtils.getBaseName(name) + "__";

			if (!BokslPhotoConstant.Photo.SAVE_DIR.exists()) {
				BokslPhotoConstant.Photo.SAVE_DIR.mkdir();
			}

			File saveFile = File.createTempFile(nameWithOutExt, "." + ext, BokslPhotoConstant.Photo.SAVE_DIR);
			uploadFile.transferTo(saveFile);
			boolean result = photoService.savePhoto(saveFile, false);
			if (result) {
				logger.info("upload {} -> {}", uploadFile.getOriginalFilename(), saveFile.getAbsolutePath());
			} else {
				logger.info("upload fail. ({})", uploadFile.getOriginalFilename());
			}
		}
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	/**
	 * 사진 관련 폴더 추가
	 *
	 * @param photoId
	 *            사진 파일 아이디
	 * @param folderSeq
	 *            폴더 일련 번호
	 * @return 처리 결과
	 */
	@RequestMapping("/photo/addRelationFolder.do")
	@ResponseBody
	public ResponseEntity<Boolean> addRelationFolder(@RequestParam("photoId") final String photoId,
			@RequestParam("folderSeq") final int folderSeq) {

		PhotoVo p = photoRepository.findOne(photoId);
		FolderVo f = folderRepository.findOne(folderSeq);

		if (p == null || f == null) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		}

		p.addFolder(f);
		photoRepository.saveAndFlush(p);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	/**
	 * 이미지 탐색 후 저장
	 *
	 * @return 처리결과
	 */
	@RequestMapping("/photo/retrievalAndSave.do")
	@ResponseBody
	public ResponseEntity<Boolean> retrievalAndSave() {
		photoService.retrievalPhotoAndSave();
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	/**
	 * 분류 폴더 추가
	 *
	 * @param folder
	 *            폴더
	 * @return 처리결과
	 */
	@RequestMapping("/photo/addFolder.do")
	@ResponseBody
	public ResponseEntity<Boolean> addFolder(final FolderVo folder) {
		folderRepository.save(folder);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	// ============== 데이터 수정 ==============

	/**
	 * 메모 정보 업데이트
	 *
	 * @param photoId
	 *            사진 파일 아이디
	 * @param memo
	 *            사진에 추가할 메모
	 * @return 처리 결과
	 */
	@RequestMapping("/photo/updateMemo.do")
	@ResponseBody
	public ResponseEntity<Boolean> updateMemo(@RequestParam("photoId") final String photoId,
			@RequestParam("memo") final String memo) {

		PhotoVo p = photoRepository.findOne(photoId);
		if (p == null) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		}

		p.setMemo(memo);
		photoRepository.save(p);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	/**
	 * 보호 이미지 지정
	 *
	 * @param photoId
	 *            사진 파일 아이디
	 * @param protect
	 *            보호 여부
	 * @param request
	 *            request
	 * @return 처리 결과
	 */
	@RequestMapping("/photo/updateProtect.do")
	@ResponseBody
	public ResponseEntity<Boolean> updateProtect(@RequestParam("photoId") final String photoId,
			@RequestParam("protect") final Boolean protect, final HttpServletRequest request) {

		PhotoVo p = photoRepository.findOne(photoId);
		if (p == null) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		}

		String clientIp = request.getRemoteAddr();
		// 보호 이미지 해제는 허가된 IP에서만 작업 할 수 있음.
		if (!protect && !isAllowAccessProtected(clientIp)) {
			logger.info("not allow ip:{}", clientIp);
			return new ResponseEntity<>(false, HttpStatus.OK);
		}
		p.setProtectF(protect);
		photoRepository.save(p);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	/**
	 * 분류 폴더 삭제
	 *
	 * @param folder
	 *            폴더
	 * @return 처리결과
	 */
	@RequestMapping("/photo/updateFolder.do")
	@ResponseBody
	public ResponseEntity<Boolean> updateFolder(final FolderVo folder) {

		FolderVo v = folderRepository.findOne(folder.getFolderSeq());
		v.setName(folder.getName());
		v.setParentId(folder.getParentId());

		folderRepository.save(v);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	// ============== 데이터 삭제 ==============
	/**
	 * 이미지 삭제<br>
	 * 물리적인 파일도 삭제함
	 *
	 * @param photoId
	 *            사진 파일 아이디
	 * @return 처리 결과
	 */
	@RequestMapping("/photo/deletePhoto.do")
	@ResponseBody
	public ResponseEntity<Boolean> deleteProtect(@RequestParam("photoId") final String photoId) {
		PhotoVo p = photoRepository.findOne(photoId);
		if (p == null) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		}
		boolean delete = p.getFullPath().delete();
		photoRepository.delete(p);
		logger.info("Photo Delete: {} ({})", p.getFullPath(), delete);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	/**
	 * 사진 관련 폴더 삭제
	 *
	 * @param photoId
	 *            사진 파일 아이디
	 * @param folderSeq
	 *            폴더 일련번호
	 * @return 처리 결과
	 */
	@RequestMapping("/photo/deleteRelationFolder.do")
	@ResponseBody
	public ResponseEntity<Boolean> deleteRelationFolder(@RequestParam("photoId") final String photoId,
			@RequestParam("folderSeq") final int folderSeq) {

		PhotoVo p = photoRepository.findOne(photoId);
		FolderVo f = folderRepository.findOne(folderSeq);

		if (p == null || f == null) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		}

		p.removeFolder(f);
		photoRepository.saveAndFlush(p);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	/**
	 * 중복 파일 제거
	 *
	 * @return 제거한 파일 경로. 파일 기준으로 BokslPhotoConstant.Photo#BASE_DIR 부터 시작
	 * @see BokslPhotoConstant.Photo#BASE_DIR
	 */
	@RequestMapping("/photo/deleteDuplicate.json")
	@ResponseBody
	public ResponseEntity<List<String>> deleteDuplicate() {
		List<File> deleteFiles = photoService.deleteDuplicate();

		List<String> result = deleteFiles.stream()
				.map(file -> ApplicationUtil.getRelativePath(BokslPhotoConstant.Photo.BASE_DIR, file))
				.collect(Collectors.toList());

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * 분류 폴더 삭제<br>
	 * 하위 분류까지 삭제 함
	 *
	 * @param folderSeq
	 *            분류 폴더 아이디
	 * @return 처리결과
	 */
	@RequestMapping("/photo/deleteFolder.do")
	@ResponseBody
	public ResponseEntity<Boolean> deleteFolder(@RequestParam("folderSeq") final int folderSeq) {
		photoService.deleteFolder(folderSeq);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	// ============== CRUD 성격 아님 ==============

	/**
	 * 비공개 이미지 접근 여부
	 *
	 * @param clientIp
	 *            클라이언트 아이피
	 * @return true: 비공개 이미지 접근
	 */
	private boolean isAllowAccessProtected(final String clientIp) {
		return clientIp.equals(BokslPhotoConstant.Photo.ALLOW_IP);
	}

	/**
	 * @param binder
	 *            날짜 바인드
	 */
	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
}
