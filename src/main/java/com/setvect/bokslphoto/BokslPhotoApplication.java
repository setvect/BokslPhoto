package com.setvect.bokslphoto;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

@SpringBootApplication
@Configuration
public class BokslPhotoApplication {
	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private UserRepository userRepository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) {
		SpringApplication.run(BokslPhotoApplication.class, args);
	}

	@Bean
	InitializingBean insertFixtureUsers() {
		logger.info("#########################################################");
		logger.info("#########################################################");
		logger.info("#########################################################");

		return () -> {
			FolderVo folder = new FolderVo();
			folder.setFolderSeq(0);
			folder.setParentId(0);
			folder.setName("ROOT");
			folderRepository.save(folder);

			PhotoVo photo = new PhotoVo();
			photo.setPhotoId("asasasas");
			photo.setPath("aaa.jpg");
			photo.setRegData(new Date());

			photo.setFolders(Arrays.asList(folder));

			photoRepository.save(photo);

			UserVo user = new UserVo();
			user.setUserId("admin");
			user.setName("관리자");
			user.setEmail("a@abcde.com");

			user.setDeleteF(true);

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
