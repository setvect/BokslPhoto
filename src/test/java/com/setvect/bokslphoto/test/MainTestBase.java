package com.setvect.bokslphoto.test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.setvect.bokslphoto.BokslPhotoApplication;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.service.PhotoService;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

/**
 * spring 테스트를 위한 설정<br>
 * spring과 연관된 테스트는 해당 클래스를 상속 한다.
 */
@RunWith(SpringRunner.class)
@Transactional()
@SpringBootTest(classes = { BokslPhotoApplication.class })
@TestPropertySource(locations = "classpath:test.properties")
public class MainTestBase {
	static {
		System.setProperty(BokslPhotoConstant.TEST_CHECK_PROPERTY_NAME, "true");
	}

	/** */
	@Autowired
	private FolderRepository folderRepository;

	/** */
	@Autowired
	private PhotoService photoService;

	/** */
	@Autowired
	private PhotoRepository photoRepository;

	/** 암호화 인코더. */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/** 사용자. */
	@Autowired
	private UserRepository userRepository;

	/**
	 * 초기 값 등록.
	 *
	 * @throws IOException
	 *             파일 복사 실패
	 */
	protected void insertInitValue() throws IOException {
		/*
		 * 기본 폴더에 있는 사진 지우고, 테스트 디렉토리에 있는 이미지에 있는 사진 복사
		 */
		File baseFolder = new File("./test_data/base_folder");
		FileUtils.deleteDirectory(baseFolder);

		File tempFolder = new File("./test_data/temp_folder");
		FileUtils.copyDirectory(tempFolder, baseFolder);

		photoService.retrievalPhotoAndSave();

		List<PhotoVo> allList = photoRepository.findAll();
		allList.get(0).setMemo("메모 1");
		allList.get(1).setMemo("메모 2");
		allList.get(2).setMemo("테스트 입니다.");

		FolderVo folderRoot = new FolderVo();
		folderRoot.setParentId(1);
		folderRoot.setName("ROOT");
		folderRepository.save(folderRoot);
		folderRoot.setParentId(folderRoot.getFolderSeq());
		folderRepository.save(folderRoot);

		FolderVo folderSub = new FolderVo();
		folderSub.setParentId(folderRoot.getFolderSeq());
		folderSub.setName("SUB1");
		folderRepository.save(folderSub);

		folderSub = new FolderVo();
		folderSub.setParentId(folderRoot.getFolderSeq());
		folderSub.setName("SUB2");
		folderRepository.save(folderSub);

		FolderVo folderSubSub = new FolderVo();
		folderSubSub.setParentId(folderSub.getFolderSeq());
		folderSubSub.setName("SUB2-1");
		folderRepository.save(folderSubSub);

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
