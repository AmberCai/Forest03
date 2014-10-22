package com.example.forest.util;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 创建自己的Application来进行数据传递、数据共享、数据缓存等操作。 需要在manifest文件的application标签中注册
 * 
 * @author comprq
 * 
 */

public class Forest extends Application {

	/****************************** 20140912 创建以下代码 开始 ***************************/
	public static SharedPreferences config_preferences;
	/****************************** 20140912 创建以下代码 结束 ***************************/
	// 用于保存未发送成功数据的文件
	final static String path = Environment.getExternalStorageDirectory() + "/";

	File filedir = new File(path + "forest/msg/");

	@Override
	public void onCreate() {

		super.onCreate();

		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);

		filedir.mkdirs();
		/****************************** 20140912 创建以下代码 开始 ***************************/
		config_preferences = getSharedPreferences("saveContent", MODE_PRIVATE);
		/****************************** 20140912 创建以下代码 结束 ***************************/
	}

	// 检查当前网络连接状态
	/**
	 * isNetConnect只是初略的判断是否连接上网络，而不关心当前使用的是哪种网络(WIFI or MOBILE)
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConnect(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();

				if (info != null && info.isConnected()) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}

	// 获取Android_ID
	public static String getAndroidID(Context context) {
		String android_id = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		return android_id;
	}

	// 获得Device_ID
	public String getDeviceID() {
		return ((TelephonyManager) Forest.this
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}
}