package com.setvect.bokslphoto;

import java.net.URL;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

/**
 * Spring boot application 시작점.
 */
@SpringBootApplication
public class BokslPhotoApplication extends SpringBootServletInitializer {
	/** 설정 파일 경로. */
	private static final String CONFIG_CONFIG_PROPERTIES = "/application.properties";

	/**
	 * Application 시작점.
	 *
	 * @param args
	 *            사용 안함
	 */
	public static void main(final String[] args) {
		// spring boot에서 클래스가 및 properties 변경되었을 때 restart 안됨.
		// 즉 reload 효과
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(BokslPhotoApplication.class, args);
	}

	@Override
	protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
		return application.sources(BokslPhotoApplication.class);
	}

	/**
	 * 서비스 시작점 초기화.
	 *
	 * @return Spring boot 시작 bean
	 */
	@Bean
	InitializingBean init() {
		return () -> {
			URL configUrl = BokslPhotoApplication.class.getResource(CONFIG_CONFIG_PROPERTIES);
			EnvirmentProperty.init(configUrl);
		};
	}

	/**
	 * @return JSP 변경 체크 여부 설정
	 */
	@Bean
	public EmbeddedServletContainerCustomizer servletContainerCustomizer() {
		return new EmbeddedServletContainerCustomizer() {
			@Override
			public void customize(final ConfigurableEmbeddedServletContainer container) {
				if (container instanceof TomcatEmbeddedServletContainerFactory) {
					customizeTomcat((TomcatEmbeddedServletContainerFactory) container);
				}
			}

			private void customizeTomcat(final TomcatEmbeddedServletContainerFactory tomcatFactory) {
				tomcatFactory.addContextCustomizers(new TomcatContextCustomizer() {

					@Override
					public void customize(final Context context) {
						Container jsp = context.findChild("jsp");
						if (jsp instanceof Wrapper) {
							// true면 변경 체크함, false 안함.
							((Wrapper) jsp).addInitParameter("development", "true");
						}
					}
				});
			}
		};
	}
}
