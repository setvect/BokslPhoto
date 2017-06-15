package com.setvect.bokslphoto.test.photo;

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
import com.setvect.bokslphoto.service.PhotoSearchParam;
import com.setvect.bokslphoto.test.MainTestBase;
import com.setvect.bokslphoto.util.DateUtil;
import com.setvect.bokslphoto.util.GenericPage;
import com.setvect.bokslphoto.util.TreeNode;
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoDirectory;
import com.setvect.bokslphoto.vo.PhotoVo;
import com.setvect.bokslphoto.vo.UserVo;

public class PhotoControllerTestCase extends MainTestBase {
	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private MockHttpSession session;

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

		Type type = new TypeToken<List<GroupByDate>>() {
			/** */
			private static final long serialVersionUID = 8349948434510094988L;
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
		Type confType = new TypeToken<GenericPage<PhotoVo>>() {
			/** */
			private static final long serialVersionUID = 8598639125420936733L;
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

		Type confType = new TypeToken<TreeNode<PhotoDirectory>>() {
			/** */
			private static final long serialVersionUID = 8349948434510094988L;
		}.getType();

		Gson gson = new Gson();
		TreeNode<PhotoDirectory> response = gson.fromJson(jsonDirectory, confType);
		Assert.assertThat(response.getData().getName(), CoreMatchers.is(""));
		Assert.assertThat(response.getChildren().size(), CoreMatchers.is(3));
		TreeNode<PhotoDirectory> treeNode = response.getChildren().get(0);
		Assert.assertThat(treeNode.getData().getName(), CoreMatchers.is("여행"));
		Assert.assertThat(treeNode.getChildren().size(), CoreMatchers.is(2));
	}

	@Test
	public void testGetFolder() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/folder.json"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		MvcResult mvcResult = resultActions.andReturn();
		String jsonFolder = mvcResult.getResponse().getContentAsString();

		Type confType = new TypeToken<TreeNode<FolderVo>>() {
			/** */
			private static final long serialVersionUID = 8349948434510094988L;
		}.getType();

		Gson gson = new Gson();
		TreeNode<FolderVo> response = gson.fromJson(jsonFolder, confType);
		Assert.assertThat(response.getData().getName(), CoreMatchers.is("ROOT"));
		Assert.assertThat(response.getChildren().get(0).getData().getName(), CoreMatchers.is("SUB1"));
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

		// 2. 연관 폴더 등록
		callRequest = MockMvcRequestBuilders.get("/photo/addRelationFolder.do");
		callRequest.param("photoId", photoId);
		callRequest.param("folderSeq", String.valueOf(folderList.get(1).getFolderSeq()));
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);
		System.out.println(photo);

		Assert.assertThat(photo.getFolders().size(), CoreMatchers.is(2));

		FolderVo folder = folderRepository.findOne(folderSeq);
		List<PhotoVo> s = folder.getPhotos();
		System.out.println(s);

		// 3. 폴더 연결 삭제
		callRequest = MockMvcRequestBuilders.get("/photo/deleteRelationFolder.do");
		callRequest.param("photoId", photoId);
		callRequest.param("folderSeq", String.valueOf(folderList.get(1).getFolderSeq()));
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		photo = photoRepository.findOne(photoId);
		System.out.println(photo);

		Assert.assertThat(photo.getFolders().size(), CoreMatchers.is(1));

		folder = folderRepository.findOne(folderSeq);
		s = folder.getPhotos();
		System.out.println(s);
		// TODO Mapping된 값을 가져오지 못함.
		// Assert.assertThat(folder.getPhotos().get(0).getPhotoId(),
		// CoreMatchers.is(photoId));

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

		byte[] receive = getImageByte(photoId);
		Assert.assertArrayEquals(new byte[0], receive);

		receive = getImageOrgByte(photoId);
		Assert.assertArrayEquals(new byte[0], receive);

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

		Gson gson = new Gson();
		List<String> result = gson.fromJson(jsonDirectory, new TypeToken<List<String>>() {
			/** */
			private static final long serialVersionUID = 1L;
		}.getType());
		Assert.assertThat(result.size(), CoreMatchers.is(1));

		// 두 번째 - 중복 데이터 나오면 안됨.
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/photo/deleteDuplicate.json"));
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andDo(MockMvcResultHandlers.print());
		mvcResult = resultActions.andReturn();
		jsonDirectory = mvcResult.getResponse().getContentAsString();
		System.out.println(jsonDirectory);

		result = gson.fromJson(jsonDirectory, new TypeToken<List<String>>() {
			/** */
			private static final long serialVersionUID = 1L;
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
		Assert.assertNotNull(securitySessionValue);
		UserVo user = (UserVo) securitySessionValue.getAuthentication().getPrincipal();
		System.out.println(securitySessionValue.getAuthentication().getName());
		Assert.assertThat(user.getName(), CoreMatchers.is("관리자"));

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

}
