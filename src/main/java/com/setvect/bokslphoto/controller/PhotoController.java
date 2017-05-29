package com.setvect.bokslphoto.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
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
import com.setvect.bokslphoto.util.DateRange;
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

	// 뷰 페이지 오픈

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

	// 데이터 조회

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
		Map<DateRange, Integer> dateCountMap = photoService.groupByDate(searchParam.getSearchDateGroup());

		List<GroupByDate> reseult = dateCountMap.entrySet().stream().map(entry -> {
			GroupByDate g = new GroupByDate();
			g.setFrom(entry.getKey().getStartString("yyyyMMdd"));
			g.setTo(entry.getKey().getEndString("yyyyMMdd"));
			g.setCount(entry.getValue());
			return g;
		}).collect(Collectors.toList());

		return reseult;
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

	// 데이터 등록

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

	// 데이터 수정

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
	 * @return 처리 결과
	 */
	@RequestMapping("/photo/updateProtect.do")
	@ResponseBody
	public ResponseEntity<Boolean> updateProtect(@RequestParam("photoId") final String photoId,
			@RequestParam("protect") final Boolean protect, HttpServletRequest request) {

		PhotoVo p = photoRepository.findOne(photoId);
		if (p == null) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		}

		String clientIp = request.getRemoteAddr();
		// 보호 이미지 해제는 허가된 IP에서만 작업 할 수 있음.
		if (!protect && !clientIp.equals(BokslPhotoConstant.Photo.ALLOW_IP)) {
			logger.info("not allow ip:{}", clientIp);
			return new ResponseEntity<>(false, HttpStatus.OK);
		}
		p.setProtectF(protect);
		photoRepository.save(p);
		return new ResponseEntity<>(true, HttpStatus.OK);
	}

	// 데이터 삭제
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

}
