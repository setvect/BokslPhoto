package com.setvect.bokslphoto.util;

import org.apache.commons.lang.StringUtils;

/**
 * anyframe.common.util.StringUtil에서 사용하고 있는 Methd Delegate
 *
 * @version $Id$
 */
public class StringUtil {

	/**
	 * @param word
	 *
	 * @return sql String 값에 들어가도록 변경
	 */
	public static String getSqlString(String word) {

		word = null2str(word);
		word = StringUtils.replace(word, "'", "''");
		word = word.trim();
		return new String("'" + word + "'");
	}

	/**
	 * @param word
	 * @return sql String 값에 like 형식으로 검색 되도록 변경<br>
	 *         예) '%단어%'
	 */
	public static String getSqlStringLike(String word) {
		word = null2str(word);
		word = StringUtils.replace(word, "'", "''");
		word = word.trim();
		return new String("'%" + word + "%'");
	}

	/**
	 * @param word
	 * @return sql String 값에 like 형식으로 검색 되도록 변경<br>
	 *         예) '%단어'
	 */
	public static String getSqlStringLikeLeft(String word) {
		word = null2str(word);
		word = StringUtils.replace(word, "'", "''");
		word = word.trim();
		return new String("'%" + word + "'");
	}

	/**
	 * @param word
	 * @return sql String 값에 like 형식으로 검색 되도록 변경<br>
	 *         예) '단어%'
	 */
	public static String getSqlStringLikeRight(String word) {
		word = null2str(word);
		word = StringUtils.replace(word, "'", "''");
		word = word.trim();
		return new String("'" + word + "%'");
	}

	/**
	 * @param word
	 *            문자열
	 * @return word가 null 이면 빈문자열로 변환
	 */
	public static String null2str(String word) {
		return word == null ? "" : word;
	}

	/**
	 * @param word
	 *            문자열
	 * @param substitution
	 *            대체 문자열
	 * @return word가 null 이면 대체 문자열로 변환
	 */
	public static String null2str(String word, String substitution) {
		if (word == null || word.equals("")) {
			return substitution;
		} else {
			return word;
		}
	}
}
