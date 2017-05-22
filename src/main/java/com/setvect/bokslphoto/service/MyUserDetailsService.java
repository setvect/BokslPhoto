package com.setvect.bokslphoto.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.vo.UserVo;

/**
 * Spring security에 사용할 사용자 데이터를 조회 하는 ㄴ서비스
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

	/** 사용자 데이터 조회용 */
	@Autowired
	private UserRepository userDao;

	@Override
	public UserDetails loadUserByUsername(final String username) {
		UserVo user = userDao.findOne(username);
		if (user == null) {
			throw new UsernameNotFoundException(username);
		}
		List<GrantedAuthority> authorities = ApplicationUtil.buildUserAuthority(user.getUserRole());

		User userDetail = buildUserForAuthentication(user, authorities);
		return userDetail;
	}

	/**
	 * @param user
	 *            로그인 사용자
	 * @param authorities
	 *            권한 정보
	 * @return spring security에 적용할 사용자 인스턴스
	 */
	private static User buildUserForAuthentication(final UserVo user, final List<GrantedAuthority> authorities) {
		return new User(user.getUserId(), user.getPassword(), !user.isDeleteF(), true, true, true, authorities);
	}
}