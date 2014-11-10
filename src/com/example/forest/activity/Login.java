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

    // public final static String SERVER_IP = "221.207.25.161"; //����
    // public final static String SERVER_IP = "111.12.185.6"; // ��ͨ
    // public final static String SERVER_IP = "27.21.13.29"; //����ģ�����
    public final static String SERVER_IP = "192.168.1.113"; // ����

    // �����Ͽؼ�
    Spinner username;
    EditText password;
    Button login;
    /********** 20140910����checkBox,ʹ�� sharedPreferences��ʵ���Զ���½ **********/
    CheckBox aotoLogin;
    private SharedPreferences sharedPreferences;
    Editor editor;
    private static final String FILE_NAME = "saveContent";

    /************************************************************************/

    // Ϊ�����б����Ӧ��������
    private ArrayAdapter<String> adapter;

    // �û����б��û�����б�
    List<String> localUserNameList;

    ActivityManager activityManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ��ȡActivityManager�����ж�Ӧ���Ƿ���������
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        /**
         * ÿ�ε����������ʱ����һ��ִ�еľ���Login����������Ҫ�ж��¸�����Ƿ��ں�̨����״̬��������ں�̨����״̬��
         * ����Ҫȡ����֪ͨ���е���С��ͼ�꣬���ҽ���½����ֱ������������Ҫ�ٴ����룬����ֱ�ӽ��������ܽ��棻
         * ������Ǵ��ں�̨����״̬���򿪸��������Ҫѡ���û�������������е�½�����ܽ��������ܽ��棻
         */
        if (isRunning()) {
            /**
             * ������ں�̨����״̬������Ҫȡ����֪ͨ���е���С��ͼ�꣬ ���ҽ���½����ֱ������������Ҫ�ٴ����룬����ֱ�ӽ��������ܽ��棻
             */
            this.finish();
            Main.notification.cancelNotification();
        }

        /********** 20140910 ��Ӻ͸������´��룬ÿ�ε�¼�Ȳ�ѯ����sharedPrefernces,��ʼ **********/
        sharedPreferences = this.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setContentView(R.layout.activity_login);
        initView();

        spinnerDataAdapt();

        // ��sharedPreferences�ļ��л�ȡ���������
        String nameContent = sharedPreferences.getString("username", "");
        String pwdContent = sharedPreferences.getString("password", "");
        int position = sharedPreferences.getInt("position", 0);
        Util.userID = sharedPreferences.getString("userID", "");
        Util.phoneID = sharedPreferences.getString("phoneID", "");
        // �ж��Ƿ������ݴ��ڲ�������Ӧ����
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
        /********************************* ���� *********************************/
        // �����½��ť
        loginBtnClick();
    }

    /**
     * @date 20140910
     * @content �Ż����� ��ʼ����ͼ�ؼ�
     */
    public void initView() {
        username = (Spinner) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.psw);
        login = (Button) findViewById(R.id.login);
        aotoLogin = (CheckBox) findViewById(R.id.login_check1);

        // Ĭ�ϼ�ס�û���
        // username.setText(sharedPreferences.getString("userName", ""));
    }

    /**
     * @date 20140910
     * @content �Ż����� spinnerDataAdapt()-- ��spinner�������ݵĴ��뵥����ȡ����������
     */
    public void spinnerDataAdapt() {
        // ���û������û�����б�����ӱ����û���Ϣ������������б�����ݿⷽʽ����spinner�ؼ�������Դ
        DBManager dbManager = new DBManager(Login.this);
        dbManager.openDatabase();
        localUserNameList = dbManager.getLocalUserNameList();
        dbManager.closeDatabase();
        // Ϊ�����б���������,�õ�ǰ��������б���localUserNameList��Ϊ�������������Դ��
        adapter = new ArrayAdapter<String>(Login.this,
                android.R.layout.simple_spinner_item, localUserNameList);
        // Ϊ���������������б�����ʱ��������ʽ
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // ����������ӵ������б���
        username.setAdapter(adapter);
        // Ϊspinner�ؼ�������ʾ��
        username.setPrompt("��ѡ���û�����");
    }

    /**
     * @date 20140912
     * @content �Ż����� �������½��ť�¼���ȡ����
     */
    public void loginBtnClick() {
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                String username_tmp = username.getSelectedItem().toString();
                String psw_tmp = password.getText().toString();
                int position_tmp = username.getPositionForView(username);

                // ��½ͨ��������֤����ת��������
                if (validate(username_tmp, psw_tmp)) {
                    // �����û����ڱ����ҵ�user_id������user_idд��xml�ļ�
                    DBManager dbManager = new DBManager(Login.this);
                    dbManager.openDatabase();
                    String userID_tmp = dbManager
                            .queryUserIDByName(username_tmp);
                    dbManager.closeDatabase();
                    Util.userID = userID_tmp;

                    /********************************* ��ʼ *********************************/
                    /**
                     * @date 20140911
                     * @content ��һ��ѡ���admin�û�����½�����û��������롢userID��
                     *          phoneID�����ڱ���sharedPreferences��
                     * 
                     */
                    String nameContent = username_tmp;
                    String pwdContent = psw_tmp;
                    int positionContent = position_tmp;
                    String userIDContent = userID_tmp;

                    if (aotoLogin.isChecked()) {
                        // ���Ҫ���������
                        editor.putString("username", nameContent);
                        editor.putString("password", pwdContent);
                        editor.putInt("position", positionContent);
                        editor.putString("userID", userIDContent);
                        // ȷ�ϱ��沢�ύ
                        // editor.putBoolean("AUTO_ISCHECK", true).commit();
                        editor.commit();
                    }
                    /********************************* ���� *********************************/

                    Intent intent = new Intent(Login.this, Main.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    startActivity(intent);
                    Login.this.finish();
                }
                else {
                    // δͨ����֤,������������
                    password.setText("");
                }
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == Const.NO_SERVER) {
                Toast.makeText(Login.this, "��ָ���ķ�����δ��������", Toast.LENGTH_LONG)
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
        // //360WiFi����

        // SERVER_IP = config_preferences.getString("server_ip",
        // "221.207.25.161"); //����

        Thread queryPhoneID = new Thread() {
            @Override
            public void run() {
                // ����ֻ���ţ����ڱ���xml�ļ�
                // ��androidID����mac???
                String mac = Forest.getAndroidID(Login.this);
                String phoneID = ResolveXML.getPhoneID(mac);
                /*********************** 20140912 ������´��� ****************************/
                if (phoneID != null && !phoneID.equals("null")) {
                    // ����������ȡ��phoneID�쳣�������²�ѯ
                    if (phoneID.equals("netexception")
                            || phoneID.equals("xxxxxxxx")) {
                        phoneID = ResolveXML.getPhoneID(mac);
                    }
                    else {
                        // �����ȡ��phoneID����������뱾��sharedPreferences��
                        String phoneIDContent = phoneID;
                        editor.putString("phoneID", phoneIDContent);
                        editor.commit();
                    }
                }
                /*********************** 20140912 ע�͵����´��� ****************************/
                // if (phoneID != null && !phoneID.equals("null")) {
                // if (phoneID.equals("netexception")
                // || phoneID.equals("xxxxxxxx"))// �޸�������20140630
                // {
                // handler.sendEmptyMessage(Const.NO_SERVER);
                // // editor.putString("phoneID", "xxxxxxxx");
                //
                // // ////////////20140630ע������Ĵ���
                // // Util.phoneID = "xxxxxxxx";
                // // ////////��ӵĴ���20140630��ʼ////////////////////
                // String xmlphoneID = ResolveXML
                // .getXMLPhoneID(Login.this);
                // Util.phoneID = xmlphoneID;
                //
                // // ////////��ӵĴ���20140630����////////////////////
                // } else {
                // // editor.putString("phoneID", phoneID);
                // Util.phoneID = phoneID;
                // // ////////��ӵĴ���20140631��ʼ////////////////////
                // // �޸�xml�ļ��е�phoneID
                // // ����һ������
                // String xmlphoneID = ResolveXML
                // .getXMLPhoneID(Login.this);
                // if (!xmlphoneID.equals(phoneID))
                // ResolveXML.setXMLPhoneID(Login.this, phoneID);
                // // ////////��ӵĴ���20140631����////////////////////
                // }
                // // editor.commit();
                // }
            };
        };

        /**
         * @date 20140911 �޸� �������Ϊ�����������״��������ǰ�����Ƿ���ã�
         *       �����Ǽ���û��Ƿ�����������ӣ���������£����ֻ��GPRS�� ֻҪGPRS�źŲ��þͻᵯ�����ѿ�
         */
        if (!Forest.isNetConnect(Login.this)) {
            showDialogLogin("�ף�������������״��~");
        }
        else {
            queryPhoneID.start();
        }

    }

    // ��½��֤�������ɹ�����true�����򷵻�false
    public boolean validate(String name, String passwrd) {
        if (name == null || name.trim().equals("")) {
            showDialogLogin("��ѡ���û�����");
            return false;
        }
        else if (passwrd == null || passwrd.trim().equals("")) {
            showDialogLogin("���������룡");
            return false;
        }
        // �����������ݿ��и��û��Ƿ����
        DBManager dbManager = new DBManager(Login.this);
        dbManager.openDatabase();
        if (dbManager.isUserValid(name, passwrd)) {
            dbManager.closeDatabase();
            return true;
        }
        else {
            dbManager.closeDatabase();
            showDialogLogin("�û���������������������룡");
            return false;
        }
    }

    // ��½��֤��ʾ�Ի���
    public void showDialogLogin(String msg) {
        new AlertDialog.Builder(Login.this)
                .setIcon(android.R.drawable.btn_star_big_on).setTitle("��ʾ")
                .setMessage(msg).setCancelable(false)
                .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                }).create().show();
    }

    // �ж�����Ƿ��ڴ�״̬��ͨ���ж������б����Ƿ��Ѿ�����Main����
    // isRunning()���������ж��������Ƿ��Ǵ��ں�̨����״̬��ʵ��ԭ���ж�ϵͳ��̨����������Ƿ�
    // ����������Ӧ���������ƣ�����У���˵���������������ں�̨���У����û����˵��������û�����У�
    // ����������ں�̨����ʱ���ֻ���Ļ���Ϸ�������С����ͼ����֣�
    private boolean isRunning() {
        // ����ϵͳ��ǰ�������е����������
        int intGetTaskCounter = 30;
        List<RunningTaskInfo> list = activityManager
                .getRunningTasks(intGetTaskCounter);
        for (RunningTaskInfo task : list) {
            // task.baseActivity��ʾϵͳ��̨�д˽����������У�
            // task.topActivity��ʾϵͳ��ǰ�������е�Activity
            // �˴�getClassName()���Ի���getPackageName()��
            if (task.baseActivity.getClassName().equals(
                    "com.example.forest.activity.Main"))
                return true;
        }
        return false;
    }
}
