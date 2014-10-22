package com.example.forest.util;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import android.content.Context;

import com.example.forest.activity.Login;
import com.example.forest.bean.UserInfo;

public class ResolveXML {

	public static final String LASTUPDATETIME = "lastUpdateTime";

	public static final String KINDSNAME = "kindsname";
	public static final String KINDSNUMBER = "kindsnumber";

	public static final String STAGENAME = "stagename";
	public static final String STAGENUMBER = "stagenumber";

	public static final String AMOUNTNAME = "amountname";
	public static final String AMOUNTNUMBER = "amountnumber";

	public static final String LEVELNAME = "levelname";
	public static final String LEVELNUMBER = "levelnumber";

	public static final String ADVISENAME = "advisename";
	public static final String ADVISENUMBER = "advisenumber";

	public static final String HANDLENAME = "handlename";
	public static final String HANDLENUMBER = "handlenumber";

	/*
	 * 根据传入的参数 获得病虫害详情定义相应的字符串
	 * 
	 * pestskinds :害虫种类 pestsstage :生长阶段 pestsamount :受害数量 pestslevel :危害程度
	 * pestsadvise :处理建议
	 */
	public static String[] getValue() {
		String BASE_URL = "http://" + Login.SERVER_IP
				+ ":8080/AndroidServer/servlet/";
		String[] result = new String[11];
		String urlStr = BASE_URL + "PestsDetailsServlet";
		System.out.println(urlStr);
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);

			// 第1个元素保存生物种类
			result[0] = doc.getElementsByTagName("kindsname").item(0)
					.getFirstChild().getNodeValue();
			// 第2个元素保存生种种类编号
			result[1] = doc.getElementsByTagName("kindsnumber").item(0)
					.getFirstChild().getNodeValue();

			// 第3个元素保存生长阶段
			result[2] = doc.getElementsByTagName("stagename").item(0)
					.getFirstChild().getNodeValue();
			// 第4个元素保存生长阶段编号
			result[3] = doc.getElementsByTagName("stagenumber").item(0)
					.getFirstChild().getNodeValue();

			// 第5个元素保存受害数量
			result[4] = doc.getElementsByTagName("amountname").item(0)
					.getFirstChild().getNodeValue();
			// 第6个元素保存受害数量编号
			result[5] = doc.getElementsByTagName("amountnumber").item(0)
					.getFirstChild().getNodeValue();

			// 第7个元素保存危害程度
			result[6] = doc.getElementsByTagName("levelname").item(0)
					.getFirstChild().getNodeValue();
			// 第8个元素保存危害程度编号
			result[7] = doc.getElementsByTagName("levelnumber").item(0)
					.getFirstChild().getNodeValue();

			// 第9个元素保存处理建议
			result[8] = doc.getElementsByTagName("advisename").item(0)
					.getFirstChild().getNodeValue();
			// 第10个元素保存处理建议编号
			result[9] = doc.getElementsByTagName("advisenumber").item(0)
					.getFirstChild().getNodeValue();

			// 第11个元素保存最后更新时间
			result[10] = doc.getElementsByTagName("lastUpdateTime").item(0)
					.getFirstChild().getNodeValue();
			conn = null;
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}

	public static List<String> getNotification() {
		String BASE_URL = "http://" + Login.SERVER_IP
				+ ":8080/AndroidServer/servlet/";
		List<String> notifyList = new ArrayList<String>();

		// String urlStr =
		// BASE_URL+"NotifyServlet"+"?phoneID="+Forest.config_preferences.getString("phoneID",
		// "xxxxxxxx");
		String urlStr = BASE_URL + "NotifyServlet";
		System.out.println(urlStr);
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(3000);
			InputStream in = conn.getInputStream();

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			String result = doc.getElementsByTagName("notification").item(0)
					.getFirstChild().getNodeValue();
			System.out.print("通知消息：" + result);
			conn = null;
			if (result != null && !result.trim().equals("")) {
				String[] notify = result.split(",");
				for (int i = 0; i < notify.length; i++) {
					notifyList.add(notify[i]);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return notifyList;
	}

	// ////////添加的代码20140630开始---彭老师////////////////////
	public static boolean setXMLPhoneID(Context context, String phoneID) {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		boolean result = true;
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// phoneID.xml 放到 assets目录中的
			doc = docBuilder.parse(context.getResources().getAssets()
					.open("phoneID.xml"));
			if (doc != null) {
				// 将phoneID放入phoneID.xml中
				doc.getElementsByTagName("phoneID").item(0).getFirstChild()
						.setNodeValue(phoneID);
			}
		} catch (Exception e) {
			// TODO: handle exception
			result = false;
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}

		return result;
	}

	public static String getXMLPhoneID(Context context) {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		String xmlphoneID = "xxxxxxxx";
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(context.getResources().getAssets()
					.open("phoneID.xml"));
			if (doc != null) {
				xmlphoneID = doc.getElementsByTagName("phoneID").item(0)
						.getFirstChild().getNodeValue();
				System.out.println("========getxmlPhoneID：" + xmlphoneID);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("getxmlPhoneID异常：" + e.getClass().getName());
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}

		return xmlphoneID;
	}

	// ////////添加的代码20140630结束////////////////////

	public static String getPhoneID(String mac) {
		String BASE_URL = "http://" + Login.SERVER_IP
				+ ":8080/AndroidServer/servlet/";
		String phoneID = "xxxxxxxx";
		String urlStr = BASE_URL + "QueryPhoneIDServlet?mac=" + mac;
		System.out.println(urlStr);
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(3000);
			InputStream in = conn.getInputStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			if (doc != null) {
				phoneID = doc.getElementsByTagName("phoneID").item(0)
						.getFirstChild().getNodeValue();
			}
			conn = null;
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			phoneID = "netexception";
		} catch (ConnectException e) {
			e.printStackTrace();
			phoneID = "netexception";
			System.out.println("getPhoneID异常：" + e.getClass().getName());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("getPhoneID异常：" + e.getClass().getName());
		}
		return phoneID;
	}

	public static List<UserInfo> getAllUser() {
		String BASE_URL = "http://" + Login.SERVER_IP
				+ ":8080/AndroidServer/servlet/";
		List<UserInfo> userList = new ArrayList<UserInfo>();
		String urlStr = BASE_URL + "UserServlet";
		System.out.println(urlStr);
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			String userIDStr = doc.getElementsByTagName("userid").item(0)
					.getFirstChild().getNodeValue();
			String userNameStr = doc.getElementsByTagName("username").item(0)
					.getFirstChild().getNodeValue();
			String passStr = doc.getElementsByTagName("pass").item(0)
					.getFirstChild().getNodeValue();
			String belongFarmStr = doc.getElementsByTagName("belongfarm")
					.item(0).getFirstChild().getNodeValue();

			conn = null;
			if (userIDStr != null && userNameStr != null && passStr != null
					&& belongFarmStr != null) {
				for (int i = 0; i < userIDStr.split(",").length; i++) {
					UserInfo userinfo = new UserInfo();
					userinfo.setUserID(userIDStr.split(",")[i]);
					userinfo.setUserName(userNameStr.split(",")[i]);
					userinfo.setPassword(passStr.split(",")[i]);
					userinfo.setBelongFarm(belongFarmStr.split(",")[i]);
					userList.add(userinfo);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getClass().getName());
			e.printStackTrace();
		}
		return userList;
	}

}
