package com.setvect.bokslphoto.test.photo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.setvect.bokslphoto.test.MainTestBase;

public class PhotoControllerTestCase extends MainTestBase {
	@Autowired
	private WebApplicationContext webApplicationContext;

	@Test
	public void testUploadPhoto() throws Exception {

		System.out.println(webApplicationContext);

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

}
