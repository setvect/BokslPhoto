package com.setvect.bokslphoto.controller;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import com.setvect.bokslphoto.ApplicationRuntimeException;
import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.service.FolderAddtion;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.service.PhotoService;
import com.setvect.bokslphoto.service.PhotoService.StoreType;
import com.setvect.bokslphoto.util.DateRange;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.util.TreeNode;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoDirectory;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.PhotoVo.ShotDateType;
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
		// constraintLogin(request);

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
	 * @param request
	 * @return 사진 목록 보기 페이지
	 */
	@RequestMapping("/photo/listAll.do")
	public String allList() {
		return "photo/photo_list_all";
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
	@SuppressWarnings("unused")
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
	 * 전체 분류 폴더와, 해당 이미지가 포함된 폴더 정보를 반환
	 *
	 * @param photoId
	 *            사진 아이디
	 * @return 부가정보가 포함된 모든 분류 폴더. 최상위 루트 폴더 제외
	 */
	@RequestMapping("/photo/folderAddtionList.json")
	@ResponseBody
	public ResponseEntity<List<FolderAddtion>> getFolderAddtionList(@RequestParam("photoId") final String photoId) {
		List<FolderAddtion> result = photoService.getFolderAddtionList(photoId);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * @param folderSeq
	 *            폴더 아이디
	 * @param includeRoot
	 *            최상위 폴더 포함 여부
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
	 *             예외
	 */
	@ResponseBody
	@RequestMapping(value = "/photo/getImageOrg.do", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getImageOrg(@RequestParam("photoId") final String photoId, final HttpServletRequest request)
			throws IOException {
		PhotoVo photo = photoRepository.findOne(photoId);

		String clientIp = request.getRemoteAddr();
		// 허가된 아이피가 아니면 비공개 이미지의 경로 정보를 제거함
		if (!isAllowAccessProtected(clientIp) && photo.isProtectF()) {
			logger.info("deny access image. client ip:{}", clientIp);
			try (InputStream in = new FileInputStream(BokslPhotoConstant.Photo.PROTECT_IMAGE);) {
				return IOUtils.toByteArray(in);
			}
		}
		byte[] imageOrg = photoService.getImageOrg(photo);
		return imageOrg;
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
			logger.info("deny access image. client ip:{}", clientIp);
			try (InputStream in = new FileInputStream(BokslPhotoConstant.Photo.PROTECT_IMAGE);) {
				return IOUtils.toByteArray(in);
			}
		}
		return photoService.makeThumbimage(photo, width, height);
	}

	/**
	 * @param photoId
	 *            사진 아이디
	 * @return 이미지 메타 정보
	 */
	@ResponseBody
	@RequestMapping("/photo/getMeta.json")
	public ResponseEntity<Map<String, String>> getMeta(@RequestParam("photoId") final String photoId) {
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
			boolean result = photoService.savePhoto(saveFile, StoreType.UPDATE);
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
	 * 사진 관련 폴더 추가
	 *
	 * @param photoId
	 *            사진 파일 아이디
	 * @param folderSeq
	 *            폴더 일련 번호
	 * @return 처리 결과
	 */
	@RequestMapping("/photo/addRelationFolders.do")
	@ResponseBody
	public ResponseEntity<Boolean> addRelationFolders(@RequestParam("photoId") final String photoId,
			@RequestParam(value = "folderSeq", required = false) final int[] folderSeq) {
		PhotoVo p = photoRepository.findOne(photoId);
		Set<FolderVo> mappingFolder;
		if (folderSeq == null) {
			mappingFolder = Collections.emptySet();
		} else {
			mappingFolder = Arrays.stream(folderSeq).mapToObj(seq -> folderRepository.findOne(seq)).collect(toSet());
		}
		p.setFolders(mappingFolder);
		photoRepository.saveAndFlush(p);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	/**
	 * 서버 저장된 이미지 탐색 후 저장
	 *
	 * @param existUpdate
	 *            true: 이미지 자료가 DB에 있어도 이미지 경로 정보를 업데이트<br>
	 *            false: 업로드 이미지가 이미 DB에 저장되어 있다면 이미지 업데이트하지 않음.
	 * @return 처리결과
	 */
	@RequestMapping("/photo/retrievalAndSave.do")
	@ResponseBody
	public ResponseEntity<Boolean> retrievalAndSave(
			@RequestParam(name = "existUpdate", required = false) final Boolean existUpdate) {
		if (existUpdate == null || !existUpdate) {
			photoService.syncPhotoAndSave(StoreType.NO_OVERWRITE);
		} else {
			photoService.syncPhotoAndSave(StoreType.UPDATE);
		}

		photoService.deleteNonexistPhoto();

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
	 * 촬영일 정보 업데이트
	 *
	 * @param photoId
	 *            사진 파일 아이디
	 * @param shotDate
	 *            사진에 추가할 메모
	 * @return 처리 결과
	 */
	@RequestMapping("/photo/updateShotDate.do")
	@ResponseBody
	public ResponseEntity<Boolean> updateShotDate(@RequestParam("photoId") final String photoId,
			@RequestParam("shotDate") final Date shotDate) {
		PhotoVo p = photoRepository.findOne(photoId);
		p.setShotDate(shotDate);
		p.setShotDateType(ShotDateType.MANUAL);
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
	 * 분류 폴더 수정
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
	public ResponseEntity<Boolean> deletePhoto(@RequestParam("photoId") final String photoId) {
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
		try {
			photoService.deleteFolder(folderSeq);
		} catch (ApplicationRuntimeException e) {
			logger.warn(e.getMessage());
			return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	// ============== CRUD 성격 아님 ==============
	/**
	 * 접속 아이피가 보호 이미지 해제 할수 있는 여부를 알려줌
	 *
	 * @param request
	 *            request
	 * @return true: 접속 PC 보호 이미지 해제 가능, false: 해제 불가능
	 */
	@RequestMapping("/photo/isAllowAccessProtected.json")
	@ResponseBody
	public ResponseEntity<Boolean> isProtect(final HttpServletRequest request) {
		String clientIp = request.getRemoteAddr();
		boolean result = isAllowAccessProtected(clientIp);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

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