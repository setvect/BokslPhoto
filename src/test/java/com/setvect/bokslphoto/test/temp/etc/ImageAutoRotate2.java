package com.setvect.bokslphoto.test.temp.etc;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.setvect.bokslphoto.util.LapTimeChecker;

import net.coobird.thumbnailator.Thumbnails;

public class ImageAutoRotate2 {
	public static void main(String[] args) throws IOException {

		LapTimeChecker ck = new LapTimeChecker("aaa");
		File input = new File("temp/aa.jpg");
		BufferedImage image = Thumbnails.of(input).scale(1).asBufferedImage();
		ck.check("img");

	}
}
