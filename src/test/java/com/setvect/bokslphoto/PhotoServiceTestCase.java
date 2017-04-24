package com.setvect.bokslphoto;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.setvect.bokslphoto.service.PhotoService;

public class PhotoServiceTestCase extends MainTestBase {
	@Autowired
	private PhotoService photoService;
	private Logger logger = LoggerFactory.getLogger(PhotoServiceTestCase.class);

	@Test
	public void test() {
		photoService.retrievalPhoto();

		logger.info("================================= 끝.");
	}
}