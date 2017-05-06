package com.setvect.bokslphoto.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.PhotoVo;

/**
 * 사진 검색 조건
 */
public interface PhotoRepositoryCustom {

	/**
	 * 사진 목록
	 */
	public GenericPage<PhotoVo> getPhotoPagingList(PhotoSearchParam pageCondition);

	/**
	 * 디렉토리 정보
	 */
	public Map<String, Integer> getPhotoDirectoryList();

	/**
	 * 날짜별 사진 건수
	 *
	 * @param pageCondition
	 * @return
	 */
	public List<ImmutablePair<Date, Integer>> getGroupShotDate();

}
