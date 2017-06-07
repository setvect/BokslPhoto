package com.setvect.bokslphoto.service;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.coobird.thumbnailator.Thumbnails;

/**
 * 섬네일 이미지 제작
 */
public abstract class ThumbnailImageConvert {

	/** 로깅 */
	private static Logger logger = LoggerFactory.getLogger(ThumbnailImageConvert.class);

	/**
	 * 파일 확장자를 보고 gif->jpg, jpg->jpg 결정
	 *
	 * @param orgFile
	 *            원본 파일
	 * @param destFile
	 *            목적지 파일
	 * @param wishSizeW
	 *            최대 넓이
	 * @param wishSizeH
	 *            최대 높이
	 */
	public static void makeThumbnail(final File orgFile, final File destFile, final int wishSizeW,
			final int wishSizeH) {
		try {
			Thumbnails.of(orgFile).size(wishSizeW, wishSizeH).toFile(destFile);
			logger.info(String.format("make thumbnail %s -> %s", orgFile.getName(), destFile.getName()));
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

}
