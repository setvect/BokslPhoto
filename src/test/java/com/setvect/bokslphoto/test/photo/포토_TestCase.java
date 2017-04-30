package com.setvect.bokslphoto.test.photo;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.test.MainTestBase;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.PhotoVo;

public class 포토_TestCase extends MainTestBase {

	@Autowired
	private PhotoRepository photoRepository;

	private Logger logger = LoggerFactory.getLogger(포토_TestCase.class);

	/**
	 * 목록 조회
	 */
	@Test
	public void test_list() {
		PhotoSearchParam pageCondition = new PhotoSearchParam(0, 10);
		GenericPage<PhotoVo> result = photoRepository.getPhotoPagingList(pageCondition);
		logger.debug("TotalCount: {}", result.getTotalCount());

		result.getList().stream().forEach(p -> logger.info(p.getPath()));

		System.out.println("끝. ====================");
	}
}
