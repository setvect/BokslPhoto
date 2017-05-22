package com.setvect.bokslphoto;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 상수 정의.
 */
public final class BokslPhotoConstant {
	/**
	 * not instance.
	 */
	private BokslPhotoConstant() {
	}

	/**
	 * 웹 관련 상수
	 */
	public static class WEB {
		/** 한페이지에 불러오 항목 수 */
		public static final int DEFAULT_PAGE_SIZE = 10;
	}

	/**
	 * 로그인 관련 상수.
	 */
	public static class Login {
		/** remember 관련. */
		public static final String REMEMBER_ME_KEY = "bokslLoginKey";
		/** remember 관련. */
		public static final String REMEMBER_COOKIE_NAME = "bokslCookie";
	}

	/**
	 * 포토 서비스 관련 상수.
	 */
	public static class Photo {
		/** 이미지 저장 기본 경로. */
		public static final File BASE_DIR = new File(EnvirmentProperty.getString("com.setvect.photo.base"));

		/**
		 * 웹으로 업로드한 이미지 저장 경로<br>
		 * BASE_DIR 하위 디렉토리로 함.<br>
		 *
		 * @see Photo#BASE_DIR
		 */
		public static final File SAVE_DIR = new File(BASE_DIR, "web_upload");

		/** 허용 이미지 파일 확장자. */
		public static final Set<String> ALLOW = new HashSet<>(Arrays.asList("jpg", "png", "gif"));
	}

	/**
	 * 이미지 메타정보.
	 */
	public static class ImageMeta {
		/** 촬영일. */
		public static final String DATE_TIME_ORIGINAL = "Date/Time Original";

		/** 위도. */
		public static final String GPS_LATITUDE = "GPS Latitude";

		/** 경도. */
		public static final String GPS_LONGITUDE = "GPS Longitude";

	}

	/**
	 * 정규표현식.
	 */
	public static class RegexPattern {
		/** GPS 좌표. */
		public static final String GPS = "^(\\d*)\\W*(\\d*)\\W*((?:\\d|\\.)*)";
	}

	/** WHERE 절 replace를 하기 위함. */
	public static final String SQL_WHERE = "{WHERE}";

}
