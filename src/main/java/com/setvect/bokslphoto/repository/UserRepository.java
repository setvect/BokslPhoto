package com.setvect.bokslphoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.setvect.bokslphoto.vo.UserVo;

public interface UserRepository extends JpaRepository<UserVo, String> {

}