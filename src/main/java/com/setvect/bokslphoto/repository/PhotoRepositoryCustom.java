package com.setvect.bokslphoto.repository;

import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.PhotoVo;

/**
 * 코멘트
 *
 * @version $Id$
 */
public interface PhotoRepositoryCustom {

	/**
	 */
	public GenericPage<PhotoVo> getPhotoPagingList(PhotoSearchParam pageCondition);

}
