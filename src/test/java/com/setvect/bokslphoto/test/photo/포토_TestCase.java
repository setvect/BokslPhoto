package com.setvect.bokslphoto.test.photo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.service.DateGroup;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.service.PhotoService;
import com.setvect.bokslphoto.test.MainTestBase;
import com.setvect.bokslphoto.util.DateRange;
import com.setvect.bokslphoto.util.DateUtil;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.util.TreeNode;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoDirectory;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

/**
 * Web controller를 제외한 테스트.
 */
public class 포토_TestCase extends MainTestBase {

	/** */
	@Autowired
	private PhotoRepository photoRepository;

	/** */
	@Autowired
	private FolderRepository folderRepository;

	/** */
	@Autowired
	private PhotoService photoService;

	/** 암호화 인코더. */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/** 사용자. */
	@Autowired
	private UserRepository userRepository;

	/** */
	private Logger logger = LoggerFactory.getLogger(포토_TestCase.class);

	/**
	 * 이미지 탐색 후 저장. <br>
	 * 저장될 결과 조회
	 */
	@Before
	public void retrievalPhotoAndSave() {
		insertInitValue();
		System.out.println("끝. ====================");
	}

	/**
	 * 초기 값 등록.
	 */
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

	/**
	 * 목록 조회
	 */
	@Test
	public void testList() {
		PhotoSearchParam pageCondition = new PhotoSearchParam(0, 10);
		pageCondition.setSearchFrom(DateUtil.getDate("2014-01-01", "yyyy-MM-dd"));
		pageCondition.setSearchTo(DateUtil.getDate("2017-05-01", "yyyy-MM-dd"));
		String searchDirectory = "/여행/바다/";
		pageCondition.setSearchDirectory(searchDirectory);

		GenericPage<PhotoVo> result = photoRepository.getPhotoPagingList(pageCondition);

		Assert.assertThat(result.getTotalCount(), CoreMatchers.is(1));
		logger.debug("TotalCount: {}", result.getTotalCount());
		result.getList().stream().forEach(p -> {
			Assert.assertThat(p.getDirectory(), CoreMatchers.is(searchDirectory));
			logger.info(p.getDirectory());
		});
	}

	/**
	 * 디렉토리 구조 조회
	 */
	@Test
	public void testDirtory() {
		TreeNode<PhotoDirectory> rootNode = photoService.getDirectoryTree();

		List<TreeNode<PhotoDirectory>> nodeList = rootNode.exploreTree();
		nodeList.stream().forEach(t -> {
			String depthPadding = String.join("", Collections.nCopies(t.getLevel(), "--"));
			System.out.println(depthPadding + t.getData().getFullPath() + "  " + t.getData().getPhotoCount());
		});
		Assert.assertThat(nodeList.size(), CoreMatchers.is(6));
		Assert.assertThat(nodeList.get(2).getData().getFullPath(), CoreMatchers.is("/여행/바다/"));
		Assert.assertThat(nodeList.get(3).getData().getPhotoCount(), CoreMatchers.is(3));

		// System.out.println(rootNode.printData());
		System.out.println("끝. ====================");
	}

	/**
	 * 날짜별 사진 건수
	 */
	@Test
	public void testGroupBy() {
		List<ImmutablePair<Date, Integer>> result = photoRepository.getGroupShotDate();
		result.stream().forEach(p -> {
			System.out.printf("%tF: %,d\n", p.getLeft(), p.getRight());
		});

		Assert.assertThat(result.size(), CoreMatchers.is(13));
		Assert.assertNull(result.get(0).getLeft());
		Assert.assertThat(result.get(0).getRight(), CoreMatchers.is(4));
		Assert.assertThat(result.get(10).getRight(), CoreMatchers.is(2));

		logger.debug("TotalCount: {}", result.size());

		System.out.println("끝. ====================");
	}

	/**
	 * 폴더 추가<br>
	 * 폴더 매핑<br>
	 */
	@Test
	public void testFolder() {
		List<FolderVo> folderList = folderRepository.findAll();
		folderList.stream().forEach(p -> System.out.println(p));

		FolderVo folder = folderList.get(0);

		FolderVo forderNew1 = new FolderVo();
		forderNew1.setParentId(folder.getFolderSeq());
		forderNew1.setName("사진_폴더1");
		folderRepository.save(forderNew1);

		FolderVo forderNew2 = new FolderVo();
		forderNew2.setParentId(folder.getFolderSeq());
		forderNew2.setName("사진_폴더2");
		folderRepository.save(forderNew2);

		System.out.println("-------------------------------");

		folderList = folderRepository.findAll();
		Assert.assertThat(folderList.size(), CoreMatchers.is(3));

		folderList.stream().forEach(p -> System.out.println(p));

		System.out.println("------------------------------- 폴더 맵핑 전");
		PhotoSearchParam pageCondition = new PhotoSearchParam(0, 10);
		pageCondition.setSearchFrom(DateUtil.getDate("2014-01-01", "yyyy-MM-dd"));
		pageCondition.setSearchTo(DateUtil.getDate("2017-05-01", "yyyy-MM-dd"));

		GenericPage<PhotoVo> photoList = photoRepository.getPhotoPagingList(pageCondition);
		logger.debug("TotalCount: {}", photoList.getTotalCount());
		photoList.getList().stream().forEach(p -> System.out.println(p));

		photoList.getList().stream().forEach(p -> {
			p.addFolder(forderNew1);
			p.addFolder(forderNew2);
			photoRepository.save(p);
		});

		System.out.println("------------------------------- 폴더 맵핑 후");
		photoList = photoRepository.getPhotoPagingList(pageCondition);
		logger.debug("TotalCount: {}", photoList.getTotalCount());
		photoList.getList().stream().forEach(p -> System.out.println(p));

		GenericPage<PhotoVo> photoList2 = photoRepository.getPhotoPagingList(pageCondition);

		folderList = folderRepository.findAll();
		folderList.stream().forEach(p -> {
			p.addPhoto(photoList2.getList().get(0));
			folderRepository.save(p);
		});

		System.out.println("------------------------------- 폴더 조회");
		folderList = folderRepository.findAll();
		folderList.stream().forEach(p -> System.out.printf("%s: %s\n", p, p.getPhotoCount()));

		FolderVo folderGet = folderRepository.getOne(folderList.get(2).getFolderSeq());
		System.out.printf("========= %s: %s\n", folderGet, folderGet.getPhotoCount());
		Assert.assertThat(folderGet.getPhotoCount(), CoreMatchers.is(1));
		Assert.assertThat(folderGet.getPhotos().get(0).getFolders().size(), CoreMatchers.is(2));

		System.out.println("끝. ====================");
	}

	/**
	 * 중복된 파일 삭제
	 */
	@Test
	public void testDeleteDuplicate() {
		List<File> deleteFiles = photoService.deleteDuplicate();
		System.out.println("삭제 파일들");

		deleteFiles.stream().forEach(file -> {
			System.out.printf("%s(%s)\n", file, file.exists());
		});

		Assert.assertThat(deleteFiles.size(), CoreMatchers.is(0));
		System.out.println("끝. ====================");
	}

	/**
	 * 중복된 파일 찾기
	 */
	@Test
	public void testFindDuplicate() {
		Map<String, List<File>> result = photoService.findDuplicate();
		Assert.assertThat(result.size(), CoreMatchers.is(0));
		result.entrySet().stream().forEach(p -> {
			System.out.println(p.getKey());
			p.getValue().stream().map(file -> "\t" + file.getAbsolutePath()).forEach(System.out::println);

		});
		System.out.println("끝 ===============");
	}

	/**
	 * 날짜 그룹핑 Test
	 */
	@Test
	public void testGroupByDate() {
		for (DateGroup t : DateGroup.values()) {
			System.out.println("-------------- " + t);
			Map<DateRange, Integer> r = photoService.groupByDate(t);
			r.entrySet().stream().forEach(System.out::println);
		}

		Map<DateRange, Integer> r = photoService.groupByDate(DateGroup.DATE);
		Assert.assertThat(r.size(), CoreMatchers.is(13));

		r = photoService.groupByDate(DateGroup.MONTH);
		Assert.assertThat(r.size(), CoreMatchers.is(12));

		r = photoService.groupByDate(DateGroup.YEAR);
		Assert.assertThat(r.size(), CoreMatchers.is(10));

		Set<Entry<DateRange, Integer>> entry = r.entrySet();

		List<Entry<DateRange, Integer>> e = new ArrayList<>(entry);

		Assert.assertThat(e.get(0).getValue(), CoreMatchers.is(4));
		Assert.assertThat(e.get(1).getValue(), CoreMatchers.is(1));

		System.out.println("끝 ===============");
	}
}
