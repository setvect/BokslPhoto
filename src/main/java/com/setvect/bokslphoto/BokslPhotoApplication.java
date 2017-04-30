package com.setvect.bokslphoto;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

@SpringBootApplication
@Configuration
public class BokslPhotoApplication {
	private static final String CONFIG_CONFIG_PROPERTIES = "/application.properties";

	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(BokslPhotoApplication.class, args);
	}

	@Bean
	InitializingBean init() {
		return () -> {
			URL configUrl = BokslPhotoApplication.class.getResource(CONFIG_CONFIG_PROPERTIES);
			EnvirmentProperty.init(configUrl);
//
//			UserVo user = new UserVo();
//			user.setUserId("admin");
//			user.setName("관리자");
//			user.setEmail("a@abcde.com");
//			user.setDeleteF(false);
//			PasswordEncoder encoder = new BCryptPasswordEncoder();
//			user.setPassword(encoder.encode("1234"));
//
//			userRepository.save(user);
		};
	}
}
