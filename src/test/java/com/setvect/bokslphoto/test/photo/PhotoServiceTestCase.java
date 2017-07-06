package com.setvect.bokslphoto.test.photo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import com.setvect.bokslphoto.vo.UserVo;

/**
 * Web controller를 제외한 테스트.
 */
public class PhotoServiceTestCase extends MainTestBase {

	/** */
	@Autowired
	private PhotoRepository photoRepository;

	/** */
	@Autowired
	private FolderRepository folderRepository;

	/** */
	@Autowired
	private PhotoService photoService;

	/** */
	@Autowired
	private UserRepository userRepository;

	/** */
	private Logger logger = LoggerFactory.getLogger(PhotoServiceTestCase.class);

	/**
	 * 이미지 탐색 후 저장. <br>
	 * 저장될 결과 조회
	 *
	 * @throws IOException
	 *             파일 복사 실패
	 */
	@Before
	public void init() throws IOException {
		insertInitValue();
		System.out.println("끝. ====================");
	}

	// ============== 데이터 조회 ==============
	/**
	 * 목록 조회
	 */
	@Test
	public void testList() {

		List<PhotoVo> all = photoRepository.findAll();
		for (PhotoVo a : all) {
			System.out.println(a);
		}

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
		PhotoSearchParam param = new PhotoSearchParam();
		param.setSearchDateGroup(DateGroup.DATE);
		List<ImmutablePair<Date, Integer>> result = photoRepository.getGroupShotDate(param);
		result.stream().forEach(p -> {
			System.out.printf("%tF: %,d\n", p.getLeft(), p.getRight());
		});

		Assert.assertThat(result.size(), CoreMatchers.is(13));
		Assert.assertNull(result.get(result.size() - 1).getLeft());
		Assert.assertThat(result.get(2).getRight(), CoreMatchers.is(2));
		Assert.assertThat(result.get(10).getRight(), CoreMatchers.is(1));

		logger.debug("TotalCount: {}", result.size());

		System.out.println("끝. ====================");
	}

	/**
	 * 회원 정보 조회
	 */
	@Test
	public void testUser() {
		UserVo user = userRepository.findOne("admin");
		Assert.assertThat(user.getUserId(), CoreMatchers.is("admin"));
		Assert.assertThat(user.getUserRole().size(), CoreMatchers.is(2));
	}

	/**
	 * 폴더 조회
	 */
	@Test
	public void testSelectFolder() {
		FolderVo folderRoot = folderRepository.findOne(1);
		Assert.assertNotNull(folderRoot);
		Assert.assertNotNull(folderRoot.getPhotos());
		Assert.assertThat(folderRoot.getPhotoCount(), CoreMatchers.is(2));
		List<FolderVo> children = folderRoot.getChildren();
		Assert.assertThat(children.size(), CoreMatchers.is(3));

		FolderVo folderSub = folderRepository.findOne(3);
		Assert.assertThat(folderSub.getParent(), CoreMatchers.is(folderRoot));
	}

	// ============== 데이터 등록 ==============
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
		Assert.assertThat(folderList.size(), CoreMatchers.is(6));

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
		folderList.stream().forEach(f -> {
			// TODO 이것 안해도 되야 되는데..
			// 위에서 사진에 폴더를 추가 했으면 폴더에 속한 사진은 자동적으로 카운트가 되야되는 것 아닌가?
			// 아 모르겠다.
			f.addPhoto(photoList2.getList().get(0));
			folderRepository.save(f);
		});

		System.out.println("------------------------------- 폴더 조회");
		folderList = folderRepository.findAll();
		folderList.stream().forEach(p -> System.out.printf("%s: %s\n", p, p.getPhotoCount()));

		FolderVo folderGet = folderRepository.getOne(folderList.get(2).getFolderSeq());
		System.out.printf("========= %s: %s\n", folderGet, folderGet.getPhotoCount());

		Assert.assertThat(folderGet.getPhotoCount(), CoreMatchers.is(1));
		Assert.assertThat(folderGet.getPhotos().get(0).getFolders().size(), CoreMatchers.is(4));

		System.out.println("끝. ====================");
	}

	/**
	 * 중복된 파일 찾기
	 */
	@Test
	public void testFindDuplicate() {
		Map<String, List<File>> result = photoService.findDuplicate();
		Assert.assertThat(result.size(), CoreMatchers.is(1));
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
			PhotoSearchParam param = new PhotoSearchParam();
			param.setSearchDateGroup(t);
			Map<DateRange, Integer> r = photoService.groupByDate(param);
			r.entrySet().stream().forEach(System.out::println);
		}

		PhotoSearchParam param = new PhotoSearchParam();
		param.setSearchDateGroup(DateGroup.DATE);
		Map<DateRange, Integer> r = photoService.groupByDate(param);
		Assert.assertThat(r.size(), CoreMatchers.is(13));

		param.setSearchDateGroup(DateGroup.MONTH);
		r = photoService.groupByDate(param);
		Assert.assertThat(r.size(), CoreMatchers.is(12));

		param.setSearchDateGroup(DateGroup.YEAR);
		r = photoService.groupByDate(param);
		Assert.assertThat(r.size(), CoreMatchers.is(10));

		Set<Entry<DateRange, Integer>> entry = r.entrySet();

		List<Entry<DateRange, Integer>> e = new ArrayList<>(entry);

		Assert.assertThat(e.get(0).getValue(), CoreMatchers.is(2));
		Assert.assertThat(e.get(e.size() - 1).getValue(), CoreMatchers.is(4));

		System.out.println("끝 ===============");
	}

	/**
	 * 폴더 트리 테스트
	 */
	@Test
	public void testFolderTree() {
		TreeNode<FolderVo> folderTree = photoService.getFolderTree();

		// 1. 초기값 테스트
		List<TreeNode<FolderVo>> nodeList = folderTree.exploreTree();
		nodeList.stream().forEach(t -> {
			String depthPadding = String.join("", Collections.nCopies(t.getLevel(), "--"));
			System.out.println(depthPadding + t.getData().getName() + "  " + t.getData().getPhotoCount());
		});
		Assert.assertThat(nodeList.size(), CoreMatchers.is(4));
		Assert.assertThat(nodeList.get(0).getData().getName(), CoreMatchers.is("ROOT"));
		FolderVo sub = nodeList.get(1).getData();
		Assert.assertThat(sub.getName(), CoreMatchers.is("SUB1"));
		Assert.assertThat(sub.getPhotoCount(), CoreMatchers.is(2));

		// 2. 폴더 추가. 1개
		FolderVo subOfFolder = new FolderVo();
		subOfFolder.setName("SUB_1");
		subOfFolder.setParentId(sub.getFolderSeq());
		folderRepository.save(subOfFolder);

		List<FolderVo> folderList = folderRepository.findAll();
		Assert.assertThat(folderList.size(), CoreMatchers.is(5));

		folderTree = photoService.getFolderTree();
		nodeList = folderTree.exploreTree();
		nodeList.stream().forEach(t -> {
			String depthPadding = String.join("", Collections.nCopies(t.getLevel(), "--"));
			System.out.println(depthPadding + t.getData().getName() + "  " + t.getData().getPhotoCount());
		});
		Assert.assertThat(nodeList.size(), CoreMatchers.is(5));
		Assert.assertThat(nodeList.get(2).getData().getName(), CoreMatchers.is("SUB_1"));
		Assert.assertThat(nodeList.get(2).getLevel(), CoreMatchers.is(2));

		// 2. 폴더 추가. 2개
		subOfFolder = new FolderVo();
		subOfFolder.setName("SUB_2");
		subOfFolder.setParentId(sub.getFolderSeq());
		folderRepository.save(subOfFolder);

		folderTree = photoService.getFolderTree();
		nodeList = folderTree.exploreTree();
		nodeList.stream().forEach(t -> {
			String depthPadding = String.join("", Collections.nCopies(t.getLevel(), "--"));
			System.out.println(depthPadding + t.getData().getName() + "  " + t.getData().getPhotoCount());
		});
		Assert.assertThat(nodeList.size(), CoreMatchers.is(6));
		Assert.assertThat(nodeList.get(3).getData().getName(), CoreMatchers.is("SUB_2"));
		Assert.assertThat(nodeList.get(3).getLevel(), CoreMatchers.is(2));

	}

	/**
	 * 이미지 메타 정보 확인
	 */
	@Test
	public void testImageMeta() {
		List<PhotoVo> all = photoRepository.findAll();
		Map<String, String> meta = PhotoService.getImageMeta(all.get(2).getFullPath());

		meta.entrySet().stream().forEach(entry -> {
			System.out.printf("%s  :: %s\n", entry.getKey(), entry.getValue());
		});

		Assert.assertThat(meta.get("[Exif IFD0]Date/Time"), CoreMatchers.is("2016:05:01 11:42:12"));
		Assert.assertThat(meta.get("[Exif IFD0]Image Width"), CoreMatchers.is("1280 pixels"));
		Assert.assertThat(meta.get("[Exif IFD0]Focal Length"), CoreMatchers.is("4.4 mm"));

		System.out.println("\n======================\n");

		meta = PhotoService.getImageMeta(all.get(all.size() - 1).getFullPath());
		meta.entrySet().stream().forEach(entry -> {
			System.out.printf("%s  :: %s\n", entry.getKey(), entry.getValue());
		});
		Assert.assertThat(meta.get("[File]File Size"), CoreMatchers.is("74665 bytes"));
	}
	// ============== 데이터 수정 ==============

	// ============== 데이터 삭제 ==============

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

		Assert.assertThat(deleteFiles.size(), CoreMatchers.is(1));
		System.out.println("끝. ====================");
	}

}
