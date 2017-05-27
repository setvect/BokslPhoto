package com.setvect.bokslphoto.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.setvect.bokslphoto.test.photo.포토_TestCase;

/**
 * 전체 테스트
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ 포토_TestCase.class })
public class BokslPhotoAllTests {
}
