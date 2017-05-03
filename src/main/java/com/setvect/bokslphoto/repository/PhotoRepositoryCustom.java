package com.setvect.bokslphoto.repository;

import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.PhotoVo;

/**
 * 사진 검색 조건
 */
public interface PhotoRepositoryCustom {

	/**
	 */
	public GenericPage<PhotoVo> getPhotoPagingList(PhotoSearchParam pageCondition);

}
