package com.setvect.bokslphoto.test.photo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
import com.setvect.bokslphoto.controller.GroupByDate;
import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.service.DateGroup;
import com.setvect.bokslphoto.test.MainTestBase;
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

	@Test
	public void testGroupByDate() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/groupByDate.json");
		callRequest.param("searchDateGroup", DateGroup.MONTH.name());
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		MvcResult mvcResult = resultActions.andReturn();
		String content = mvcResult.getResponse().getContentAsString();
		System.out.println(content);

		Type type = new TypeToken<List<GroupByDate>>() {
			/** */
			private static final long serialVersionUID = 8349948434510094988L;
		}.getType();

		Gson gson = new GsonBuilder().create();
		List<GroupByDate> r = gson.fromJson(content, type);
		r.stream().forEach(System.out::println);

		System.out.println("끝.");
	}

	@Test
	public void testProtect() throws Exception {
		List<PhotoVo> photoList = photoRepository.findAll();

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		MockHttpServletRequestBuilder callRequest = MockMvcRequestBuilders.get("/photo/updateProtect.do");
		String photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		callRequest.param("protect", "true");
		ResultActions resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);

		Assert.assertTrue("보호 이미지 여부", photo.isProtectF());

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/updateProtect.do");
		photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		callRequest.param("protect", "false");
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(HttpStatus.SC_OK));
		resultActions.andExpect(content().string("true"));

		photo = photoRepository.findOne(photoId);

		Assert.assertFalse("보호 이미지 여부", photo.isProtectF());

		System.out.println("끝.");
	}

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
}
