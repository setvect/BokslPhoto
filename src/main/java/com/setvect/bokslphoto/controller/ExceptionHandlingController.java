package com.setvect.bokslphoto.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 전역 예외 처리.
 */
@ControllerAdvice
@RestController
public class ExceptionHandlingController {
	/**
	 * 공통 예외 처리.
	 *
	 * @param req
	 *            servletRequest
	 * @param exception
	 *            전달 받은 예외 객체
	 * @return 예외 처리 페이지
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView handleError(final HttpServletRequest req, final Exception exception) {
		ModelAndView mav = new ModelAndView();
		exception.printStackTrace();
		return mav;
	}
}
