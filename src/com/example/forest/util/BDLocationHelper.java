package com.example.forest.util;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class BDLocationHelper {

	private static BDLocationHelper helper;
	private static BDLocation myLocation;
	private static LocationClient mLocationClient;
	private static MyLocationListener listener;
	private static LocationClientOption option;

	public BDLocationHelper(Context context) {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(context);
		}
		if (listener == null) {
			listener = new MyLocationListener();
		}
		if (option == null) {
			option = new LocationClientOption();
			option.setPriority(LocationClientOption.GpsFirst);
			option.setLocationMode(LocationMode.Hight_Accuracy);
			option.setOpenGps(true);
			option.setTimeOut(15 * 1000);
			option.setScanSpan(10 * 1000);
			option.setCoorType("gcj02");
		}
	}

	public static BDLocationHelper getInstance(Context context) {
		if (helper == null) {
			helper = new BDLocationHelper(context);
		}
		mLocationClient.registerLocationListener(listener);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		mLocationClient.requestLocation();
		return helper;
	}

	public BDLocation getLocation() {
		return myLocation;
	}

	public void release() {
		if (mLocationClient != null && mLocationClient.isStarted()) {
			myLocation = null;
			mLocationClient.stop();
			mLocationClient.unRegisterLocationListener(listener);
			listener = null;
			mLocationClient = null;
			helper = null;
		}
	}

	private static class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation loc) {
			if (loc != null && loc.getLatitude() != 4.9E-324
					&& loc.getLatitude() != 0) {
				System.out.println("纬度：" + loc.getLatitude() + "\n纬度："
						+ loc.getLongitude());
				Const.cur_location.setLatitude(loc.getLatitude());
				Const.cur_location.setLongitude(loc.getLongitude());
				Const.cur_location = new ChinaMap()
						.transformFromGCJToWGS(Const.cur_location);
			} else {
				System.out
						.println("上次纬度："
								+ mLocationClient.getLastKnownLocation()
										.getLatitude()
								+ "\n经度："
								+ mLocationClient.getLastKnownLocation()
										.getLongitude());
				Const.cur_location.setLongitude(mLocationClient
						.getLastKnownLocation().getLongitude());
				Const.cur_location.setLatitude(mLocationClient
						.getLastKnownLocation().getLatitude());
				Const.cur_location = new ChinaMap()
						.transformFromGCJToWGS(Const.cur_location);
			}

		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub

		}

	}

}
