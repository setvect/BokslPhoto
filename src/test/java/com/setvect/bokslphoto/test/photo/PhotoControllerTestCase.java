package com.setvect.bokslphoto.test.photo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
import com.setvect.bokslphoto.vo.FolderVo;
import com.setvect.bokslphoto.vo.PhotoVo;

public class PhotoControllerTestCase extends MainTestBase {
	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private FolderRepository folderRepository;

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

	@Test
	public void testUploadPhoto() throws Exception {
		File image = new File("./test_data/to_upload/a.jpg");
		InputStream input = new FileInputStream(image);
		byte[] imageByteData = IOUtils.toByteArray(input);

		MockMultipartFile imageFile = new MockMultipartFile("data", image.getName(), "image/jpg", imageByteData);
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();//
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/photo/uploadProc.do")//
				.file(imageFile))//
				.andExpect(status().is(200))//
				.andExpect(content().string("true"));//

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
		resultActions.andExpect(status().is(200));
		resultActions.andExpect(content().string("true"));

		// 2. 연관 폴더 등록
		callRequest = MockMvcRequestBuilders.get("/photo/addRelationFolder.do");
		callRequest.param("photoId", photoId);
		callRequest.param("folderSeq", String.valueOf(folderList.get(1).getFolderSeq()));
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(200));
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
		resultActions.andExpect(status().is(200));
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
		resultActions.andExpect(status().is(200));
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
		resultActions.andExpect(status().is(200));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);

		Assert.assertTrue("보호 이미지 여부", photo.isProtectF());

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		callRequest = MockMvcRequestBuilders.get("/photo/updateProtect.do");
		photoId = photoList.get(0).getPhotoId();
		callRequest.param("photoId", photoId);
		callRequest.param("protect", "false");
		resultActions = mockMvc.perform(callRequest);
		resultActions.andExpect(status().is(200));
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
		resultActions.andExpect(status().is(200));
		resultActions.andExpect(content().string("true"));

		PhotoVo photo = photoRepository.findOne(photoId);
		Assert.assertNull(photo);

		System.out.println("끝.");
	}
}
