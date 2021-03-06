package com.setvect.bokslphoto.test.photo;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.setvect.bokslphoto.BokslPhotoConstant;
import com.setvect.bokslphoto.controller.GroupByDate;
import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.service.DateGroup;
import com.setvect.bokslphoto.service.FolderAddtion;
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.service.PhotoService;
import com.setvect.bokslphoto.test.MainTestBase;
import com.setvect.bokslphoto.util.DateUtil;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.util.TreeNode;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoDirectory;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.UserVo;
import com.setvect.bokslphoto.vo.PhotoVo.ShotDateType;

public class PhotoControllerTestCase extends MainTestBase {
	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private PhotoService photoService;

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private MockHttpSession session;

	/** 개체 refresh 하기위해 */
	@Autowired
	private EntityManager entityManager;

	/**
	 * 이미지 탐색 후 저장. <br>
	 * 저장될 결과 조회
	 *
	 * @throws IOException
	 *             파일 복사 실패
	 */
	@Before
	public void init() throws IOException {
		insertInitValue();
		System.out.println("끝. ====================");
	}

	// ============== 데이터 조회 ==============
	@Test
	public void testGroupByDate() throws Exception {
		PhotoSearchParam param = new PhotoSearchParam();
		param.setSearchDateGroup(DateGroup.DATE);
		List<GroupByDate> r = listGroupByDate(param);

		Assert.assertThat(r.size(), CoreMatchers.is(13));
		r.stream().forEach(System.out::println);

		// 최근 날짜가 먼저 나오게
		Assert.assertTrue(r.get(1).getFrom().getTime() > r.get(2).getFrom().getTime());

		param = new PhotoSearchParam();
		param.setSearchDateGroup(DateGroup.MONTH);
		r = listGroupByDate(param);

		Assert.assertThat(r.size(), CoreMatchers.is(12));
		r.stream().forEach(System.out::println);
		// 최근 날짜가 먼저 나오는것 확인
		Assert.assertTrue(r.get(5).getTo().getTime() > r.get(6).getTo().getTime());

		// 디렉토리 이름 검색
		param = new PhotoSearchParam();
		param.setSearchDateGroup(DateGroup.DATE);
		param.setSearchDirectory("/여행/");
		r = listGroupByDate(param);
		Assert.assertThat(r.size(), CoreMatchers.is(1));

		// 폴더 검색
		List<FolderVo> folder = folderRepository.findAll();
		Assert.assertThat(4, CoreMatchers.is(folder.size()));

		param = new PhotoSearchParam();
		param.setSearchDateGroup(DateGroup.DATE);
		param.setSearchFolderSeq(folder.get(1).getFolderSeq());
		r = listGroupByDate(param);
		r.stream().forEach(System.out::println);
		Assert.assertThat(r.size(), CoreMatchers.is(2));

		// 메모 검색
		param = new PhotoSearchParam();
		param.setSearchDateGroup(DateGroup.DATE);
		param.setSearchMemo("메모");
		r = listGroupByDate(param);
		r.stream().forEach(System.out::println);
		Assert.assertThat(r.size(), CoreMatchers.is(2));

		// 메모 검색
		param = new PhotoSearchParam();
		param.setSearchDateGroup(DateGroup.DATE);
		param.setSearchMemo("테스트");
		r = listGroupByDate(param);
		r.stream().forEach(System.out::println);
		Assert.assertThat(r.size(), CoreMatchers.is(1));

		System.out.println("끝.");
	}

	private List<GroupByDate> listGroupByDate(PhotoSearchParam param) throws Exception, UnsupportedEncodingException {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/groupByDate.json");

		callRequest.param("searchDateGroup", param.getSearchDateGroup().name());

		if (param.getSearchMemo() != null) {
			callRequest.param("searchMemo", param.getSearchMemo());
		}
		if (param.getSearchDirectory() != null) {
			callRequest.param("searchDirectory", param.getSearchDirectory());
		}

		if (param.isDateBetween()) {
			callRequest.param("searchFrom", DateUtil.getFormatString(param.getSearchFrom(), "yyyyMMdd"));
			callRequest.param("searchTo", DateUtil.getFormatString(param.getSearchTo(), "yyyyMMdd"));
		}

		if (param.getSearchFolderSeq() != 0) {
			callRequest.param("searchFolderSeq", String.valueOf(param.getSearchFolderSeq()));
		}

		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		MvcResult mvcResult = resultActions.andReturn();
		String content = mvcResult.getResponse().getContentAsString();
		System.out.println(content);

		@SuppressWarnings("serial")
		Type type = new TypeToken<List<GroupByDate>>() {
		}.getType();

		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class,
				(JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
				.create();
		List<GroupByDate> r = gson.fromJson(content, type);
		return r;
	}

	@Test
	public void testList() throws Exception {
		List<PhotoVo> allImage = photoRepository.findAll();

		// 1. 전체 조회
		PhotoSearchParam param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);

		GenericPage<PhotoVo> response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(allImage.size()));
		Assert.assertThat(response.getList().size(), CoreMatchers.is(param.getReturnCount()));

		// 1-1. 페이지 이동
		param = new PhotoSearchParam();
		param.setStartCursor(10);
		param.setReturnCount(10);

		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(allImage.size()));
		Assert.assertThat(response.getList().size(), CoreMatchers.is(allImage.size() - param.getStartCursor()));

		// 2. 메모 검색
		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchMemo("메모");
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(2));

		// 3. 디렉토리 검색
		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchDirectory("/음식/");
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(3));

		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchDirectory("/여행/바다/");
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(7));

		// 4. 날짜 검색
		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchFrom(DateUtil.getDate("2012-01-01"));
		param.setSearchTo(DateUtil.getDate("2015-01-01"));
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(4));

		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchFrom(DateUtil.getDate("2017-05-01"));
		param.setSearchTo(DateUtil.getDate("2017-05-01"));
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(1));

		int countOfhasShotDate = (int) allImage.stream().filter(p -> p.getShotDate() != null).count();
		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchFrom(DateUtil.getDate("1970-01-01"));
		param.setSearchTo(DateUtil.getDate("2100-01-01"));
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(countOfhasShotDate));

		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchDateNoting(true);
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(4));

		// 5. 폴더 검색
		List<FolderVo> list = folderRepository.findAll();
		FolderVo folder1 = list.get(1);
		FolderVo folder2 = list.get(2);

		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchFolderSeq(folder1.getFolderSeq());
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(2));

		List<PhotoVo> allList = photoRepository.findAll();
		allList.get(0).addFolder(folder1);
		allList.get(1).addFolder(folder1);
		allList.get(1).addFolder(folder2);
		photoRepository.save(allList.get(0));
		photoRepository.save(allList.get(1));

		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchFolderSeq(folder1.getFolderSeq());
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(4));
		Assert.assertThat(response.getList().size(), CoreMatchers.is(4));

		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(10);
		param.setSearchFolderSeq(folder2.getFolderSeq());
		response = listPhoto(param);
		Assert.assertThat(response.getTotalCount(), CoreMatchers.is(1));
		Assert.assertThat(response.getList().size(), CoreMatchers.is(1));
	}

	private GenericPage<PhotoVo> listPhoto(PhotoSearchParam param) throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/list.json");
		callRequest.param("startCursor", String.valueOf(param.getStartCursor()));
		callRequest.param("returnCount", String.valueOf(param.getReturnCount()));

		callRequest.param("searchDateNoting", String.valueOf(param.isSearchDateNoting()));
		if (param.getSearchMemo() != null) {
			callRequest.param("searchMemo", param.getSearchMemo());
		}
		if (param.getSearchDirectory() != null) {
			callRequest.param("searchDirectory", param.getSearchDirectory());
		}

		if (param.isDateBetween()) {
			callRequest.param("searchFrom", DateUtil.getFormatString(param.getSearchFrom(), "yyyyMMdd"));
			callRequest.param("searchTo", DateUtil.getFormatString(param.getSearchTo(), "yyyyMMdd"));
		}

		if (param.getSearchFolderSeq() != 0) {
			callRequest.param("searchFolderSeq", String.valueOf(param.getSearchFolderSeq()));
		}

		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andDo(MockMvcResultHandlers.print());
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		MvcResult mvcResult = resultActions.andReturn();

		String jsonList = mvcResult.getResponse().getContentAsString();

		return unmarshallingPhotoPage(jsonList);
	}

	/**
	 * json를 받아 이미지 목록 객체로 변환
	 *
	 * @param jsonList
	 * @return
	 */
	private GenericPage<PhotoVo> unmarshallingPhotoPage(String jsonList) {
		@SuppressWarnings("serial")
		Type confType = new TypeToken<GenericPage<PhotoVo>>() {
		}.getType();

		// 날짜 파싱. long -> Date
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(final JsonElement json, final Type typeOfT,
					final JsonDeserializationContext context) throws JsonParseException {
				long timestamp = json.getAsJsonPrimitive().getAsLong();
				return new Date(timestamp);
			}
		});

		Gson gson = builder.create();
		GenericPage<PhotoVo> response = gson.fromJson(jsonList, confType);
		return response;
	}

	/**
	 * 디렉토리 정보
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetDiretory() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/directory.json"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		MvcResult mvcResult = resultActions.andReturn();
		String jsonDirectory = mvcResult.getResponse().getContentAsString();
		System.out.println(jsonDirectory);

		@SuppressWarnings("serial")
		Type confType = new TypeToken<TreeNode<PhotoDirectory>>() {
		}.getType();

		Gson gson = new Gson();
		TreeNode<PhotoDirectory> response = gson.fromJson(jsonDirectory, confType);
		Assert.assertThat(response.getData().getName(), CoreMatchers.is(""));
		Assert.assertThat(response.getChildren().size(), CoreMatchers.is(3));
		TreeNode<PhotoDirectory> treeNode = response.getChildren().get(0);
		Assert.assertThat(treeNode.getData().getName(), CoreMatchers.is("여행"));
		Assert.assertThat(treeNode.getChildren().size(), CoreMatchers.is(2));
	}

	/**
	 * 분류 폴더관련 테스트(조회, 추가, 수정, 삭제)
	 *
	 * @throws Exception
	 */
	@Test
	public void testFolder() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/folder.json"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		MvcResult mvcResult = resultActions.andReturn();
		String jsonFolder = mvcResult.getResponse().getContentAsString();

		@SuppressWarnings("serial")
		Type confType = new TypeToken<TreeNode<FolderVo>>() {
		}.getType();

		Gson gson = new Gson();
		TreeNode<FolderVo> response = gson.fromJson(jsonFolder, confType);
		Assert.assertThat(response.getData().getName(), CoreMatchers.is("ROOT"));
		Assert.assertThat(response.getChildren().get(0).getData().getName(), CoreMatchers.is("SUB1"));

		List<TreeNode<FolderVo>> l = response.exploreTree();

		Assert.assertThat(l.size(), CoreMatchers.is(4));
		int folderSeq = l.get(2).getData().getFolderSeq();

		List<FolderVo> pathResponse = getFolderPath(folderSeq, true);
		Assert.assertThat(pathResponse.size(), CoreMatchers.is(2));
		Assert.assertThat(pathResponse.get(0).getName(), CoreMatchers.is("ROOT"));
		Assert.assertThat(pathResponse.get(1).getName(), CoreMatchers.is("SUB2"));

		pathResponse = getFolderPath(folderSeq, false);
		Assert.assertThat(pathResponse.size(), CoreMatchers.is(1));
		Assert.assertThat(pathResponse.get(0).getName(), CoreMatchers.is("SUB2"));

		List<FolderVo> allFolder = folderRepository.findAll();

		FolderVo baseFolder = allFolder.get(1);

		// 폴더 추가
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/addFolder.do");
		callRequest.param("parentId", String.valueOf(baseFolder.getFolderSeq()));
		callRequest.param("name", String.valueOf("추가"));
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();

		// 폴더 하위에 입력되는지 검사
		List<FolderVo> allAfter = folderRepository.findAll();
		Assert.assertThat(allAfter.size(), CoreMatchers.is(allFolder.size() + 1));

		TreeNode<FolderVo> folderTree = photoService.getFolderTree();
		TreeNode<FolderVo> a = folderTree.getTreeNode(baseFolder);
		List<TreeNode<FolderVo>> b = a.getChildren();

		List<String> c = b.stream().map(t -> t.getData().getName()).collect(Collectors.toList());
		Assert.assertThat(c, hasItem("추가"));

		// 폴더 수정
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/updateFolder.do");
		callRequest.param("folderSeq", String.valueOf(baseFolder.getFolderSeq()));
		callRequest.param("parentId", String.valueOf(baseFolder.getParentId()));
		callRequest.param("name", String.valueOf("수정했음"));

		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();

		FolderVo f = folderRepository.findOne(baseFolder.getFolderSeq());
		Assert.assertThat(f.getName(), CoreMatchers.is("수정했음"));
		Assert.assertThat(f.getParentId(), CoreMatchers.is(baseFolder.getParentId()));

		// 루트 폴더 삭제
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/deleteFolder.do");
		Optional<FolderVo> rootFolder = allFolder.stream().filter(p -> p.getName().equals("ROOT")).findAny();
		callRequest.param("folderSeq", String.valueOf(rootFolder.get().getFolderSeq()));

		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_INTERNAL_SERVER_ERROR));

		// 서브 폴더 삭제
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/deleteFolder.do");
		Optional<FolderVo> subFolder = allFolder.stream().filter(p -> p.getName().equals("수정했음")).findAny();
		callRequest.param("folderSeq", String.valueOf(subFolder.get().getFolderSeq()));
		resultActions = mockMvc.perform(callRequest);

		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();

		allAfter = folderRepository.findAll();
		Assert.assertThat(allAfter.size(), CoreMatchers.is(3));
	}

	/**
	 * 폴더 목록 중 이미지 선택 여부 조회 테스트 케이스
	 *
	 * @throws Exception
	 */
	@Test
	public void testFolderAddInfo() throws Exception {
		// 분류 폴더가 있는 사진을 가져옴
		List<FolderVo> folderAll = folderRepository.findAll();
		Optional<FolderVo> folderOptional = folderAll.stream().filter(f -> f.getName().equals("SUB2-1")).findAny();
		FolderVo baseFolder = folderOptional.get();
		entityManager.refresh(baseFolder);

		String photoId = baseFolder.getPhotos().get(0).getPhotoId();

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/folderAddtionList.json");
		callRequest.param("photoId", photoId);
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		MvcResult mvcResult = resultActions.andReturn();
		String jsonFolder = mvcResult.getResponse().getContentAsString();

		@SuppressWarnings("serial")
		Type confType = new TypeToken<List<FolderAddtion>>() {
		}.getType();

		Gson gson = new Gson();
		List<FolderAddtion> response = gson.fromJson(jsonFolder, confType);

		System.out.println(response);
		// 최상위 루트 폴더 제외된 건수
		Assert.assertThat(response.size(), CoreMatchers.is(folderAll.size() - 1));

		long count = response.stream().filter(f -> f.isSelect())
				.filter(f -> f.getFolder().getFolderSeq() == baseFolder.getFolderSeq()).count();
		Assert.assertThat(count, CoreMatchers.is(1L));
	}

	private List<FolderVo> getFolderPath(int folderSeq, boolean includeRoot)
			throws Exception, UnsupportedEncodingException {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		ResultActions resultActions;
		MvcResult mvcResult;
		String jsonFolder;
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/folderPath.json");
		callRequest.param("folderSeq", String.valueOf(folderSeq));
		callRequest.param("includeRoot", String.valueOf(includeRoot));
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();
		jsonFolder = mvcResult.getResponse().getContentAsString();

		@SuppressWarnings("serial")
		Type listType = new TypeToken<List<FolderVo>>() {
		}.getType();

		Gson gson = new Gson();
		List<FolderVo> pathResponse = gson.fromJson(jsonFolder, listType);
		return pathResponse;
	}

	/**
	 * 이미지 url 스트림
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetImage() throws Exception {
		List<PhotoVo> allImage = photoRepository.findAll();
		PhotoVo targetPhoto = allImage.get(0);
		String photoId = targetPhoto.getPhotoId();
		byte[] receive = getImageByte(photoId);
		Assert.assertTrue(receive.length != 0);

		receive = getImageOrgByte(photoId);
		Assert.assertTrue(receive.length != 0);
		try (InputStream in = new FileInputStream(targetPhoto.getFullPath());) {
			byte[] origin = IOUtils.toByteArray(in);
			Assert.assertThat(receive, CoreMatchers.is(origin));
		}
	}

	private byte[] getImageByte(String photoId) throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/getImage.do");
		callRequest.param("photoId", photoId);
		callRequest.param("w", "100");
		callRequest.param("h", "100");
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		MvcResult mvcResult = resultActions.andReturn();
		byte[] receive = mvcResult.getResponse().getContentAsByteArray();
		return receive;
	}

	/**
	 * 원본 이미지
	 *
	 * @param photoId
	 * @return
	 * @throws Exception
	 */
	private byte[] getImageOrgByte(String photoId) throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/getImageOrg.do");
		callRequest.param("photoId", photoId);
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		MvcResult mvcResult = resultActions.andReturn();
		byte[] receive = mvcResult.getResponse().getContentAsByteArray();
		return receive;
	}

	/**
	 * 메타 정보 테스트
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetMeta() throws Exception {
		List<PhotoVo> allImage = photoRepository.findAll();
		PhotoVo targetPhoto = allImage.get(2);
		String photoId = targetPhoto.getPhotoId();

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/getMeta.json");
		callRequest.param("photoId", photoId);
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));

		MvcResult mvcResult = resultActions.andReturn();
		String jsonFolder = mvcResult.getResponse().getContentAsString();

		@SuppressWarnings("serial")
		Type confType = new TypeToken<Map<String, String>>() {
		}.getType();

		Gson gson = new Gson();
		Map<String, String> meta = gson.fromJson(jsonFolder, confType);

		meta.entrySet().stream().forEach(entry -> {
			System.out.printf("%s  :: %s\n", entry.getKey(), entry.getValue());
		});

		Assert.assertThat(meta.get("[Exif IFD0]Date/Time"), CoreMatchers.is("2016:05:01 11:42:12"));
		Assert.assertThat(meta.get("[Exif IFD0]Image Width"), CoreMatchers.is("1280 pixels"));
		Assert.assertThat(meta.get("[Exif IFD0]Focal Length"), CoreMatchers.is("4.4 mm"));
	}

	// ============== 데이터 등록 ==============
	/**
	 * 사진 등록
	 *
	 * @throws Exception
	 */
	@Test
	public void testUploadPhoto() throws Exception {
		// 파일 업로드
		File image = new File("./test_data/to_upload/a.jpg");
		InputStream input = new FileInputStream(image);
		byte[] imageByteData = IOUtils.toByteArray(input);
		MockMultipartFile imageFile = new MockMultipartFile("data", image.getName(), "image/jpg", imageByteData);
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		ResultActions resultActions = mockMvc
				.perform(MockMvcRequestBuilders.fileUpload("/photo/uploadProc.do").file(imageFile));
		resultActions.andDo(MockMvcResultHandlers.print());
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		// 같은 파일 업로드
		image = new File("./test_data/to_upload/a.jpg");
		input = new FileInputStream(image);
		imageByteData = IOUtils.toByteArray(input);
		imageFile = new MockMultipartFile("data", image.getName(), "image/jpg", imageByteData);
		resultActions = mockMvc.perform(MockMvcRequestBuilders.fileUpload("/photo/uploadProc.do").file(imageFile));
		resultActions.andDo(MockMvcResultHandlers.print());
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		// 비 이미지 파일 업로드
		image = new File("./test_data/to_upload/not_image.xml");
		input = new FileInputStream(image);
		imageByteData = IOUtils.toByteArray(input);

		imageFile = new MockMultipartFile("data", image.getName(), "image/jpg", imageByteData);
		resultActions = mockMvc.perform(MockMvcRequestBuilders.fileUpload("/photo/uploadProc.do").file(imageFile));
		resultActions.andDo(MockMvcResultHandlers.print());
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string(""));

		System.out.println("끝.");
	}

	/**
	 * 사진과 연결되 폴더 추가, 삭제
	 *
	 * @throws Exception
	 */
	@Test
	public void testRelationFolder() throws Exception {
		List<PhotoVo> photoList = photoRepository.findAll();
		System.out.println("PhotoCount: " + photoList.size());

		List<FolderVo> folderList = folderRepository.findAll();
		System.out.println("folderCount: " + folderList.size());

		// 1. 연관 폴더 등록
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/addRelationFolder.do");
		String photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		int folderSeq = folderList.get(0).getFolderSeq();
		callRequest.param("folderSeq", String.valueOf(folderSeq));
		ResultActions resultActions = mockMvc.perform(callRequest);
		// resultActions.andDo(MockMvcResultHandlers.print());
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));
		entityManager.refresh(photoList.get(0));
		Set<FolderVo> folderByPhoto = photoList.get(0).getFolders();
		Assert.assertThat(folderByPhoto.size(), CoreMatchers.is(1));
		Assert.assertThat(folderByPhoto.size(), CoreMatchers.is(1));

		// 2. 복수의 연관 폴더 등록
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/addRelationFolders.do");
		photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		callRequest.param("folderSeq", String.valueOf(folderList.get(0).getFolderSeq()),
				String.valueOf(folderList.get(1).getFolderSeq()));
		resultActions = mockMvc.perform(callRequest);
		// resultActions.andDo(MockMvcResultHandlers.print());
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));
		entityManager.refresh(photoList.get(0));
		folderByPhoto = photoList.get(0).getFolders();
		Assert.assertThat(folderByPhoto.size(), CoreMatchers.is(2));
		Set<Integer> folderSeqSet = folderByPhoto.stream().map(f -> f.getFolderSeq()).collect(toSet());
		Assert.assertTrue("폴더 포함 여부", folderSeqSet.contains(folderList.get(0).getFolderSeq()));
		Assert.assertTrue("폴더 포함 여부", folderSeqSet.contains(folderList.get(1).getFolderSeq()));

		// 3. 연관 폴더 등록
		callRequest = MockMvcRequestBuilders.get("/photo/addRelationFolder.do");
		callRequest.param("photoId", photoId);
		callRequest.param("folderSeq", String.valueOf(folderList.get(1).getFolderSeq()));
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);

		Assert.assertThat(photo.getFolders().size(), CoreMatchers.is(2));

		FolderVo folder = folderRepository.findOne(folderSeq);
		List<PhotoVo> s = folder.getPhotos();

		// 4. 폴더 연결 삭제
		callRequest = MockMvcRequestBuilders.get("/photo/deleteRelationFolder.do");
		callRequest.param("photoId", photoId);
		callRequest.param("folderSeq", String.valueOf(folderList.get(1).getFolderSeq()));
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		photo = photoRepository.findOne(photoId);

		Assert.assertThat(photo.getFolders().size(), CoreMatchers.is(1));

		folder = folderRepository.findOne(folderSeq);
		s = folder.getPhotos();

		Set<String> photoIdSet = s.stream().map(photo1 -> photo1.getPhotoId()).collect(toSet());
		Assert.assertTrue("폴더에 특정 이미지가 포함되어 있지는지", photoIdSet.contains(photoId));

		System.out.println("끝.");
	}

	/**
	 * 이미지 재탐색 후 저장
	 *
	 * @throws Exception
	 */
	@Test
	public void testRetrievalAndSave() throws Exception {
		List<PhotoVo> before = photoRepository.findAll();

		// 첫 번째
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/retrievalAndSave.do"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		resultActions.andExpect(content().string("true"));

		List<PhotoVo> after = photoRepository.findAll();

		Assert.assertThat(before.size(), CoreMatchers.is(after.size()));

		System.out.printf("before size: %,d, after size: %,d\n", before.size(), after.size());
	}

	// ============== 데이터 수정 ==============
	/**
	 * 메모 업데이트
	 *
	 * @throws Exception
	 */
	@Test
	public void testUploadMemo() throws Exception {
		String memo = "복슬이 사랑해~";
		List<PhotoVo> photoList = photoRepository.findAll();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/updateMemo.do");
		String photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		callRequest.param("memo", memo);
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);
		Assert.assertThat(photo.getMemo(), CoreMatchers.is(memo));
		System.out.println("끝.");
	}

	@Test
	public void testProtect() throws Exception {
		List<PhotoVo> photoList = photoRepository.findAll();

		// 전체 조회
		PhotoSearchParam param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(Integer.MAX_VALUE);
		GenericPage<PhotoVo> photoPage = listPhoto(param);
		photoPage.getList().forEach(p -> {
			Assert.assertFalse("보호 이미지 여부", p.isProtectF());
		});

		// 보호 이미지 셋팅은 IP와 상관 없이 처리
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/updateProtect.do");
		String photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		callRequest.param("protect", "true");
		callRequest.with((paramMockHttpServletRequest) -> {
			paramMockHttpServletRequest.setRemoteAddr("111.111.111.111");
			return paramMockHttpServletRequest;
		});
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);

		Assert.assertTrue("보호 이미지 여부", photo.isProtectF());

		// 전체 조회. 비허가 IP
		param = new PhotoSearchParam();
		param.setStartCursor(0);
		param.setReturnCount(Integer.MAX_VALUE);
		photoPage = listPhoto(param);
		Assert.assertThat(photoPage.getList().stream().filter(PhotoVo::isProtectF).count(), CoreMatchers.is(1L));
		Assert.assertThat(photoPage.getList().stream().filter(PhotoVo::isDeny).count(), CoreMatchers.is(1L));
		String checkId = photoId;
		photoPage.getList().stream().filter(PhotoVo::isDeny).findAny().ifPresent(p -> {
			Assert.assertThat(p.getPhotoId(), CoreMatchers.is(checkId));
			Assert.assertNull(p.getName());
		});

		byte[] protectImageByte;
		try (InputStream in = new FileInputStream(BokslPhotoConstant.Photo.PROTECT_IMAGE);) {
			protectImageByte = IOUtils.toByteArray(in);
		}

		byte[] receive = getImageByte(photoId);
		Assert.assertArrayEquals(receive, protectImageByte);

		receive = getImageOrgByte(photoId);
		Assert.assertArrayEquals(receive, protectImageByte);

		// 전체 조회. 허가 IP
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/list.json");
		callRequest.param("startCursor", String.valueOf(param.getStartCursor()));
		callRequest.param("returnCount", String.valueOf(param.getReturnCount()));
		callRequest.with((paramMockHttpServletRequest) -> {
			paramMockHttpServletRequest.setRemoteAddr(BokslPhotoConstant.Photo.ALLOW_IP);
			return paramMockHttpServletRequest;
		});
		resultActions = mockMvc.perform(callRequest);
		MvcResult mvcResult = resultActions.andReturn();

		String jsonList = mvcResult.getResponse().getContentAsString();
		photoPage = unmarshallingPhotoPage(jsonList);
		Assert.assertThat(photoPage.getList().stream().filter(PhotoVo::isProtectF).count(), CoreMatchers.is(1L));
		Assert.assertThat(photoPage.getList().stream().filter(PhotoVo::isDeny).count(), CoreMatchers.is(0L));

		// 비허가 IP
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/updateProtect.do");
		photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		callRequest.param("protect", "false");
		callRequest.with((paramMockHttpServletRequest) -> {
			paramMockHttpServletRequest.setRemoteAddr("111.111.111.111");
			return paramMockHttpServletRequest;
		});
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("false"));
		photo = photoRepository.findOne(photoId);
		Assert.assertTrue("보호 이미지 여부", photo.isProtectF());

		// 허가 IP 접근
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/updateProtect.do");
		photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		callRequest.param("protect", "false");
		callRequest.with((paramMockHttpServletRequest) -> {
			paramMockHttpServletRequest.setRemoteAddr(BokslPhotoConstant.Photo.ALLOW_IP);
			return paramMockHttpServletRequest;
		});
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));
		photo = photoRepository.findOne(photoId);
		Assert.assertFalse("보호 이미지 여부", photo.isProtectF());

		System.out.println("끝.");
	}

	/**
	 * 촬영 날짜 업데이트
	 *
	 * @throws Exception
	 */
	@Test
	public void testUploadShotDate() throws Exception {
		List<PhotoVo> photoList = photoRepository.findAll();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/updateShotDate.do");
		String photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		String updateDate = "20120101";
		callRequest.param("shotDate", updateDate);
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);
		Assert.assertThat(DateUtil.getFormatString(photo.getShotDate(), "yyyyMMdd"), CoreMatchers.is(updateDate));
		Assert.assertThat(photo.getShotDateType(), CoreMatchers.is(ShotDateType.MANUAL));
		System.out.println("끝.");
	}

	// ============== 데이터 삭제 ==============
	/**
	 * 포토 삭제
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeletePhoto() throws Exception {
		List<PhotoVo> photoList = photoRepository.findAll();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/deletePhoto.do");
		String photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);
		Assert.assertNull(photo);

		System.out.println("끝.");
	}

	/**
	 * 중복 데이터 삭제
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("serial")
	@Test
	public void testDeleteDuplicate() throws Exception {
		// 첫 번째
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/deleteDuplicate.json"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		MvcResult mvcResult = resultActions.andReturn();
		String jsonDirectory = mvcResult.getResponse().getContentAsString();
		System.out.println(jsonDirectory);

		// 이미지 업로드 되면서 중복파일 제거되 '0'이 나옴
		Gson gson = new Gson();
		List<String> result = gson.fromJson(jsonDirectory, new TypeToken<List<String>>() {
		}.getType());
		Assert.assertThat(result.size(), CoreMatchers.is(0));

		// 두 번째 - 중복 데이터 나오면 안됨.
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/deleteDuplicate.json"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();
		jsonDirectory = mvcResult.getResponse().getContentAsString();
		System.out.println(jsonDirectory);

		result = gson.fromJson(jsonDirectory, new TypeToken<List<String>>() {
		}.getType());
		Assert.assertThat(result.size(), CoreMatchers.is(0));
	}

	// ============== 기타 ==============
	/**
	 * DB 조작이 없는 단순 페이지 로딩
	 *
	 * @throws Exception
	 */
	@Test
	public void testLoadPage() throws Exception {
		// '/' 테스트
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/").session(session);
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_MOVED_TEMPORARILY));
		SecurityContextImpl securitySessionValue = (SecurityContextImpl) session
				.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		Assert.assertNull(securitySessionValue);
		// UserVo user = (UserVo)
		// securitySessionValue.getAuthentication().getPrincipal();
		// System.out.println(securitySessionValue.getAuthentication().getName());
		// Assert.assertThat(user.getName(), CoreMatchers.is("관리자"));

		// login.do 테스트
		resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/login.do"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		MvcResult mvcResult = resultActions.andReturn();
		String forwardUrl = mvcResult.getResponse().getForwardedUrl();
		Assert.assertThat(forwardUrl, CoreMatchers.containsString("login.jsp"));

		// /photo 테스트
		resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();
		forwardUrl = mvcResult.getResponse().getForwardedUrl();
		Assert.assertThat(forwardUrl, CoreMatchers.containsString("/photo.jsp"));

		// /photo/list.do 테스트
		resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/list.do"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();
		forwardUrl = mvcResult.getResponse().getForwardedUrl();
		Assert.assertThat(forwardUrl, CoreMatchers.containsString("/photo/photo_list.jsp"));

		// /photo/list.do 테스트
		resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/upload.do"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();
		forwardUrl = mvcResult.getResponse().getForwardedUrl();
		Assert.assertThat(forwardUrl, CoreMatchers.containsString("/photo/photo_upload.jsp"));
	}

	/**
	 * 에러 처리
	 *
	 * @throws Exception
	 */
	@Test
	public void testErrorProcessing() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		// login.do 테스트
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/no_page.do"));
		resultActions.andExpect(status().is(HttpStatus.SC_NOT_FOUND));
		resultActions.andDo(MockMvcResultHandlers.print());

		// 권한이 없는 상태에서 페이지 접근
		// TODO 원래 권한 없나도 나와야 되는데...
		resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/list.do"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
	}

	/**
	 * 접속 아이피가 보호 이미지 해제 할수 있는 여부 확인
	 *
	 * @throws Exception
	 */
	@Test
	public void testProtectPossible() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/isAllowAccessProtected.json");
		callRequest.with((paramMockHttpServletRequest) -> {
			paramMockHttpServletRequest.setRemoteAddr("111.111.111.111");
			return paramMockHttpServletRequest;
		});
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("false"));

		callRequest = MockMvcRequestBuilders.get("/photo/isAllowAccessProtected.json");
		callRequest.with((paramMockHttpServletRequest) -> {
			paramMockHttpServletRequest.setRemoteAddr(BokslPhotoConstant.Photo.ALLOW_IP);
			return paramMockHttpServletRequest;
		});
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));
	}
}
