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

	public GeoCoordinates(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
