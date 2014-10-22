package com.example.forest.activity;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.forest.R;
import com.example.forest.util.Const;
import com.example.forest.util.DBManager;
import com.example.forest.util.Forest;
import com.example.forest.util.ResolveXML;
import com.example.forest.util.Util;

public class Login extends Activity {

    // public final static String SERVER_IP = "221.207.25.161"; //祁连
    // public final static String SERVER_IP = "111.12.185.6"; // 大通
    // public final static String SERVER_IP = "27.21.13.29"; //电脑模拟测试
    public final static String SERVER_IP = "192.168.1.103"; // 测试

    // 界面上控件
    Spinner username;
    EditText password;
    Button login;
    /********** 20140910加入checkBox,使用 sharedPreferences来实现自动登陆 **********/
    CheckBox aotoLogin;
    private SharedPreferences sharedPreferences;
    Editor editor;
    private static final String FILE_NAME = "saveContent";

    /************************************************************************/

    // 为下拉列表定义对应的适配器
    private ArrayAdapter<String> adapter;

    // 用户名列表、用户编号列表
    List<String> localUserNameList;

    ActivityManager activityManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取ActivityManager，并判断应用是否正在运行
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        /**
         * 每次点击打开这个软件时，第一个执行的就是Login，所以首先要判断下该软件是否处于后台运行状态，如果处于后台运行状态，
         * 则需要取消掉通知栏中的最小化图标，并且将登陆界面直接跳过，不需要再次输入，而是直接进入主功能界面；
         * 如果不是处于后台运行状态，打开该软件就需要选择用户，输入密码进行登陆，才能进入主功能界面；
         */
        if (isRunning()) {
            /**
             * 如果处于后台运行状态，则需要取消掉通知栏中的最小化图标， 并且将登陆界面直接跳过，不需要再次输入，而是直接进入主功能界面；
             */
            this.finish();
            Main.notification.cancelNotification();
        }

        /********** 20140910 添加和更改以下代码，每次登录先查询本地sharedPrefernces,开始 **********/
        sharedPreferences = this.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setContentView(R.layout.activity_login);
        initView();

        spinnerDataAdapt();

        // 从sharedPreferences文件中获取保存的数据
        String nameContent = sharedPreferences.getString("username", "");
        String pwdContent = sharedPreferences.getString("password", "");
        int position = sharedPreferences.getInt("position", 0);
        Util.userID = sharedPreferences.getString("userID", "");
        Util.phoneID = sharedPreferences.getString("phoneID", "");
        // 判断是否有数据存在并进行相应处理
        if (nameContent != null && !"".equals(nameContent)
                && !nameContent.equals("admin")) {
            username.setSelection(position, true);
            if (pwdContent != null && !"".equals(pwdContent))
                password.setText(pwdContent);

            Intent intent = new Intent();
            intent = new Intent(Login.this, Main.class);
            startActivity(intent);
            Login.this.finish();
        }
        /********************************* 结束 *********************************/
        // 点击登陆按钮
        loginBtnClick();
    }

    /**
     * @date 20140910
     * @content 优化代码 初始化视图控件
     */
    public void initView() {
        username = (Spinner) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.psw);
        login = (Button) findViewById(R.id.login);
        aotoLogin = (CheckBox) findViewById(R.id.login_check1);

        // 默认记住用户名
        // username.setText(sharedPreferences.getString("userName", ""));
    }

    /**
     * @date 20140910
     * @content 优化代码 spinnerDataAdapt()-- 将spinner适配数据的代码单独提取出来供调用
     */
    public void spinnerDataAdapt() {
        // 向用户名和用户编号列表中添加本地用户信息——添加下拉列表项（数据库方式），spinner控件的数据源
        DBManager dbManager = new DBManager(Login.this);
        dbManager.openDatabase();
        localUserNameList = dbManager.getLocalUserNameList();
        dbManager.closeDatabase();
        // 为下拉列表定义适配器,用到前面的下拉列表项localUserNameList（为适配器添加数据源）
        adapter = new ArrayAdapter<String>(Login.this,
                android.R.layout.simple_spinner_item, localUserNameList);
        // 为适配器设置下拉列表下拉时的下拉样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 将适配器添加到下拉列表上
        username.setAdapter(adapter);
        // 为spinner控件设置提示词
        username.setPrompt("请选择用户名：");
    }

    /**
     * @date 20140912
     * @content 优化代码 将点击登陆按钮事件提取出来
     */
    public void loginBtnClick() {
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                String username_tmp = username.getSelectedItem().toString();
                String psw_tmp = password.getText().toString();
                int position_tmp = username.getPositionForView(username);

                // 登陆通过本地验证，跳转到主界面
                if (validate(username_tmp, psw_tmp)) {
                    // 根据用户名在本地找到user_id，并将user_id写入xml文件
                    DBManager dbManager = new DBManager(Login.this);
                    dbManager.openDatabase();
                    String userID_tmp = dbManager
                            .queryUserIDByName(username_tmp);
                    dbManager.closeDatabase();
                    Util.userID = userID_tmp;

                    /********************************* 开始 *********************************/
                    /**
                     * @date 20140911
                     * @content 第一次选择非admin用户名登陆，将用户名、密码、userID、
                     *          phoneID保存在本地sharedPreferences中
                     * 
                     */
                    String nameContent = username_tmp;
                    String pwdContent = psw_tmp;
                    int positionContent = position_tmp;
                    String userIDContent = userID_tmp;

                    if (aotoLogin.isChecked()) {
                        // 添加要保存的数据
                        editor.putString("username", nameContent);
                        editor.putString("password", pwdContent);
                        editor.putInt("position", positionContent);
                        editor.putString("userID", userIDContent);
                        // 确认保存并提交
                        // editor.putBoolean("AUTO_ISCHECK", true).commit();
                        editor.commit();
                    }
                    /********************************* 结束 *********************************/

                    Intent intent = new Intent(Login.this, Main.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    startActivity(intent);
                    Login.this.finish();
                }
                else {
                    // 未通过验证,清空输入的密码
                    password.setText("");
                }
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == Const.NO_SERVER) {
                Toast.makeText(Login.this, "您指定的服务器未开启服务！", Toast.LENGTH_LONG)
                        .show();
            }
        };
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Process.killProcess(Process.myPid());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onUserLeaveHint() {
        // TODO Auto-generated method stub
        super.onUserLeaveHint();
        Process.killProcess(Process.myPid());
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // SERVER_IP = config_preferences.getString("server_ip", "172.27.35.1");
        // //360WiFi测试

        // SERVER_IP = config_preferences.getString("server_ip",
        // "221.207.25.161"); //祁连

        Thread queryPhoneID = new Thread() {
            @Override
            public void run() {
                // 获得手机编号，存于本地xml文件
                // 将androidID当作mac???
                String mac = Forest.getAndroidID(Login.this);
                String phoneID = ResolveXML.getPhoneID(mac);
                /*********************** 20140912 添加以下代码 ****************************/
                if (phoneID != null && !phoneID.equals("null")) {
                    // 如果从网络获取的phoneID异常，则重新查询
                    if (phoneID.equals("netexception")
                            || phoneID.equals("xxxxxxxx")) {
                        phoneID = ResolveXML.getPhoneID(mac);
                    }
                    else {
                        // 如果获取的phoneID正常，则存入本地sharedPreferences中
                        String phoneIDContent = phoneID;
                        editor.putString("phoneID", phoneIDContent);
                        editor.commit();
                    }
                }
                /*********************** 20140912 注释掉以下代码 ****************************/
                // if (phoneID != null && !phoneID.equals("null")) {
                // if (phoneID.equals("netexception")
                // || phoneID.equals("xxxxxxxx"))// 修改了条件20140630
                // {
                // handler.sendEmptyMessage(Const.NO_SERVER);
                // // editor.putString("phoneID", "xxxxxxxx");
                //
                // // ////////////20140630注释下面的代码
                // // Util.phoneID = "xxxxxxxx";
                // // ////////添加的代码20140630开始////////////////////
                // String xmlphoneID = ResolveXML
                // .getXMLPhoneID(Login.this);
                // Util.phoneID = xmlphoneID;
                //
                // // ////////添加的代码20140630结束////////////////////
                // } else {
                // // editor.putString("phoneID", phoneID);
                // Util.phoneID = phoneID;
                // // ////////添加的代码20140631开始////////////////////
                // // 修改xml文件中的phoneID
                // // 加了一个条件
                // String xmlphoneID = ResolveXML
                // .getXMLPhoneID(Login.this);
                // if (!xmlphoneID.equals(phoneID))
                // ResolveXML.setXMLPhoneID(Login.this, phoneID);
                // // ////////添加的代码20140631结束////////////////////
                // }
                // // editor.commit();
                // }
            };
        };

        /**
         * @date 20140911 修改 以下这段为检测网络连接状况，即当前网络是否可用，
         *       而不是检测用户是否打开了数据连接，这种情况下，如果只用GPRS， 只要GPRS信号不好就会弹出提醒框
         */
        if (!Forest.isNetConnect(Login.this)) {
            showDialogLogin("亲，请检查数据连接状况~");
        }
        else {
            queryPhoneID.start();
        }

    }

    // 登陆验证方法，成功返回true，否则返回false
    public boolean validate(String name, String passwrd) {
        if (name == null || name.trim().equals("")) {
            showDialogLogin("请选择用户名！");
            return false;
        }
        else if (passwrd == null || passwrd.trim().equals("")) {
            showDialogLogin("请输入密码！");
            return false;
        }
        // 检索本地数据库中该用户是否存在
        DBManager dbManager = new DBManager(Login.this);
        dbManager.openDatabase();
        if (dbManager.isUserValid(name, passwrd)) {
            dbManager.closeDatabase();
            return true;
        }
        else {
            dbManager.closeDatabase();
            showDialogLogin("用户名或密码错误，请重新输入！");
            return false;
        }
    }

    // 登陆验证提示对话框
    public void showDialogLogin(String msg) {
        new AlertDialog.Builder(Login.this)
                .setIcon(android.R.drawable.btn_star_big_on).setTitle("提示")
                .setMessage(msg).setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                }).create().show();
    }

    // 判断软件是否处于打开状态（通过判断任务列表中是否已经存在Main任务）
    // isRunning()函数用于判断这个软件是否是处于后台运行状态（实现原理：判断系统后台任务队列中是否
    // 有这个软件对应的任务名称，如果有，则说明这个软件现在是在后台运行，如果没有则说明这个软件没有运行）
    // 当该软件处于后台运行时，手机屏幕最上方会有最小化的图标出现；
    private boolean isRunning() {
        // 设置系统当前正在运行的最大任务数
        int intGetTaskCounter = 30;
        List<RunningTaskInfo> list = activityManager
                .getRunningTasks(intGetTaskCounter);
        for (RunningTaskInfo task : list) {
            // task.baseActivity表示系统后台有此进程正在运行；
            // task.topActivity表示系统当前正在运行的Activity
            // 此处getClassName()可以换成getPackageName()吗？
            if (task.baseActivity.getClassName().equals(
                    "com.example.forest.activity.Main"))
                return true;
        }
        return false;
    }
}
