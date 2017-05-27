package com.setvect.bokslphoto.test.temp.etc;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.setvect.bokslphoto.test.MainTestBase;

public class 패스워드_인코딩_TestCase extends MainTestBase {
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	public void test() {
		String a = passwordEncoder.encode("1234");
		System.out.println(a);

		System.out.println(passwordEncoder.encode("1234"));

		boolean b = passwordEncoder.matches("1234", a);
		System.out.println(b);

	}
}
