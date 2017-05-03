package com.setvect.bokslphoto.test;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.setvect.bokslphoto.BokslPhotoApplication;
import com.setvect.bokslphoto.test.MainTestBase.TestConfiguration;

@RunWith(SpringRunner.class)
@Transactional()
@AutoConfigureTestDatabase(replace = Replace.NONE)
@SpringBootTest(classes = { BokslPhotoApplication.class, TestConfiguration.class })
@Rollback(true)
public class MainTestBase {

	@PropertySource("classpath:application.properties")
	@Configuration
	public static class TestConfiguration {
	}
}
