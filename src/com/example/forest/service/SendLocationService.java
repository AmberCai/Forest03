package com.example.forest.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;

import com.example.forest.util.BDLocationHelper;
import com.example.forest.util.Const;
import com.example.forest.util.DBManager;
import com.example.forest.util.Util;

public class SendLocationService extends Service {

	// //监听网络状态
	// NetState netreceiver = null;
	// IntentFilter netfilter = null;
	// //监听GPS状态
	// GPSState gpsreceiver = null;
	// IntentFilter gpsfilter = null;

	// SharedPreferences conPreferences;
	Util util = null;
	BDLocationHelper helper;

	@Override
	public void onCreate() {
		super.onCreate();

		// //注册移动网络状态接收器
		// netreceiver = new NetState();
		// netfilter = new IntentFilter();
		// netfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// this.registerReceiver(netreceiver, netfilter);
		// netreceiver.onReceive(this, null);
		//
		// //注册gps状态状态接收器
		// gpsreceiver = new GPSState();
		// gpsfilter = new IntentFilter();
		// gpsfilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
		// this.registerReceiver(gpsreceiver, gpsfilter);
		// gpsreceiver.onReceive(this, null);

		// conPreferences = Forest.config_preferences;

		util = new Util(SendLocationService.this);

		helper = BDLocationHelper.getInstance(this);
		helper.getLocation();
	}

	public void send_unsendLocation() {
		List<String> unsendLocationList;
		DBManager dbManager = new DBManager(SendLocationService.this);
		dbManager.openDatabase();
		unsendLocationList = dbManager.query_unsendlocation();

		if (unsendLocationList != null && unsendLocationList.size() > 0) {
			for (int i = 0; i < unsendLocationList.size(); i++) {
				System.out.println("未发送成功的定位记录:" + unsendLocationList.get(i));
				boolean sendsuc_flag = util.sendLocation(unsendLocationList
						.get(i));
				if (sendsuc_flag == true) {
					dbManager.update_senddedlocation(unsendLocationList.get(i));
					dbManager.delete_sendedlocation();
				} else {
					break;
				}
			}
		}
		dbManager.closeDatabase();
	}

	public void send_unsendPhotos() {
		List<String> unsendPhotosList;
		DBManager dbManager = new DBManager(SendLocationService.this);
		dbManager.openDatabase();
		unsendPhotosList = dbManager.query_unsendPhotos();

		if (unsendPhotosList != null && unsendPhotosList.size() > 0) {
			for (int i = 0; i < unsendPhotosList.size(); i++) {
				System.out.println("未发送成功的照片名:" + unsendPhotosList.get(i)
						+ ".jpg");
				File file = new File(Environment.getExternalStorageDirectory()
						+ "/forest/msg/", unsendPhotosList.get(i) + ".jpg");

				boolean sendsuc_flag = Util.uploadFile(file);
				if (sendsuc_flag == true) {
					dbManager.update_sendPhoto(unsendPhotosList.get(i));
					dbManager.delete_sendedPhoto();
				} else {
					break;
				}
			}
		}
		dbManager.closeDatabase();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		helper.release();
		// this.unregisterReceiver(gpsreceiver);
		// this.unregisterReceiver(netreceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		int t = super.onStartCommand(intent, flags, startId);

		Date now = new Date();
		SimpleDateFormat dat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat time = new SimpleDateFormat("HHmmss");

		final String loc_dat = dat.format(now);
		final String loc_time = time.format(now);

		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				send_unsendLocation();
				send_unsendPhotos();

				// 获得足迹定位信息
				String msg = util.obtainLocation_Auto(loc_dat, loc_time,
						Const.FOOT, SendLocationService.this);
				boolean flag = util.sendLocation(msg);

				/**
				 * @date 20140915
				 * @content 以上flag也可能为false,这种情况下以下的if语句块存在逻辑错误！！！
				 */
				if (!flag) {
					System.out.println("SendService中thread发送失败，存入数据库");
					DBManager dbManager = new DBManager(
							SendLocationService.this);
					dbManager.openDatabase();
					dbManager.insert_location(msg, 0);
					dbManager.closeDatabase();
				}
			}
		};

		// //网络更新消息通告
		// Thread notifyThread = new Thread()
		// {
		// @Override
		// public void run() {
		// super.run();
		// Main.notify = ResolveXML.getNotification();
		//
		// DBManager dbManager = new DBManager(SendLocationService.this);
		// dbManager.openDatabase();
		// dbManager.delete_readedMsg();
		//
		// for(int i=0; i<Main.notify.size();i++)
		// {
		// if(Main.notify.get(i) != null &&
		// !Main.notify.get(i).trim().equals(""))
		// dbManager.insert_serverMsg(Main.notify.get(i), 0);
		// System.out.println("获取到服务器的通告消息："+Main.notify.get(i));
		// }
		// dbManager.closeDatabase();
		// }
		// };
		//
		// if(conPreferences.getBoolean("shangban", false))

		/**
		 * @date 20140915
		 * @content 以下语句块表明，仅在用户按下“上班”按钮后才执行发送缓存信息、发送当前位置信息的操作
		 */
		if (Util.shangbanFlag) {
			thread.start();
		}
		return t;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**
	 * @date 20140915
	 * @content 以下语句块表明在网络可用，并且是移动数据连接的网络条件下执行发送缓存信息操作
	 * 
	 */
	// 网络连接状态广播接收器
	class NetState extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo gprs = manager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (!gprs.isConnected()) {
				// 网络未连接状态
				System.out.println("网络变为不可用");
			} else {
				System.out.println("网络变为可用");
				send_unsendLocation();
				send_unsendPhotos();
			}
		}
	}

	// GPS状态广播接收器
	class GPSState extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			LocationManager locmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			boolean gps_use = locmanager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (gps_use) {
				Const.GPS_STATE = Const.GPS_AVILIBALE;
			} else {
				Const.GPS_STATE = Const.GPS_UNAVILIBALE;
			}
		}
	}
}
