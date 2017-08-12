package com.setvect.bokslphoto.test.temp.etc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.setvect.bokslphoto.util.LapTimeChecker;

public class ImageAutoRotate {
	public static void main(String[] args) throws Exception {

		LapTimeChecker ck = new LapTimeChecker("aaa");
		File input = new File("temp/aa.jpg");

		ImageInformation a = readImageInformation(input);
		ck.check("#1");
		System.out.println(a);

		AffineTransform b = getExifTransformation(a);
		ck.check("#2");
		System.out.println(b);

		BufferedImage in = ImageIO.read(input);
		ck.check("#3");
		BufferedImage c = transformImage(in, b);
		ck.check("#4");
	}

	public static ImageInformation readImageInformation(File imageFile)
			throws IOException, MetadataException, ImageProcessingException {
		LapTimeChecker ck = new LapTimeChecker("bbbb");
		Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
		ck.check("1");
		Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		ck.check("2");
		JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
		ck.check("3");
		int orientation = 1;
		try {
			orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
			ck.check("4");
		} catch (MetadataException me) {
			System.err.println(me.getMessage());
		}
		int width = jpegDirectory.getImageWidth();
		int height = jpegDirectory.getImageHeight();

		return new ImageInformation(orientation, width, height);
	}

	public static BufferedImage transformImage(BufferedImage image, AffineTransform transform) throws Exception {

		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

		BufferedImage destinationImage = op.createCompatibleDestImage(image,
				(image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? image.getColorModel() : null);
		Graphics2D g = destinationImage.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
		destinationImage = op.filter(image, destinationImage);
		return destinationImage;
	}

	public static AffineTransform getExifTransformation(ImageInformation info) {

		AffineTransform t = new AffineTransform();

		switch (info.orientation) {
		case 1:
			break;
		case 2: // Flip X
			t.scale(-1.0, 1.0);
			t.translate(-info.width, 0);
			break;
		case 3: // PI rotation
			t.translate(info.width, info.height);
			t.rotate(Math.PI);
			break;
		case 4: // Flip Y
			t.scale(1.0, -1.0);
			t.translate(0, -info.height);
			break;
		case 5: // - PI/2 and Flip X
			t.rotate(-Math.PI / 2);
			t.scale(-1.0, 1.0);
			break;
		case 6: // -PI/2 and -width
			t.translate(info.height, 0);
			t.rotate(Math.PI / 2);
			break;
		case 7: // PI/2 and Flip
			t.scale(-1.0, 1.0);
			t.translate(-info.height, 0);
			t.translate(0, info.width);
			t.rotate(3 * Math.PI / 2);
			break;
		case 8: // PI / 2
			t.translate(0, info.width);
			t.rotate(3 * Math.PI / 2);
			break;
		}

		return t;
	}

	// Inner class containing image information
	public static class ImageInformation {
		public final int orientation;
		public final int width;
		public final int height;

		public ImageInformation(int orientation, int width, int height) {
			this.orientation = orientation;
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString() {
			return String.format("%dx%d,%d", this.width, this.height, this.orientation);
		}
	}

}
