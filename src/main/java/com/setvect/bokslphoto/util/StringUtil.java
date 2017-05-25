package com.setvect.bokslphoto.util;

import org.apache.commons.lang.StringUtils;

/**
 * 문자열 처리 관련 함수 제공
 */
public abstract class StringUtil {

	/**
	 * 인스턴스 말들지 않음.
	 */
	private StringUtil() {
	}

	/**
	 * @param word
	 *            입력 문자열
	 *
	 * @return sql String 값에 들어가도록 변경
	 */
	public static String getSqlString(final String word) {
		String w = word;
		w = null2str(w);
		w = StringUtils.replace(w, "'", "''");
		w = w.trim();
		return new String("'" + w + "'");
	}

	/**
	 * @param word
	 *            입력 문자열
	 * @return sql String 값에 like 형식으로 검색 되도록 변경<br>
	 *         예) '%단어%'
	 */
	public static String getSqlStringLike(final String word) {
		String w = word;
		w = null2str(w);
		w = StringUtils.replace(w, "'", "''");
		w = w.trim();
		return new String("'%" + w + "%'");
	}

	/**
	 * @param word
	 *            입력 문자열
	 * @return sql String 값에 like 형식으로 검색 되도록 변경<br>
	 *         예) '%단어'
	 */
	public static String getSqlStringLikeLeft(final String word) {
		String w = word;
		w = null2str(w);
		w = StringUtils.replace(w, "'", "''");
		w = w.trim();
		return new String("'%" + w + "'");
	}

	/**
	 * @param word
	 *            입력 문자열
	 * @return sql String 값에 like 형식으로 검색 되도록 변경<br>
	 *         예) '단어%'
	 */
	public static String getSqlStringLikeRight(final String word) {
		String w = word;
		w = null2str(w);
		w = StringUtils.replace(w, "'", "''");
		w = w.trim();
		return new String("'" + w + "%'");
	}

	/**
	 * @param word
	 *            문자열
	 * @return word가 null 이면 빈문자열로 변환
	 */
	public static String null2str(final String word) {
		return word == null ? "" : word;
	}

	/**
	 * @param word
	 *            문자열
	 * @param substitution
	 *            대체 문자열
	 * @return word가 null 이면 대체 문자열로 변환
	 */
	public static String null2str(final String word, final String substitution) {
		if (word == null || word.equals("")) {
			return substitution;
		} else {
			return word;
		}
	}
}
