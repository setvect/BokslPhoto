package com.setvect.bokslphoto.test.temp.image;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageProcessingException;
import com.setvect.bokslphoto.service.GeoCoordinates;
import com.setvect.bokslphoto.service.PhotoService;

public class ImageMetaExtrator_GEO_TestCase {
	private static Logger logger = LoggerFactory.getLogger(ImageMetaExtrator_GEO_TestCase.class);

	public static void main(String[] args) throws ImageProcessingException, IOException {
		File file = new File("test_data/temp_folder/여행/풍경/2012-12-01_13-56-13_632.jpg");
		GeoCoordinates g = PhotoService.getGeo(file);
		System.out.println(g);
		System.out.println("끝.");
	}

}