package com.setvect.bokslphoto;

import javax.transaction.Transactional;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import com.setvect.bokslphoto.MainTestBase.TestConfiguration;

public class MainTestBase {

	@PropertySource("classpath:application.properties")
	@Configuration
	public static class TestConfiguration {
	}
}
