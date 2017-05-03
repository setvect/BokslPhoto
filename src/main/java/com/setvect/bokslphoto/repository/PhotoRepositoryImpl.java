package com.setvect.bokslphoto.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.PhotoVo;

/**
 * 사진 검색 조건
 */
public class PhotoRepositoryImpl implements PhotoRepositoryCustom {
	@PersistenceContext
	private EntityManager em;

	@Override
	public GenericPage<PhotoVo> getPhotoPagingList(PhotoSearchParam pageCondition) {
		String queryStatement = "select count(*) from PhotoVo p " + BokslPhotoConstant.SQL_WHERE;
		Query queryCount = makeQueryWithWhere(pageCondition, queryStatement);
		int totalCount = ((Long) queryCount.getSingleResult()).intValue();

		queryStatement = "select p from PhotoVo p " + BokslPhotoConstant.SQL_WHERE + " order by p.shotDate desc";

		Query querySelect = makeQueryWithWhere(pageCondition, queryStatement);
		querySelect.setFirstResult(pageCondition.getStartCursor());
		querySelect.setMaxResults(pageCondition.getReturnCount());

		@SuppressWarnings("unchecked")
		List<PhotoVo> resultList = querySelect.getResultList();
		GenericPage<PhotoVo> resultPage = new GenericPage<PhotoVo>(resultList, pageCondition.getStartCursor(),
				totalCount);

		return resultPage;
	}

	private Query makeQueryWithWhere(PhotoSearchParam pageCondition, String queryStatement) {
		String where = "";
		if (pageCondition.isDateBetween()) {
			where = " where p.shotDate between :from and :to ";
		}
		String queryString = queryStatement.replace(BokslPhotoConstant.SQL_WHERE, where);
		Query query = em.createQuery(queryString);
		if (pageCondition.isDateBetween()) {
			query.setParameter("from", pageCondition.getSearchFrom());
			query.setParameter("to", pageCondition.getSearchTo());
		}
		return query;
	}

}