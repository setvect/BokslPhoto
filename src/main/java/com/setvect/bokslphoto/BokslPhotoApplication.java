package com.setvect.bokslphoto;

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

import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

@SpringBootApplication
@Configuration
public class BokslPhotoApplication {
	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(BokslPhotoApplication.class, args);
	}

	@Bean
	InitializingBean insertFixtureUsers() {
		return () -> {
			PhotoVo photo = new PhotoVo();
			photo.setId(2);
			photo.setFileName("aaa.jpg");
			photoRepository.save(photo);

			UserVo user = new UserVo();
			user.setUsername("admin");
			user.setEnabled(true);

			PasswordEncoder encoder = new BCryptPasswordEncoder();
			user.setPassword(encoder.encode("1234"));
			Set<UserRoleVo> userRole = new HashSet<>();
			UserRoleVo role = new UserRoleVo();
			role.setRole("ROLE_ADMIN");
			role.setUser(user);
			userRole.add(role);
			user.setUserRole(userRole);

			role = new UserRoleVo();
			role.setRole("ROLE_USER");
			role.setUser(user);
			userRole.add(role);
			user.setUserRole(userRole);

			userRepository.save(user);

		};
	}
}
