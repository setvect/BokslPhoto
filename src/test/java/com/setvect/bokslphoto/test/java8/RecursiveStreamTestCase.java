package com.setvect.bokslphoto.test.java8;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.test.MainTestBase;

public class RecursiveStreamTestCase extends MainTestBase {

	@Test
	public void test() {
		ApplicationUtil.listFiles(new File(".")).filter(p -> {
			String name = p.getName();
			String ext = FilenameUtils.getExtension(name);
			return BokslPhotoConstant.Photo.ALLOW.contains(ext);
		}).forEach(x -> {
			System.out.println("AAAAAAAAAAAAAAAAAAAAA" + x);
		});
		;
	}
}