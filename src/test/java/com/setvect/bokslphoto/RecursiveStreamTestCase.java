package com.setvect.bokslphoto;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

public class RecursiveStreamTestCase extends MainTestBase {

	@Test
	public void test() {
		ApplicationUtil.listFiles(Paths.get(".")).filter(p -> {
			String name = p.getFileName().toString();
			String ext = FilenameUtils.getExtension(name);
			return BokslPhotoConstant.Photo.ALLOW.contains(ext);
		}).forEach(x -> {
			System.out.println("AAAAAAAAAAAAAAAAAAAAA" + x);
		});
		;
	}
}