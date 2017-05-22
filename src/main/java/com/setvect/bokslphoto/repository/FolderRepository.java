package com.setvect.bokslphoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.setvect.bokslphoto.vo.FolderVo;

/**
 * 폴더 Repository
 */
public interface FolderRepository extends JpaRepository<FolderVo, Integer> {
}
