package com.setvect.bokslphoto;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

/**
 * 프로젝트 설정 정보 제공
 *
 * @version $Id$
 */
public class EnvirmentProperty {
	private static PropertiesConfiguration config;

	/**
	 * @param propertise
	 *            propertise 파일
	 */
	public static void init(File propertise) {
		try {
			PropertiesConfiguration conf = new PropertiesConfiguration(propertise);
			// 파일 수정 자동 감지
			conf.setReloadingStrategy(new FileChangedReloadingStrategy());
			config = conf;
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param propertise
	 *            propertise 파일
	 */
	public static void init(URL propertise) {
		try {
			PropertiesConfiguration conf = new PropertiesConfiguration(propertise);
			// 파일 수정 자동 감지
			conf.setReloadingStrategy(new FileChangedReloadingStrategy());
			config = conf;
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	// Delegate
	public static boolean containsKey(String paramString) {
		return config.containsKey(paramString);
	}

	@SuppressWarnings("rawtypes")
	public static Iterator getKeys(String paramString) {
		return config.getKeys(paramString);
	}

	@SuppressWarnings("rawtypes")
	public static Iterator getKeys() {
		return config.getKeys();
	}

	public static boolean getBoolean(String paramString) {
		return config.getBoolean(paramString);
	}

	public static boolean getBoolean(String paramString, boolean paramBoolean) {
		return config.getBoolean(paramString, paramBoolean);
	}

	public static byte getByte(String paramString) {
		return config.getByte(paramString);
	}

	public static byte getByte(String paramString, byte paramByte) {
		return config.getByte(paramString, paramByte);
	}

	public static double getDouble(String paramString) {
		return config.getDouble(paramString);
	}

	public static double getDouble(String paramString, double paramDouble) {
		return config.getDouble(paramString, paramDouble);
	}

	public static float getFloat(String paramString) {
		return config.getFloat(paramString);
	}

	public static float getFloat(String paramString, float paramFloat) {
		return config.getFloat(paramString, paramFloat);
	}

	public static Float getFloat(String paramString, Float paramFloat) {
		return config.getFloat(paramString, paramFloat);
	}

	public static int getInt(String paramString) {
		return config.getInt(paramString);
	}

	public static int getInt(String paramString, int paramInt) {
		return config.getInt(paramString, paramInt);
	}

	public static long getLong(String paramString) {
		return config.getLong(paramString);
	}

	public static long getLong(String paramString, long paramLong) {
		return config.getLong(paramString, paramLong);
	}

	public static short getShort(String paramString) {
		return config.getShort(paramString);
	}

	public static short getShort(String paramString, short paramShort) {
		return config.getShort(paramString, paramShort);
	}

	public static BigDecimal getBigDecimal(String paramString) {
		return config.getBigDecimal(paramString);
	}

	public static BigDecimal getBigDecimal(String paramString, BigDecimal paramBigDecimal) {
		return config.getBigDecimal(paramString, paramBigDecimal);
	}

	public static BigInteger getBigInteger(String paramString) {
		return config.getBigInteger(paramString);
	}

	public static BigInteger getBigInteger(String paramString, BigInteger paramBigInteger) {
		return config.getBigInteger(paramString, paramBigInteger);
	}

	public static String getString(String paramString) {
		return config.getString(paramString);
	}

	public static String getString(String paramString1, String paramString2) {
		return config.getString(paramString1, paramString2);
	}

	public static String[] getStringArray(String paramString) {
		return config.getStringArray(paramString);
	}

	@SuppressWarnings("rawtypes")
	public static List getList(String paramString) {
		return config.getList(paramString);
	}

	@SuppressWarnings("rawtypes")
	public static List getList(String paramString, List paramList) {
		return config.getList(paramString, paramList);
	}

}
