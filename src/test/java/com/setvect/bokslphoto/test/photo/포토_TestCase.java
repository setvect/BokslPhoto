package com.setvect.bokslphoto.test.photo;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.test.MainTestBase;
import com.setvect.bokslphoto.util.DateUtil;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.PhotoVo;

public class 포토_TestCase extends MainTestBase {

	@Autowired
	private PhotoRepository photoRepository;

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

		result.getList().stream().forEach(p -> logger.info(p.getPath()));

		System.out.println("끝. ====================");
	}

	@Test
	public void testGroupBy() {
		List<ImmutablePair<Date, Integer>> result = photoRepository.getGroupShotDate();
		result.stream().forEach(p -> {
			System.out.println(p.left);
			System.out.println(p.right);
		});

		// logger.debug("TotalCount: {}", result.size());

		System.out.println("끝. ====================");
	}

}
