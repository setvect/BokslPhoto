package com.setvect.bokslphoto.controller;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.vo.UserVo;

@Controller
public class PhotoController {
	@Autowired
	private UserRepository userRepository;
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

	@RequestMapping("/photo/upload.do")
	public void uploadPhoto(MultipartHttpServletRequest request) {
		Iterator<String> itr = request.getFileNames();
		while (itr.hasNext()) {
			String uploadedFile = itr.next();
			MultipartFile file = request.getFile(uploadedFile);
			logger.info("{}", file);
		}
	}

	@RequestMapping("/403")
	public String accessDenied() {
		return "errors/403";
	}

	@RequestMapping("/photo")
	public String admin() {
		return "photo/index";
	}
}
