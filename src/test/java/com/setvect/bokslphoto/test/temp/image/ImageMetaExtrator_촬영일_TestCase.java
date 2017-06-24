package com.setvect.bokslphoto.test.temp.image;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.setvect.bokslphoto.ApplicationUtil;
import com.setvect.bokslphoto.BokslPhotoConstant;

public class ImageMetaExtrator_촬영일_TestCase {
	public static void main(String[] args) throws ImageProcessingException, IOException {
		File file = new File("test_data/temp_folder/여행/풍경/2013-04-12_11-55-47_282.jpg");

		Metadata metadata = ImageMetadataReader.readMetadata(file);
		ExifSubIFDDirectory meta = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

		// 2017:01:07 09:45:11
		Optional<LocalDateTime> shotDate = meta.getTags().stream()
				.filter(tag -> tag.getTagName().equals(BokslPhotoConstant.ImageMeta.DATE_TIME_ORIGINAL))
				.map(tag -> tag.getDescription()).findAny()
				.map(dateStr -> LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));

		LocalDateTime value = shotDate.orElse(null);
		if (value != null) {
			Date date = ApplicationUtil.getDate(value);
			System.out.println(date);
			System.out.println(value);
		}
	}
}