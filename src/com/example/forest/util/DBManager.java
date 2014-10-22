/**
 * forest.sdb这个数据库我是用的一个SQLite DataBase管理工具直接创建的，不是用代码来创建的，因为这样可以直接
 * 用管理工具可视化的看到数据库是什么样的，然后将创建的这个.sdb的数据库文件拷贝到raw文件夹中直接使用，
 * 要想打开这个.sdb数据库文件需要下载一个SQLite管理工具打开查看数据表的结构和字段，直接打开肯定是乱码的，
 * 或者你也可以在电脑的运行，输入cmd,然后在DOS窗口，输入命令adb shell 进入android Linux命令中，
 * 使用命令查看数据库结构；
 */

package com.example.forest.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.forest.R;
import com.example.forest.bean.UserInfo;

public class DBManager {
	private final int BUFFER_SIZE = 1024;
	public static final String DB_NAME = "forest.sdb";
	public static final String PACKAGE_NAME = "com.example.forest";
	public static final String DB_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME;
	private SQLiteDatabase database;
	private Context context;
	private File file = null;

	public DBManager(Context context) {
		this.context = context;
	}

	/**
	 * 打开数据库
	 */
	public void openDatabase() {
		this.database = this.openDatabase(DB_PATH + "/" + DB_NAME);
	}

	/**
	 * 返回数据库
	 * 
	 * @return
	 */
	private SQLiteDatabase getDatabase() {
		return this.database;
	}

	/**
	 * 打开数据库文件，并返回数据库
	 * 
	 * @param dbfile
	 * @return
	 */
	private SQLiteDatabase openDatabase(String dbfile) {
		try {
			file = new File(dbfile);
			if (!file.exists()) {
				InputStream is = context.getResources().openRawResource(
						R.raw.forest);
				FileOutputStream fos = new FileOutputStream(dbfile);
				byte[] buffer = new byte[BUFFER_SIZE];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
					fos.flush();
				}
				fos.close();
				is.close();
			}
			// ???此处为何不是database = SQLiteDatabase.openOrCreateDatabase(file,
			// null);
			database = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
			return database;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 关闭数据库
	 */
	public void closeDatabase() {
		if (this.database != null)
			this.database.close();
	}

	/****************** 定位信息存取删除相关数据库操作 *******************************************/
	/**
	 * 1、向数据库中插入一条定位记录
	 * 
	 * @param content
	 * @param sendsuc_flag
	 */
	public void insert_location(String content, int sendsuc_flag) {
		System.out.println("数据库插入定位记录");
		database.execSQL("insert into location(content,sendsuc_flag) "
				+ "values('" + content + "'," + sendsuc_flag + ")");
	}

	/**
	 * 2、查询未发送成功的定位信息
	 * 
	 * @return
	 */
	public List<String> query_unsendlocation() {
		System.out.println("数据库查询未发送定位记录");
		List<String> list = new ArrayList<String>();

		Cursor cursor = database.rawQuery(
				"select * from location where sendsuc_flag=0", null);
		while (cursor.moveToNext()) {
			String location = cursor
					.getString(cursor.getColumnIndex("content"));
			list.add(location);
		}
		cursor.close();
		return list;
	}

	/**
	 * 3、更新已发成功的定位信息标志位
	 * 
	 * @param content
	 */
	public void update_senddedlocation(String content) {
		System.out.println("数据库更新定位记录");
		database.execSQL("update location set sendsuc_flag=1 where content='"
				+ content + "'");
	}

	/**
	 * 4、删除数据库中已发的定位记录
	 */
	public void delete_sendedlocation() {
		System.out.println("数据库删除定位记录");
		database.execSQL("delete from location where sendsuc_flag=1");
	}

	/*************** 服务器消息存取删除数据库操作 *************************************************/
	/**
	 * 1、向数据库中插入一条从服务器接收到的消息记录
	 * 
	 * @param content
	 * @param read_flag
	 */
	public void insert_serverMsg(String content, int read_flag) {
		database.execSQL("insert into server_msg(content,read_flag) "
				+ "values('" + content + "'," + read_flag + ")");
	}

	/**
	 * 2、查询未读取的服务器消息
	 * 
	 * @return
	 */
	public List<String> query_unreadMsg() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = database.rawQuery(
				"select * from server_msg where read_flag=0", null);
		while (cursor.moveToNext()) {
			String msg = cursor.getString(cursor.getColumnIndex("content"));
			list.add(msg);
		}
		cursor.close();
		return list;
	}

	/**
	 * 3、更新已阅读的服务器消息标志位 阅读后将标志位设为1
	 * 
	 * @param content
	 */
	public void update_readedMsg(String content) {
		database.execSQL("update server_msg set read_flag=1 where content='"
				+ content + "'");
	}

	/**
	 * 4、删除数据库中已阅读的服务器消息
	 */
	public void delete_readedMsg() {
		database.execSQL("delete from server_msg where read_flag=1");
	}

	/**************** 照片存取删除数据库操作 ******************************************************/
	/**
	 * 1、向数据库中插入一条照片的路径
	 * 
	 * @param path
	 * @param sendsuc_flag
	 */
	public void insert_photos(String path, int sendsuc_flag) {
		database.execSQL("insert into photos(path,sendsuc_flag) " + "values('"
				+ path + "'," + sendsuc_flag + ")");
	}

	/**
	 * 2、查询未发送成功的照片记录
	 * 
	 * @return
	 */
	public List<String> query_unsendPhotos() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = database.rawQuery(
				"select * from photos where sendsuc_flag=0", null);
		while (cursor.moveToNext()) {
			String photo_name = cursor.getString(cursor.getColumnIndex("path"));
			list.add(photo_name);
		}
		cursor.close();
		return list;
	}

	/**
	 * 3、更新已发送成功的照片信息 发送成功，标志位设为1
	 * 
	 * @param name
	 */
	public void update_sendPhoto(String name) {
		database.execSQL("update photos set sendsuc_flag=1 where path='" + name
				+ "'");
	}

	/**
	 * 4、删除数据库中已经发送的照片信息
	 */
	public void delete_sendedPhoto() {
		database.execSQL("delete from photos where sendsuc_flag=1");
	}

	/*************** 生物种类数据库存取删操作 *****************************************************/

	public boolean haspestsKinds() {
		Cursor cursor = database.rawQuery("select * from pests_kinds", null);
		if (cursor.moveToNext()) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
	}

	/**
	 * 1、插入生物种类
	 * 
	 * @param kindsnumber
	 * @param kindsname
	 */
	public void insert_pestsKinds(String kindsnumber, String kindsname) {
		// 插入生物种类之前把保存的所有生物种类信息删除，重新插入
		delete_pestsKinds();

		String kindsname_list[] = kindsname.split(",");
		String kindsnumber_list[] = kindsnumber.split(",");

		for (int i = 0; i < kindsnumber_list.length; i++) {
			database.execSQL("insert into pests_kinds(id,kinds) values('"
					+ kindsnumber_list[i] + "','" + kindsname_list[i] + "')");
		}
	}

	/**
	 * 2、查询生物种类
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsKinds() {
		Map<String, String> kinds_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_kinds", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("kinds"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			kinds_list.put(number, name);
		}
		cursor.close();
		return kinds_list;
	}

	/**
	 * 3、删除所有生物种类信息
	 */
	private void delete_pestsKinds() {
		database.execSQL("delete from pests_kinds");
	}

	/*************** 生物生长阶段数据库存取删操作 *****************************************************/
	/**
	 * 1、插入生物生长阶段
	 * 
	 * @param stagenumber
	 * @param stagename
	 */
	public void insert_pestsStage(String stagenumber, String stagename) {
		// 插入生长阶段之前删除所有之前保存的生长阶段定义
		delete_pestsStage();

		String stagename_list[] = stagename.split(",");
		String stagenumber_list[] = stagenumber.split(",");

		for (int i = 0; i < stagename_list.length; i++) {
			database.execSQL("insert into pests_stage(id,stage) values('"
					+ stagenumber_list[i] + "','" + stagename_list[i] + "')");
		}
	}

	/**
	 * 2、查询生物生长阶段
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsStage() {
		Map<String, String> stage_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_stage", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("stage"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			stage_list.put(number, name);
		}
		cursor.close();
		return stage_list;
	}

	/**
	 * 3、删除所有成长阶段
	 */
	private void delete_pestsStage() {
		database.execSQL("delete from pests_stage");
	}

	/*************** 受害数量数据库存取删操作 *****************************************************/
	/**
	 * 1、插入受害数量
	 * 
	 * @param amountnumber
	 * @param amountname
	 */
	public void insert_pestsAmount(String amountnumber, String amountname) {
		// 插入受害数量定义之前，删除之前的数量定义
		delete_pestsAmount();

		String amountname_list[] = amountname.split(",");
		String amountnumber_list[] = amountnumber.split(",");

		for (int i = 0; i < amountname_list.length; i++) {
			database.execSQL("insert into pests_amount(id,amount) values('"
					+ amountnumber_list[i] + "','" + amountname_list[i] + "')");
		}
	}

	/**
	 * 2、查询受害数量
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsAmount() {
		Map<String, String> amount_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_amount", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("amount"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			amount_list.put(number, name);
		}
		cursor.close();
		return amount_list;
	}

	/**
	 * 3、删除所有生物数量
	 */
	private void delete_pestsAmount() {
		database.execSQL("delete from pests_amount");
	}

	/*************** 危害程度数据库存取删操作 *****************************************************/
	/**
	 * 1、插入受害程度
	 * 
	 * @param levelnumber
	 * @param levelname
	 */
	public void insert_pestsLevel(String levelnumber, String levelname) {
		// 插入受害程度定义之前，先删除之前定义的各个受害程度
		delete_pestsLevel();

		String levelnumber_list[] = levelnumber.split(",");
		String levelname_list[] = levelname.split(",");

		for (int i = 0; i < levelname_list.length; i++) {
			database.execSQL("insert into pests_level(id,level) values('"
					+ levelnumber_list[i] + "','" + levelname_list[i] + "')");
		}
	}

	/**
	 * 2、查询受害程度
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsLevel() {
		Map<String, String> level_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_level", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("level"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			level_list.put(number, name);
		}
		cursor.close();
		return level_list;
	}

	/**
	 * 3、删除所有受害程度
	 */
	private void delete_pestsLevel() {
		database.execSQL("delete from pests_level");
	}

	/*************** 处理建议数据库存取删操作 *****************************************************/
	/**
	 * 1、插入处理建议
	 * 
	 * @param adviseName
	 * @param adviseNumber
	 */
	public void insert_pestsAdvise(String adviseName, String adviseNumber) {
		// 插入处理建议之前先删除所有之前的建议定义
		delete_pestsAdvise();

		String advisenumber_list[] = adviseNumber.split(",");
		String advisename_list[] = adviseName.split(",");

		for (int i = 0; i < advisename_list.length; i++) {
			database.execSQL("insert into pests_advise(id,advise) values('"
					+ advisenumber_list[i] + "','" + advisename_list[i] + "')");
		}
	}

	/**
	 * 2、查询处理建议
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsAdvise() {
		Map<String, String> advise_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_advise", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("advise"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			advise_list.put(number, name);
		}
		cursor.close();
		return advise_list;
	}

	// 3、删除所有处理建议
	private void delete_pestsAdvise() {
		database.execSQL("delete from pests_advise");
	}

	/***************** 病虫类别等详情最后更新时间 **************************************************/
	/**
	 * 1、获取最后更新时间
	 * 
	 * @return
	 */
	public String query_LastTime() {
		String lastTime = "";
		Cursor cursor = database.rawQuery("select * from pests_lasttime", null);
		while (cursor.moveToNext()) {
			lastTime = cursor.getString(cursor.getColumnIndex("lasttime"));
			System.out.println("查询上次更新病虫害详情的时间为：" + lastTime);
		}
		cursor.close();
		return lastTime;
	}

	/**
	 * 2、插入最后更新时间
	 * 
	 * @param lastTime
	 */
	public void insert_LastTime(String lastTime) {
		database.execSQL("delete from pests_lasttime");
		database.execSQL("insert into pests_lasttime(lasttime) values('"
				+ lastTime + "')");
	}

	/******************** 网络验证成功后，将用户名和密码存在本地 ***************************************/
	/**
	 * 
	 * @param user
	 *            1、插入一个合法的用户
	 */
	public void insert_User(UserInfo user) {
		database.execSQL("insert into user_info(username,password,user_id,belong_farm) values('"
				+ user.getUserName()
				+ "','"
				+ user.getPassword()
				+ "','"
				+ user.getUserID() + "','" + user.getBelongFarm() + "')");
	}

	/**
	 * 查询本地是否存在用户名为username，密码为password的合法用户
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean isUserValid(String username, String password) {
		System.out.println("本地验证");
		Cursor cursor = database.rawQuery(
				"select * from user_info where username='" + username
						+ "' and password='" + password + "'", null);
		if (cursor.moveToNext()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @return 获取本地所有用户
	 */
	public List<String> getLocalUserNameList() {
		List<String> localUserList = new ArrayList<String>();
		Cursor cursor = database.rawQuery("select * from user_info", null);
		while (cursor.moveToNext()) {
			String username = cursor.getString(cursor
					.getColumnIndex("username"));

			localUserList.add(username);
		}
		cursor.close();
		return localUserList;
	}

	/**
	 * 
	 * @param username
	 * @return 判断数据库中是否已存在该用户
	 */
	public boolean isExisted(String username) {
		Cursor cursor = database.rawQuery(
				"select * from user_info where username='" + username + "'",
				null);
		if (cursor.moveToNext()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param username
	 * @return 根据用户名获得用户的编号
	 */
	public String queryUserIDByName(String username) {
		String userID = null;
		Cursor cursor = database.rawQuery(
				"select user_id from user_info where username='" + username
						+ "'", null);
		while (cursor.moveToNext()) {
			userID = cursor.getString(cursor.getColumnIndex("user_id"));
		}
		cursor.close();
		return userID;
	}

	/**
	 * 删除本地所有的用户
	 */
	public void deleteAllLocalUser() {
		database.execSQL("delete from user_info where username!='admin'");
	}

	public void updatePassword(String username, String password) {
		database.execSQL("update user_info set password='" + password
				+ "' where username='" + username + "'");
	}
}