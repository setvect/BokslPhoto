package com.setvect.bokslphoto.test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
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

	/** 엔티티의 캐시를 지우기 위한 목적 */
	@PersistenceContext
	private EntityManager entityManager;

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
		allList.get(1).addFolder(folderRoot);
		allList.get(2).addFolder(folderRoot);

		FolderVo folderSub1 = new FolderVo();
		folderSub1.setParentId(folderRoot.getFolderSeq());
		folderSub1.setName("SUB1");
		folderRepository.save(folderSub1);
		allList.get(1).addFolder(folderSub1);
		allList.get(10).addFolder(folderSub1);

		FolderVo folderSub2 = new FolderVo();
		folderSub2.setParentId(folderRoot.getFolderSeq());
		folderSub2.setName("SUB2");
		folderRepository.save(folderSub2);

		FolderVo folderSub2_1 = new FolderVo();
		folderSub2_1.setParentId(folderSub2.getFolderSeq());
		folderSub2_1.setName("SUB2-1");
		folderRepository.save(folderSub2_1);
		allList.get(9).addFolder(folderSub2_1);
		allList.get(10).addFolder(folderSub2_1);
		allList.get(11).addFolder(folderSub2_1);

		photoRepository.saveAndFlush(allList.get(1));
		photoRepository.saveAndFlush(allList.get(2));
		photoRepository.saveAndFlush(allList.get(9));
		photoRepository.saveAndFlush(allList.get(10));
		photoRepository.saveAndFlush(allList.get(11));

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

		// TODO 삭제 해보기
		folderRepository.saveAndFlush(folderRoot);
		folderRepository.saveAndFlush(folderSub1);
		folderRepository.saveAndFlush(folderSub2);
		folderRepository.saveAndFlush(folderSub2_1);

		entityManager.refresh(allList.get(1));
		entityManager.refresh(allList.get(2));
		entityManager.refresh(allList.get(9));
		entityManager.refresh(allList.get(10));
		entityManager.refresh(allList.get(11));

		entityManager.refresh(folderRoot);
		entityManager.refresh(folderSub1);
		entityManager.refresh(folderSub2);
		entityManager.refresh(folderSub2_1);
	}
}
