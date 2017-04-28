package com.setvect.bokslphoto;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 상수 정의
 */
public class BokslPhotoConstant {
	public static class Login {
		public static final String REMEMBER_ME_KEY = "bokslLoginKey";
		public static final String REMEMBER_COOKIE_NAME = "bokslCookie";
	}

	public static class Photo {
		/** 이미지 저장 기본 경로 */
		public static final Path BASE_DIR = Paths.get(EnvirmentProperty.getString("com.setvect.photo.base"));
		/** 허용 이미지 */
		public static final Set<String> ALLOW = new HashSet<>(Arrays.asList("jpg", "png", "gif"));

	}

}
