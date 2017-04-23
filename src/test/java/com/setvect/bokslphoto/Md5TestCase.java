package com.setvect.bokslphoto;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public class Md5TestCase {
	public static void main(String[] args) throws IOException {

		File list = new File("temp");

		Stream.of(list.listFiles()).forEach(f -> {
			String md5 = ApplicationUtil.getMd5(f);
			System.out.printf("%s: %s\n", f, md5);
		});
	}
}