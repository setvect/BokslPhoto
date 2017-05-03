package com.setvect.bokslphoto.test.etc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class EtcTestCase {
	public static void main(String[] args) throws IOException {
		Map<String, String> hash = new HashMap<>();
		hash.put("AAA", null);
		hash.put("BBB", null);

		Map<String, String> table = new Hashtable<>();
		table.put("AAA", null);

		System.out.println("끝.");
	}
}