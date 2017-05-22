package com.setvect.bokslphoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.setvect.bokslphoto.vo.UserVo;

/**
 * 사용자 Repository
 */
public interface UserRepository extends JpaRepository<UserVo, String> {

}