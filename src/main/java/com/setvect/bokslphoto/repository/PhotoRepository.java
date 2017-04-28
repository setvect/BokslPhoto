package com.setvect.bokslphoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.setvect.bokslphoto.vo.PhotoVo;

public interface PhotoRepository extends JpaRepository<PhotoVo, Integer>, PhotoRepositoryCustom {
}
