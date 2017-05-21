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
 * 어플리케이션 전반에 사용되는 공통 함수 제공.
 */
public final class ApplicationUtil {

	/**
	 * not instance.
	 */
	private ApplicationUtil() {

	}

	/**
	 * MD5 변환.
	 *
	 * @param file
	 *            대상 파일
	 * @return MD5
	 */
	public static String getMd5(final File file) {
		String md5 = null;
		try (FileInputStream fis = new FileInputStream(file);) {
			md5 = DigestUtils.md5Hex(fis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return md5;
	}

	/**
	 * 하위 디레토리를 재귀적으로 탐색해 파일 목록을 제공.
	 *
	 * @param path
	 *            탐색할 경로
	 * @return 하위 파일 탐색용 스트림
	 */
	public static Stream<File> listFiles(final File path) {
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
	 *            변환 날짜 값
	 * @return LocalDateTime를 Date로 변환된 값
	 */
	public static Date getDate(final LocalDateTime value) {
		ZonedDateTime zdt = value.atZone(ZoneId.systemDefault());
		Date d = Date.from(zdt.toInstant());
		return d;
	}

	/**
	 * @param userRoles
	 *            Role 정보
	 * @return UserRoleVo를 GrantedAuthority로 변환
	 */
	public static List<GrantedAuthority> buildUserAuthority(final Set<UserRoleVo> userRoles) {
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
	 * @return filePath에서 basePath 경로를 제외된 값
	 */
	public static String getRelativePath(final File basePath, final File filePath) {
		String dir = basePath.toURI().relativize(filePath.toURI()).getPath();
		return dir;
	}

}
