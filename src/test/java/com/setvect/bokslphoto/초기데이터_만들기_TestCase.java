package com.setvect.bokslphoto;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.setvect.bokslphoto.MainTestBase.TestConfiguration;
import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.service.PhotoService;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
@Rollback(false)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BokslPhotoApplication.class, TestConfiguration.class })

public class 초기데이터_만들기_TestCase {
	@Autowired
	private PhotoService photoService;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private UserRepository userRepository;

	private Logger logger = LoggerFactory.getLogger(초기데이터_만들기_TestCase.class);

	@Test
	public void test() throws InterruptedException {
		// photoService.retrievalPhoto();

		// FolderVo folder = new FolderVo();
		// folder.setFolderSeq(0);
		// folder.setParentId(0);
		// folder.setName("ROOT");
		// folderRepository.save(folder);

		UserVo user = new UserVo();
		user.setUserId("admin");
		user.setName("관리자-testcase");
		user.setEmail("a@abcde.com");

		user.setDeleteF(false);

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

		logger.info("================================= 끝.");
	}
}