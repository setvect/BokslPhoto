package com.setvect.bokslphoto.test.photo;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.test.MainTestBase;
import com.setvect.bokslphoto.util.DateUtil;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoVo;

public class 포토_TestCase extends MainTestBase {

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private FolderRepository folderRepository;

	private Logger logger = LoggerFactory.getLogger(포토_TestCase.class);

	/**
	 * 목록 조회
	 */
	// @Test
	public void test_list() {
		PhotoSearchParam pageCondition = new PhotoSearchParam(0, 10);
		pageCondition.setSearchFrom(DateUtil.getDate("2014-01-01", "yyyy-MM-dd"));
		pageCondition.setSearchTo(DateUtil.getDate("2017-05-01", "yyyy-MM-dd"));

		GenericPage<PhotoVo> result = photoRepository.getPhotoPagingList(pageCondition);
		logger.debug("TotalCount: {}", result.getTotalCount());

		result.getList().stream().forEach(p -> logger.info(p.getDirectory()));

		System.out.println("끝. ====================");
	}

	// @Test
	public void testGroupBy() {
		List<ImmutablePair<Date, Integer>> result = photoRepository.getGroupShotDate();
		result.stream().forEach(p -> {
			System.out.println(p.left);
			System.out.println(p.right);
		});

		logger.debug("TotalCount: {}", result.size());

		System.out.println("끝. ====================");
	}

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

		FolderVo a = folderRepository.getOne(2);
		System.out.printf("========= %s: %s\n", a, a.getPhotoCount());
		System.out.println("끝. ====================");
	}
}
