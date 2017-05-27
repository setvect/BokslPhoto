package com.setvect.bokslphoto.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.setvect.bokslphoto.test.photo.PhotoControllerTestCase;
import com.setvect.bokslphoto.test.photo.PhotoServiceTestCase;

/**
 * 전체 테스트
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ PhotoServiceTestCase.class, PhotoControllerTestCase.class })
public class BokslPhotoAllTests {
}
