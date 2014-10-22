package com.example.forest.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

import com.example.forest.activity.Login;

public class Util {
	public static List<String> notify;

	/******** 记录发送过程 **********************/
	static final String SDPATH = Environment.getExternalStorageDirectory()
			+ "/";
	static final String filename = "fail_record.txt";
	static File file = null;
	/***************************************************************************
	 * 开始
	 * 
	 * @date 20140912 更改以下代码
	 * @content 使phoneID和userID不初始化为“xxxxxxxx”,而是初始化为从sharedPreferences读取的值;
	 * 
	 **/
	// 打开本地已经存在的sharedPreferences文件
	static SharedPreferences sharedPreferences = Forest.config_preferences;
	final Editor editor = sharedPreferences.edit();
	// 静态全局变量
	public static String phoneID = sharedPreferences.getString("phoneID", "");
	public static String userID = sharedPreferences.getString("userID", "");
	/************************************** 结束 **********************************/
	public static boolean shangbanFlag = false;

	public static String newName = "image.jpg";

	Context context;
	// Location cur_location = new Location(0, 0);

	// 记录定位的时间，日期，（照片以及病虫害详情报告的时间必须以之前定位信息的时间一致）
	Date now = null;
	public String loc_dat = null;
	public String loc_time = null;

	// SharedPreferences config_preferences;

	public Util(Context context) {
		this.context = context;
		// config_preferences = Forest.config_preferences;
	}

	// 根据传来的标号，返回定位相应的格式的字符串（火情，病虫害，滥砍滥伐）
	public String obtainLocation(String msg_type, Context context) {
		StringBuilder result = new StringBuilder();
		now = new Date();
		SimpleDateFormat dat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat time = new SimpleDateFormat("HHmmss");

		loc_dat = dat.format(now);
		loc_time = time.format(now);

		// String phoneID = Forest.config_preferences.getString("phoneID",
		// "xxxxxxxx");
		// String userID = Forest.config_preferences.getString("userID", null);

		result.append(phoneID + ","); // 手机终端编号
		result.append(dat.format(now) + ","); // 日期
		result.append(time.format(now) + ","); // 标准定位时间
		result.append(Const.GPS_STATE + ","); // GPS定位状态标识
		result.append("E,"); // 东西半球标识
		result.append(Const.cur_location.getLongitude() + ","); // 经度
		result.append("N,"); // 南北半球标识
		result.append(Const.cur_location.getLatitude() + ","); // 纬度
		result.append(msg_type + ","); // 类型
		result.append(userID);

		return result.toString();
	}

	// 自动发送时定位信息
	public String obtainLocation_Auto(String date, String time,
			String msg_type, Context context) {
		StringBuilder result = new StringBuilder();
		// String phoneID = Forest.config_preferences.getString("phoneID",
		// "xxxxxxxx");
		// String userID = Forest.config_preferences.getString("userID", null);

		result.append(phoneID + ","); // 手机终端编号
		result.append(date + ","); // 日期
		result.append(time + ","); // 标准定位时间
		result.append(Const.GPS_STATE + ","); // GPS定位状态标识
		result.append("E,"); // 东西半球标识
		result.append(Const.cur_location.getLongitude() + ","); // 经度
		result.append("N,"); // 南北半球标识
		result.append(Const.cur_location.getLatitude() + ","); // 纬度
		result.append(msg_type + ","); // 类型
		result.append(userID);

		return result.toString();
	}

	// 发送定位信息
	public boolean sendLocation(String locationMsg) {
		String BASE_URL = "http://" + Login.SERVER_IP
				+ ":8080/AndroidServer/servlet/";
		String urlStr = BASE_URL + "ReceiveMsgServlet";
		System.out.println(urlStr);
		try {
			createFile();
			writedata(locationMsg + "\n");
			String result = null;
			String notifications = null;
			if (Forest.isNetConnect(context)) {
				URL url = new URL(urlStr + "?location=" + locationMsg);
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(3000);
				InputStream in = conn.getInputStream();

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(in);
				result = doc.getElementsByTagName("received").item(0)
						.getFirstChild().getNodeValue();
				System.out.println("定位成功确认：：" + result);

				// 在发送定位信息的同时尝试从服务器端查询是否有新通告消息，如果有，则顺便将其插入本地数据库
				notifications = doc.getElementsByTagName("notification")
						.item(0).getFirstChild().getNodeValue();
				System.out.println("消息通告：" + notifications);
				writedata("消息通告：" + notifications);
				if (notifications != null && !notifications.trim().equals("")) {
					DBManager dbManager = new DBManager(context);
					dbManager.openDatabase();

					String[] notifyList = notifications.split(",");
					// 为什么不是notifyList.size()?
					// notifyList.length表示元素个数
					for (int i = 0; i < notifyList.length; i++) {
						dbManager.insert_serverMsg(notifyList[i], 0);
						System.out.println("插入数据库：" + notifyList[i]);
					}
					dbManager.closeDatabase();
				}
			}

			if (result != null && !result.equals("")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			writedata("发送定位：" + e.getMessage() + "\n");
			return false;
		}
	}

	// 发送病虫害详情
	public boolean sendPestsDetail(String detailMsg) {
		String BASE_URL = "http://" + Login.SERVER_IP
				+ ":8080/AndroidServer/servlet/";
		String urlStr = BASE_URL + "ReceivePestsDetailServlet";
		System.out.println(urlStr);
		try {
			URL url = new URL(urlStr + "?pestsDetail=" + detailMsg);
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			String result = doc.getElementsByTagName("received").item(0)
					.getFirstChild().getNodeValue();
			if (result != null && !result.equals("")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			writedata("发送病虫害：" + e.getMessage() + "\n");
			e.printStackTrace();
			return false;
		}
	}

	// 向服务器发送文件
	public static boolean uploadFile(File file) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		try {
			String actionUrl = "http://" + Login.SERVER_IP
					+ ":8080/AndroidServer/servlet/ReceivePhotoServlet";
			// 服务器地址
			URL url = new URL(actionUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			/* 允许Input、Output，不使用Cache */
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			/* 设置传送的method=POST */
			con.setRequestMethod("POST");
			/* setRequestProperty */
			con.setRequestProperty("Connection", "Keep-Alive");
			con.setRequestProperty("Charset", "UTF-8");
			con.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			/* 设置DataOutputStream */
			DataOutputStream ds = new DataOutputStream(con.getOutputStream());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; "
					+ "name=\"file1\";filename=\"" + newName + "\"" + end);
			ds.writeBytes(end);

			/* 取得文件的FileInputStream */
			FileInputStream fStream = new FileInputStream(file);
			System.out.println(file + "-------------------------------file");

			/* 设置每次写入1024bytes */
			int bufferSize = 2048;
			byte[] buffer = new byte[bufferSize];
			int length = -1;
			/* 从文件读取数据至缓冲区 */
			while ((length = fStream.read(buffer)) != -1) {
				/* 将资料写入DataOutputStream中 */
				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			/* close streams */
			System.out.println(ds + "========ds");
			fStream.close();
			ds.flush();
			/* 取得Response内容 */
			InputStream is = con.getInputStream();
			System.out.println(is + "=============is");
			/* 关闭DataOutputStream */
			ds.close();
			return true;
		} catch (Exception e) {
			writedata("上传照片：" + e.getMessage() + "\n");
			e.printStackTrace();
			return false;
		}
	}

	/***** 判断ip地址是否合法有效 *************/
	public static boolean isIpv4(String ipAddress) {
		String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}

	// 在SD卡的根目录下创建一个txt文件用来保存数据
	public static File createFile() throws IOException {
		System.out.println(SDPATH);

		file = new File(SDPATH, filename);
		if (file == null)
			file.createNewFile();
		return file;
	}

	// 判断文件是否已经存在
	public static boolean checkFileExists() {
		// File file = new File(SDPATH,filename);
		return file.exists();
	}

	public static void writedata(String data) {
		try {
			if (checkFileExists()) {
				FileWriter out = new FileWriter(file, true);
				out.write(data);
				out.close();
			} else {
				createFile();
				FileWriter out = new FileWriter(file, true);
				out.write(data);
				out.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.toString());
		}
	}

}
