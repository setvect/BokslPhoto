package com.setvect.bokslphoto.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.setvect.bokslphoto.repository.UserRepository;
import com.setvect.bokslphoto.vo.UserRoleVo;
import com.setvect.bokslphoto.vo.UserVo;

@Service
public class MyUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userDao;

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		UserVo user = userDao.findOne(username);
		List<GrantedAuthority> authorities = buildUserAuthority(user.getUserRole());

		User userDetail = buildUserForAuthentication(user, authorities);
		return userDetail;
	}

	private User buildUserForAuthentication(UserVo user, List<GrantedAuthority> authorities) {
		return new User(user.getUserId(), user.getPassword(), user.isDeleteF(), true, true, true, authorities);
	}

	private List<GrantedAuthority> buildUserAuthority(Set<UserRoleVo> userRoles) {
		List<GrantedAuthority> authList = userRoles.stream().map(x -> new SimpleGrantedAuthority(x.getRole()))
				.collect(Collectors.toList());
		return authList;
	}

}