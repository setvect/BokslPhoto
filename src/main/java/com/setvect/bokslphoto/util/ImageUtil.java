package com.setvect.bokslphoto.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;

/**
 * 이미지 제어와 관련된 메소드
 */
public abstract class ImageUtil {
	/**
	 * 디카 이미지를 자동으로 회전 시킴
	 *
	 * @param photoFile
	 *            사진 파일
	 * @return 변환된 사진 정보
	 * @throws IOException
	 *             이미지 처리 예외
	 * @deprecated 속도가 너무 느림
	 */
	@Deprecated
	public static byte[] autoRotation(final File photoFile) throws IOException {
		// 디카 이미지 자동 회전을 위해 Thumbnails 라이브러리 사용
		BufferedImage image = Thumbnails.of(photoFile).scale(1).asBufferedImage();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", baos);
			baos.flush();
			byte[] imageByte = baos.toByteArray();
			return imageByte;
		}
	}

}
