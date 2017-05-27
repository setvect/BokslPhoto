package com.setvect.bokslphoto.test.temp.etc;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.setvect.bokslphoto.ApplicationUtil;

public class EtcTestCase {
	// @Test
	public void testMap() {
		Map<String, String> hash = new HashMap<>();
		hash.put("AAA", null);
		hash.put("BBB", null);

		Map<String, String> table = new Hashtable<>();
		table.put("AAA", null);

		System.out.println("끝.");
	}

	@Test
	public void testGetRelativePath() {
		File basePath = new File("/home/user/");
		File filePath = new File("/home/user/temp/readme.txt");
		String relative = ApplicationUtil.getRelativePath(basePath, filePath);
		Assert.assertThat(relative, CoreMatchers.is("temp/readme.txt"));

		basePath = new File("/home/user/../user");
		filePath = new File("/home/user/temp/readme.txt");
		relative = ApplicationUtil.getRelativePath(basePath, filePath);
		Assert.assertThat(relative, CoreMatchers.is("temp/readme.txt"));

		basePath = new File(".");
		filePath = new File("./abcde.txt");
		relative = ApplicationUtil.getRelativePath(basePath, filePath);
		Assert.assertThat(relative, CoreMatchers.is("abcde.txt"));

	}
}