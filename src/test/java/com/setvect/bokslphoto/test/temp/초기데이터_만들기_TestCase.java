package com.setvect.bokslphoto.test.temp;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.setvect.bokslphoto.BokslPhotoApplication;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { BokslPhotoApplication.class })
@Transactional
@Rollback(true)
public class 초기데이터_만들기_TestCase {

	@Autowired
	private UserRepository userRepository;

	private Logger logger = LoggerFactory.getLogger(초기데이터_만들기_TestCase.class);

	@Test
	@Commit
	public void test() throws InterruptedException {
		// photoService.retrievalPhoto();

		// FolderVo folder = new FolderVo();
		// folder.setFolderSeq(0);
		// folder.setParentId(0);
		// folder.setName("ROOT");
		// folderRepository.save(folder);

		UserVo user = new UserVo();
		user.setUserId("33332");
		user.setName("관리자-testcase111111111111111");
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