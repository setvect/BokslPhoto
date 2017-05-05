package com.setvect.bokslphoto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PhotoController {

	@RequestMapping(value = "/")
	public String index() {
		return "index";
	}

	@RequestMapping("/login.do")
	public void login() {
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
