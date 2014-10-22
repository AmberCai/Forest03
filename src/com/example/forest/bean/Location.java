package com.example.forest.bean;

public class Location
{
	public double lat,lon;

	public Location(double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
	}
	public double getLatitude() {
		return lat;
	}

	public void setLatitude(double latitude) {
		this.lat = latitude;
	}

	public double getLongitude() {
		return lon;
	}

	public void setLongitude(double longitude) {
		this.lon = longitude;
	}
	
}
