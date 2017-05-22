package com.setvect.bokslphoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.setvect.bokslphoto.vo.PhotoVo;

/**
 * 사진 Repository
 */
public interface PhotoRepository extends JpaRepository<PhotoVo, String>, PhotoRepositoryCustom {
}
