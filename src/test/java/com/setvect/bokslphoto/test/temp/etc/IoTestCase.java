package com.setvect.bokslphoto.test.temp.etc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

public class IoTestCase {
	// @Test
	public void testPath() {
		Path p = Paths.get("c:\\temp");
		System.out.println(p);

		Path pp = p.resolve("b\\c");
		System.out.println(pp);
	}

	@Test
	public void testCreateFileName() throws IOException {
		File a = File.createTempFile("file_", ".jpg", new File("c:\\temp"));
		System.out.println(a);

		String withoutExt = FilenameUtils.getBaseName(a.getName());
		System.out.println(withoutExt);
	}
}
