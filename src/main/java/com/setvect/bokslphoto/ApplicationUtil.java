package com.setvect.bokslphoto;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 어플리케이션 전반에 사용되는 공통 함수 제공
 */
public class ApplicationUtil {
	/**
	 * MD5 변환
	 *
	 * @param file
	 * @return
	 */
	public static String getMd5(File file) {
		String md5 = null;
		try (FileInputStream fis = new FileInputStream(file);) {
			md5 = DigestUtils.md5Hex(fis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return md5;
	}
}
