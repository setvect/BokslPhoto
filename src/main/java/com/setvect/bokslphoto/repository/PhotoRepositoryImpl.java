package com.setvect.bokslphoto.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.PhotoVo;

/**
 * 코멘트
 */
public class PhotoRepositoryImpl implements PhotoRepositoryCustom {
	@PersistenceContext
	private EntityManager em;

	@Override
	public GenericPage<PhotoVo> getPhotoPagingList(PhotoSearchParam pageCondition) {
		String q = "select count(*) from PhotoVo c" + getWhereClause(pageCondition);
		Query query = em.createQuery(q);
		int totalCount = ((Long) query.getSingleResult()).intValue();

		// TODO 쿼리 만들기
		q = "select c from PhotoVo c " + getWhereClause(pageCondition) + " order by c.commentSeq desc";
		query = em.createQuery(q);
		query.setFirstResult(pageCondition.getStartCursor());
		query.setMaxResults(pageCondition.getReturnCount());

		@SuppressWarnings("unchecked")
		List<PhotoVo> resultList = query.getResultList();
		GenericPage<PhotoVo> resultPage = new GenericPage<PhotoVo>(resultList, pageCondition.getStartCursor(),
				totalCount);

		return resultPage;
	}

	private String getWhereClause(PhotoSearchParam pageCondition) {
		String where = " ";
		return where;
	}

}