package com.setvect.bokslphoto.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 로그인 체크.<br>
 * 모든 액션에 대해서 로그인 여부를 검사하여 로그인이 되지 않으면 로그인 페이지로 이동
 */
@Service
public class SessionCheckInterceptor extends HandlerInterceptorAdapter {
	// private static final Logger logger =
	// LoggerFactory.getLogger(SessionCheckInterceptor.class);

	/**
	 * Application 시작과 동시에 최초 한번 실행.
	 */
	@PostConstruct
	public void init() {
	}

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
			throws UnsupportedEncodingException, IOException {
		return true;
	}
}