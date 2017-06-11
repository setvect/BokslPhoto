package com.setvect.bokslphoto.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.util.DateUtil;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.vo.PhotoVo;

/**
 * 사진 검색 조건
 */
public class PhotoRepositoryImpl implements PhotoRepositoryCustom {
	/** JPA DB 세션 */
	@PersistenceContext
	private EntityManager em;

	@Override
	public GenericPage<PhotoVo> getPhotoPagingList(final PhotoSearchParam pageCondition) {
		String queryStatement = "select count(DISTINCT p.photoId) FROM PhotoVo p LEFT OUTER JOIN p.folders f "
				+ BokslPhotoConstant.SQL_WHERE;
		Query queryCount = makeListQueryWhere(pageCondition, queryStatement);
		int totalCount = ((Long) queryCount.getSingleResult()).intValue();

		queryStatement = "SELECT DISTINCT p FROM PhotoVo p LEFT OUTER JOIN p.folders f " + BokslPhotoConstant.SQL_WHERE
				+ " ORDER BY p.shotDate DESC";

		Query querySelect = makeListQueryWhere(pageCondition, queryStatement);
		querySelect.setFirstResult(pageCondition.getStartCursor());
		querySelect.setMaxResults(pageCondition.getReturnCount());

		@SuppressWarnings("unchecked")
		List<PhotoVo> resultList = querySelect.getResultList();
		GenericPage<PhotoVo> resultPage = new GenericPage<PhotoVo>(resultList, pageCondition.getStartCursor(),
				totalCount);

		return resultPage;
	}

	@Override
	public Map<String, Integer> getPhotoDirectoryList() {
		String queryStatement = "SELECT p.directory, count(*) FROM PhotoVo p GROUP BY p.directory ORDER BY 1";
		Query querySelect = em.createQuery(queryStatement);

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = querySelect.getResultList();

		Map<String, Integer> result = resultList.stream().collect(Collectors.toMap(p -> {
			Object[] v = p;
			return (String) v[0];
		}, p -> {
			Object[] v = p;
			return ((Long) v[1]).intValue();
		}, (v1, v2) -> v1, TreeMap::new));

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ImmutablePair<Date, Integer>> getGroupShotDate(PhotoSearchParam condition) {
		// H2 Database 의존 쿼리
		String queryStatement = "SELECT to_char(p.SHOT_DATE, 'YYYYMMDD') as DATE_STRING, COUNT(*) " //
				+ " FROM TBBA_PHOTO P LEFT OUTER JOIN TBBC_MAPPING  F ON P.PHOTO_ID = F.PHOTO_ID ";
		queryStatement += " WHERE  1 = 1 ";

		Map<String, Object> bindMap = new HashMap<>();

		if (StringUtils.isNotEmpty(condition.getSearchDirectory())) {
			queryStatement += " AND DIRECTORY = :directory";
			bindMap.put("directory", condition.getSearchDirectory());
		}
		if (StringUtils.isNotEmpty(condition.getSearchMemo())) {
			queryStatement += " AND MEMO like :memo";
			bindMap.put("memo", "%" + condition.getSearchMemo() + "%");
		}
		if (condition.getSearchFolderSeq() != 0) {
			queryStatement += " AND F.FOLDER_SEQ = :folderSeq";
			bindMap.put("folderSeq", condition.getSearchFolderSeq());
		}
		queryStatement += " GROUP BY DATE_STRING ORDER BY DATE_STRING DESC";
		Query querySelect = em.createNativeQuery(queryStatement);

		bindMap.entrySet().stream().forEach(entry -> {
			querySelect.setParameter(entry.getKey(), entry.getValue());
		});

		List<Object[]> resultList = querySelect.getResultList();

		List<ImmutablePair<Date, Integer>> result = resultList.stream().map(p -> {
			Object[] v = p;
			Date date = null;
			if (v[0] != null) {
				date = DateUtil.getDate((String) v[0], "yyyyMMdd");
			}
			Number right = (Number) v[1];
			@SuppressWarnings("rawtypes")
			ImmutablePair<Date, Integer> pair = new ImmutablePair(date, right.intValue());
			return pair;
		}).collect(Collectors.toList());
		return result;
	}

	/**
	 * @param pageCondition
	 *            검색 조건
	 * @param queryStatement
	 *            기본 쿼리
	 * @return Where조건이 포함된 질의
	 */
	private Query makeListQueryWhere(final PhotoSearchParam pageCondition, final String queryStatement) {
		String where = " WHERE 1=1 ";
		if (pageCondition.isSearchDateNoting()) {
			where += " AND p.shotDate IS NULL ";
		} else if (pageCondition.isDateBetween()) {
			where += " AND p.shotDate BETWEEN :from and :to ";
		}

		if (StringUtils.isNotEmpty(pageCondition.getSearchDirectory())) {
			where += " AND p.directory = :directory";
		}
		if (StringUtils.isNotEmpty(pageCondition.getSearchMemo())) {
			where += " AND p.memo like :memo";
		}
		if (pageCondition.getSearchFolderSeq() != 0) {
			where += " AND f.folderSeq = :folderSeq";
		}

		String queryString = queryStatement.replace(BokslPhotoConstant.SQL_WHERE, where);
		Query query = em.createQuery(queryString);
		if (!pageCondition.isSearchDateNoting() && pageCondition.isDateBetween()) {
			query.setParameter("from", pageCondition.getSearchFrom());
			query.setParameter("to", pageCondition.getSearchToEnd());
		}
		if (StringUtils.isNotEmpty(pageCondition.getSearchDirectory())) {
			query.setParameter("directory", pageCondition.getSearchDirectory());
		}
		if (StringUtils.isNotEmpty(pageCondition.getSearchMemo())) {
			query.setParameter("memo", "%" + pageCondition.getSearchMemo() + "%");
		}
		if (pageCondition.getSearchFolderSeq() != 0) {
			query.setParameter("folderSeq", pageCondition.getSearchFolderSeq());
		}

		return query;
	}

}