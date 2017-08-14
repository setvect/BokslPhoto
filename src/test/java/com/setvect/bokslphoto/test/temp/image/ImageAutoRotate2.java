package com.setvect.bokslphoto.test.temp.image;

import java.io.File;
import java.io.IOException;

import com.setvect.bokslphoto.util.LapTimeChecker;

import net.coobird.thumbnailator.Thumbnails;

public class ImageAutoRotate2 {
	public static void main(String[] args) throws IOException {

		LapTimeChecker ck = new LapTimeChecker("aaa");
		File input = new File("temp/aa.jpg");
		File out = new File("temp/aa-out1.jpg");
		Thumbnails.of(input).scale(1).toFile(out);
		ck.check("img");
	}
}
