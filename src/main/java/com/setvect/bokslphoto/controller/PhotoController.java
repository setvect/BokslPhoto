package com.setvect.bokslphoto.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.service.PhotoService;
import com.setvect.bokslphoto.util.TreeNode;
import com.setvect.bokslphoto.vo.PhotoDirectory;
import com.setvect.bokslphoto.vo.UserVo;

@Controller
public class PhotoController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PhotoService photoService;

	private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

	@RequestMapping(value = "/")
	public String index(HttpServletRequest request) {
		constraintLogin(request);
		return "redirect:/photo";
	}

	/**
	 * 강제 로그인<br>
	 * 개발 과정을 편리하게 하기 위함.
	 *
	 * @param request
	 */
	private void constraintLogin(HttpServletRequest request) {
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

	@RequestMapping("/login.do")
	public void login() {
	}

	@RequestMapping("/photo/uploadProc.do")
	@ResponseBody
	public ResponseEntity<Boolean> uploadPhotoProc(MultipartHttpServletRequest request) throws IOException {
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
	 * 사진 목록 보기 페이지
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("/photo/list.do")
	public String list(HttpServletRequest request) {
		return "photo/photo_list";
	}

	/**
	 * 사진 업로드 페이지
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("/photo/upload.do")
	public String upload(HttpServletRequest request) {
		return "photo/photo_upload";
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
	 * 모든 저장 경로를 폴더 구조로 반환.
	 *
	 * @return
	 */
	@RequestMapping("/photo/directory.json")
	@ResponseBody
	public ResponseEntity<TreeNode<PhotoDirectory>> getDiretory() {
		TreeNode<PhotoDirectory> dir = photoService.getDirectoryTree();
		return new ResponseEntity<>(dir, HttpStatus.OK);
	}

	@RequestMapping("/403")
	public String accessDenied() {
		return "errors/403";
	}

	@RequestMapping("/photo")
	public String admin() {
		return "photo/photo";
	}
}
