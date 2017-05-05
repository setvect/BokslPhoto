package com.setvect.bokslphoto.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import com.setvect.bokslphoto.BokslPhotoConstant;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		PasswordEncoder passwordEncoder = passwordEncoder();
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/static/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()//
				.antMatchers("/photo/**").access("hasRole('ROLE_ADMIN')")//
				// 아래 코드 넣으면 로그인 시 에러 남. 그 이후에 에러 안남.
				// .anyRequest().authenticated()//
				.and().formLogin().loginPage("/login.do").permitAll().failureUrl("/login.do?error")//
				.and().logout().logoutUrl("/logout.do").permitAll().logoutSuccessUrl("/login.do?logout")//
				.and().csrf()//
				.and().exceptionHandling().accessDeniedPage("/403");

		http.csrf().disable();
		http.headers().frameOptions().disable();
		http.rememberMe().key(BokslPhotoConstant.Login.REMEMBER_ME_KEY)
				.rememberMeServices(tokenBasedRememberMeServices());
	}

	@Bean
	public TokenBasedRememberMeServices tokenBasedRememberMeServices() {
		TokenBasedRememberMeServices tokenBasedRememberMeServices = new TokenBasedRememberMeServices(
				BokslPhotoConstant.Login.REMEMBER_ME_KEY, userDetailsService);
		tokenBasedRememberMeServices.setCookieName(BokslPhotoConstant.Login.REMEMBER_COOKIE_NAME);
		return tokenBasedRememberMeServices;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}

}