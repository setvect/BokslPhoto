package com.setvect.bokslphoto.test.photo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.setvect.bokslphoto.repository.FolderRepository;
import com.setvect.bokslphoto.repository.PhotoRepository;
import com.setvect.bokslphoto.service.PhotoService;
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

	@Autowired
	private PhotoService photoService;

	@Test
	public void testUploadPhoto() throws Exception {
		File image = new File("./temp/a.jpg");
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
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();//
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

		FolderVo folder = folderRepository.getOne(folderSeq);
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
		// TODO PBM_20170514: M:N 관계에서 폴더를 findOne을 했을 경우 사진 목록이 나오지 않는 상황
		// Assert.assertThat(folder.getPhotos().get(0).getPhotoId(),
		// CoreMatchers.is(photoId));

		System.out.println("끝.");
	}

}
