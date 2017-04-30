package com.setvect.bokslphoto;

import java.net.URL;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BokslPhotoApplication {
	private static final String CONFIG_CONFIG_PROPERTIES = "/application.properties";

	public static void main(String[] args) {
		SpringApplication.run(BokslPhotoApplication.class, args);
	}

	@Bean
	InitializingBean init() {
		return () -> {
			URL configUrl = BokslPhotoApplication.class.getResource(CONFIG_CONFIG_PROPERTIES);
			EnvirmentProperty.init(configUrl);
		};
	}
}
