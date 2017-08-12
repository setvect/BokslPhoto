package com.setvect.bokslphoto.test.temp.image;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.setvect.bokslphoto.service.GeoCoordinates;
import com.setvect.bokslphoto.service.PhotoService;

public class ImageMetaExtrator_GEO_TestCase {
	private static Logger logger = LoggerFactory.getLogger(ImageMetaExtrator_GEO_TestCase.class);

	public static void main(String[] args) throws ImageProcessingException, IOException {
		File file = new File("test_data/temp_folder/여행/풍경/2012-12-01_13-56-13_632.jpg");
		Metadata metadata = ImageMetadataReader.readMetadata(file);
		GeoCoordinates g = PhotoService.getGeo(metadata);
		System.out.println(g);
		System.out.println("끝.");
	}

}