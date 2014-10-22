package com.example.forest.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {

	private static final String TAG = "CrashHandler";
	
	//系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	
	//Crashhandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	
	private Context mContext;   //程序的Context对象
	
	//用来存储设备信息和异常信息
	private Map<String, String> info = new HashMap<String, String>();
	
	//用于格式化日期，作为日志文件的一部分
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	//保证只有一个CrashHandler实例
	private CrashHandler()
	{
		
	}
	
	//获取CrashHandler实例，单例模式
	public static CrashHandler getInstance()
	{
		return INSTANCE;
	}
	
	//初始化
	public void init(Context context)
	{
		mContext =  context;
		//获取系统默认的UnCaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		
		//该Crashhandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	//当UncaughtException发生时会转入该重写的方法来处理
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		
	}
	
	//自定义错误处理，收集错误信息
	public boolean handleException(Throwable ex)
	{
		if(ex == null)
			return false;
		Util.writedata(ex.getCause().getMessage());
		new  Thread(){
				public void run() 
				{
					Looper.prepare();
					Toast.makeText(mContext, "很抱歉，程序出现异常，即将退出", 0).show();
					Looper.loop();
				}
		}.start();
		
		//收集设备信息
		collectDeviceInfo(mContext);
		
		//保存日志文件
		saveCrashInfo2File(ex);
		return true;
	}
	
	private String saveCrashInfo2File(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, String>entry :  info.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" +value + "\r\n");
		}
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		Throwable cause = ex.getCause();
		
		while(cause != null)
		{
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		pw.close();
		String result = writer.toString();
		sb.append(result);
		
		long timetamp = System.currentTimeMillis();
		String time = format.format(new Date());
		String fileName = "crash-" + time +"-" + timetamp + ".log";
		if(Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
		{
			try
			{
				File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
						+ File.separator +"crash");
				Log.i("CrashHandler", dir.toString());
				Util.writedata("CrashHandler"+dir.toString());
				if(!dir.exists())
					dir.mkdir();
				FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
				fos.write(sb.toString().getBytes());
				fos.close();
				return fileName;
			}catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	//收集设备参数
	public void collectDeviceInfo(Context context)
	{
		try{
			PackageManager pm = context.getPackageManager();  //获得包管理器
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 
					PackageManager.GET_ACTIVITIES);
			if(pi != null)
			{
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				info.put("versionName", versionName);
				info.put("versionCode", versionCode);
			}
		}catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		Field[] fields = Build.class.getDeclaredFields();
		for(Field field : fields)
		{
			try
			{
				field.setAccessible(true);
				info.put(field.getName(), field.get("").toString());
				Log.d(TAG, field.getName()+":"+field.get(""));
				Util.writedata(field.getName()+":"+field.get(""));
			}catch (IllegalArgumentException e) {
				e.printStackTrace();
			}catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	
}
