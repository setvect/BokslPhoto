package com.setvect.bokslphoto;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.service.PhotoService;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

/**
 * Spring boot application 시작점.
 */
@SpringBootApplication
public class BokslPhotoApplication extends SpringBootServletInitializer {
	/** 설정 파일 경로. */
	private static final String CONFIG_CONFIG_PROPERTIES = "/application.properties";

	/** 사용자. */
	@Autowired
	private UserRepository userRepository;

	/** 폴더. */
	@Autowired
	private FolderRepository folderRepository;

	/** 포토 서비스. */
	@Autowired
	private PhotoService photoService;

	/** 암호화 인코더. */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Application 시작점.
	 *
	 * @param args
	 *            사용 안함
	 */
	public static void main(final String[] args) {
		// spring boot에서 클래스가 및 properties 변경되었을 때 restart 안됨.
		// 즉 reload 효과
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(BokslPhotoApplication.class, args);
	}

	@Override
	protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
		return application.sources(BokslPhotoApplication.class);
	}

	/**
	 * 서비스 시작점 초기화.
	 *
	 * @return Spring boot 시작 bean
	 */
	@Bean
	InitializingBean init() {
		return () -> {
			URL configUrl = BokslPhotoApplication.class.getResource(CONFIG_CONFIG_PROPERTIES);
			EnvirmentProperty.init(configUrl);
			// insertInitValue();
		};
	}

	/**
	 * 초기 값 등록.
	 */
	@SuppressWarnings("unused")
	private void insertInitValue() {
		photoService.retrievalPhotoAndSave();

		FolderVo folder = new FolderVo();
		folder.setParentId(1);
		folder.setName("ROOT");
		folderRepository.save(folder);

		UserVo user = new UserVo();
		user.setUserId("admin");
		user.setName("관리자");
		user.setEmail("a@abcde.com");

		user.setDeleteF(false);

		user.setPassword(passwordEncoder.encode("1234"));
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
	}
}
