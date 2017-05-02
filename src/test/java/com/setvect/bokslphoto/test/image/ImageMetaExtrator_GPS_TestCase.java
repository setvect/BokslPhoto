package com.setvect.bokslphoto.test.image;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.setvect.bokslphoto.BokslPhotoConstant.ImageMeta;
import com.setvect.bokslphoto.BokslPhotoConstant.RegexPattern;
import com.setvect.bokslphoto.service.GeoCoordinates;

public class ImageMetaExtrator_GPS_TestCase {
	private static Logger logger = LoggerFactory.getLogger(ImageMetaExtrator_GPS_TestCase.class);

	public static void main(String[] args) throws ImageProcessingException, IOException {
		File file = new File("temp/2013-07-20_12-38-41_181.jpg");
		GeoCoordinates g = getGeo(file);
		System.out.println(g);
		System.out.println("끝.");
	}

	public static GeoCoordinates getGeo(File file) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			GpsDirectory meta = metadata.getFirstDirectoryOfType(GpsDirectory.class);

			if (meta == null) {
				System.out.println("null.");
				return null;
			}
			Pattern regex = Pattern.compile(RegexPattern.GPS);

			// 37° 28' 46.12"
			Map<String, Double> geo = meta.getTags().stream()
					.filter(tag -> tag.getTagName().equals(ImageMeta.GPS_LATITUDE)
							|| tag.getTagName().equals(ImageMeta.GPS_LONGITUDE))
					.collect(Collectors.toMap(p -> p.getTagName(), p -> {
						String coordinates = p.getDescription();
						Matcher matcher = regex.matcher(coordinates);

						if (!matcher.find()) {
							return null;
						}

						double degree = Double.parseDouble(matcher.group(1));
						double minutes = Double.parseDouble(matcher.group(2));
						double seconds = Double.parseDouble(matcher.group(3));
						double a = degree + minutes / 60 + seconds / 3600;
						return a;
					}));

			if (geo.size() == 2) {
				Double latitude = geo.get(ImageMeta.GPS_LATITUDE);
				Double longitude = geo.get(ImageMeta.GPS_LONGITUDE);
				return new GeoCoordinates(latitude, longitude);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		return null;
	}
}