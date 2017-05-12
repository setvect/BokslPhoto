package com.setvect.bokslphoto;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.setvect.bokslphoto.vo.UserRoleVo;

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

	/**
	 * 하위 디레토리를 재귀적으로 탐색해 파일 목록을 제공
	 *
	 * @param path
	 * @return
	 */
	public static Stream<File> listFiles(File path) {
		if (path.isDirectory()) {
			try {
				return Stream.of(path.listFiles()).flatMap(ApplicationUtil::listFiles);
			} catch (Exception e) {
				return Stream.empty();
			}
		} else {
			return Stream.of(path);
		}
	}

	/**
	 * @param value
	 * @return
	 */
	public static Date getDate(LocalDateTime value) {
		ZonedDateTime zdt = value.atZone(ZoneId.systemDefault());
		Date d = Date.from(zdt.toInstant());
		return d;
	}

	public static List<GrantedAuthority> buildUserAuthority(Set<UserRoleVo> userRoles) {
		List<GrantedAuthority> authList = userRoles.stream().map(x -> new SimpleGrantedAuthority(x.getRole()))
				.collect(Collectors.toList());
		return authList;
	}

	/**
	 * filePath에서 basePath 경로를 제외.<br>
	 * 예)<br>
	 * basePath = /home/user/<br>
	 * filePath = /home/user/temp/readme.txt<br>
	 * 리턴값: temp/read.txt
	 *
	 * @param basePath
	 *            기준 경로(OS Full Path)
	 * @param filePath
	 *            파일 경로(OS Full Path)
	 * @return
	 */
	public static String getRelativePath(File basePath, File filePath) {
		String dir = basePath.toURI().relativize(filePath.toURI()).getPath();
		return dir;
	}

}
