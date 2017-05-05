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

@Service
public class MyUserDetailsService implements UserDetailsService {

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

	private static User buildUserForAuthentication(UserVo user, List<GrantedAuthority> authorities) {
		return new User(user.getUserId(), user.getPassword(), !user.isDeleteF(), true, true, true, authorities);
	}
}