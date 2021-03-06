package com.setvect.bokslphoto.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.h2.tools.Server;
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
import com.setvect.bokslphoto.service.PhotoService.StoreType;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

/**
 * spring 테스트를 위한 설정<br>
 * spring과 연관된 테스트는 해당 클래스를 상속 한다.
 */
@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(classes = { BokslPhotoApplication.class })
@TestPropertySource(locations = "file:" + BokslPhotoApplication.CONFIG_CONFIG_PROPERTIES_TEST)
public class MainTestBase {
	static {
		System.setProperty(BokslPhotoConstant.TEST_CHECK_PROPERTY_NAME, "true");
		// openH2WebConsole();
	}

	/**
	 * 디버깅중 데이터 입력 값을 확인하기 위해 만듦.<br>
	 * TEST 중 JDBC 접속은 TCP로 되어야 됨<br>
	 * jdbc:h2:mem:BokslPhto <-- 메모리로 DB를 오픈하면 당연히 접속을 못함.<br>
	 *
	 * 하지만 테스트 과정에서 트랜잭션이 적용되었기 때문에 테스트 과정에서 입력된 데이터를 확인할 수 없다.
	 */
	private static void openH2WebConsole() {
		try {
			String[] optionWeb = "-webAllowOthers -webPort 8082".split(" ");
			Server h2Web;
			h2Web = Server.createWebServer(optionWeb);
			h2Web.setOut(new PrintStream(System.out));
			h2Web.start();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	/** 엔티티의 refrash, merge 등을 관리하기 위해 */
	@Autowired
	private EntityManager entityManager;

	/** 로깅 */
	// private Logger logger =
	// LoggerFactory.getLogger(PhotoServiceTestCase.class);

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

		photoService.syncPhotoAndSave(StoreType.UPDATE);

		List<PhotoVo> allList = photoRepository.findAll();
		allList.get(0).setMemo("메모 1");
		allList.get(1).setMemo("메모 2");
		allList.get(2).setMemo("테스트 입니다.");

		Query a = entityManager
				.createNativeQuery("insert into tbbb_folder  (folder_seq, parent_id, name) values (?, ?, ?)");
		a.setParameter(1, 1).setParameter(2, 1).setParameter(3, "ROOT").executeUpdate();

		FolderVo folderRoot = folderRepository.findOne(1);

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

		FolderVo folderSub21 = new FolderVo();
		folderSub21.setParentId(folderSub2.getFolderSeq());
		folderSub21.setName("SUB2-1");
		folderRepository.save(folderSub21);
		allList.get(9).addFolder(folderSub21);
		allList.get(10).addFolder(folderSub21);
		allList.get(11).addFolder(folderSub21);

		photoRepository.save(allList.get(1));
		photoRepository.save(allList.get(2));
		photoRepository.save(allList.get(9));
		photoRepository.save(allList.get(10));
		photoRepository.save(allList.get(11));

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

		photoRepository.flush();
		folderRepository.flush();
	}
}
