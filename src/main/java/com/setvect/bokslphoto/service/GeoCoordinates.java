package com.setvect.bokslphoto.service;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * GPS 좌표
 */
public class GeoCoordinates {
	/** 위도 */
	private final double latitude;
	/** 경도 */
	private final double longitude;

	/**
	 * @param latitude
	 *            경도
	 * @param longitude
	 *            위도
	 */
	public GeoCoordinates(final double latitude, final double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return 경도
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return 위도
	 */
	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
