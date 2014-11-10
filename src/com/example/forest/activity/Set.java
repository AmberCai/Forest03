package com.example.forest.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.forest.R;
import com.example.forest.bean.UserInfo;
import com.example.forest.dialog.NotificationExtend;
import com.example.forest.util.Const;
import com.example.forest.util.CurrentVersion;
import com.example.forest.util.DBManager;
import com.example.forest.util.Forest;
import com.example.forest.util.ResolveXML;

public class Set extends Activity {

    EditText ip;
    Spinner timeCycle;

    long last_updateUserTime, last_updatePestsTime;
    final long dt_standard = 30;

    Button yesbtn, updateUser, updatePests, cancelAutoLogin;
    ImageButton update;
    EditText androidID;

    int[] cycl = { 1, 2, 5, 10, 30, 60, 120 };
    String[] cycle = { "1����", "2����", "3����", "10����", "30����", "60����", "120����" };

    // SharedPreferences config_preferences;
    // SharedPreferences.Editor editor;

    /************ ��С������̨ *********************************/
    NotificationExtend notification;

    /************ ��ȡ�������������û��߳� *********************************/
    Thread getUser_thread;

    /************ ��÷����������еĲ��溦������Ϣ�߳� *************************/
    Thread getPests_thread;

    // �汾���
    int newVerCode;
    String newVerName;
    String appName = "newVersion";

    ProgressDialog pBar;
    Handler handler;

    String downPath = "http://" + Login.SERVER_IP
            + ":8080/AndroidServer/version.xml";
    String appVersion = "";
    boolean has_newVersion = false;

    ActivityManager activityManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        ip = (EditText) findViewById(R.id.ip);
        ip.setEnabled(false);

        appName = CurrentVersion.getAppName(Set.this);

        timeCycle = (Spinner) findViewById(R.id.timecycle);
        timeCycle.setEnabled(false);

        yesbtn = (Button) findViewById(R.id.yesbtn);
        update = (ImageButton) findViewById(R.id.update);

        updateUser = (Button) findViewById(R.id.updateuser);
        updatePests = (Button) findViewById(R.id.updatepests);
        /**************** 20140912 ���ӡ�ȡ���Զ���¼����ť ��ʼ ***********************/
        cancelAutoLogin = (Button) findViewById(R.id.cancelAutoLogin);
        /**************** 20140912 ���ӡ�ȡ���Զ���¼����ť ���� ***********************/

        androidID = (EditText) findViewById(R.id.androidID);
        androidID.setText(Forest.getAndroidID(Set.this));
        androidID.setEnabled(false);

        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        // config_preferences = getSharedPreferences("config",
        // MODE_WORLD_WRITEABLE);
        // editor = config_preferences.edit();

        // ip.setText(config_preferences.getString("server_ip",
        // Login.SERVER_IP));
        ip.setText(Login.SERVER_IP);

        yesbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(Set.this, Main.class);
                i.setFlags(i.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(i);
                i = null;
                Set.this.finish();

            }
        });

        timeCycle.setAdapter(new ArrayAdapter<String>(Set.this,
                android.R.layout.simple_spinner_item, cycle));

        // int t = config_preferences.getInt("cycle", 5);
        // int poi = getPosition(cycl, t);
        timeCycle.setSelection(2);

        // ��ȡ�ֻ����,���е��û�
        getUser_thread = new Thread() {
            @Override
            public void run() {
                // ��ȡ�������������ն��û�
                List<UserInfo> userList = ResolveXML.getAllUser();
                System.out.println("userList:  " + userList.get(0));
                if (userList != null && userList.size() > 0) {
                    DBManager dbManager = new DBManager(Set.this);
                    dbManager.openDatabase();
                    dbManager.deleteAllLocalUser();
                    for (int i = 0; i < userList.size(); i++) {
                        dbManager.insert_User(userList.get(i));
                    }
                    dbManager.closeDatabase();
                    System.out.println("�����û��б����ݿ����");
                }
            }
        };

        // ��ȡ���溦����
        getPests_thread = new Thread() {
            @Override
            public void run() {
                String[] pestsDetail = ResolveXML.getValue();
                String pestsDetail_lastTime_tmpt;
                if (pestsDetail != null) {
                    pestsDetail_lastTime_tmpt = pestsDetail[10];
                    DBManager dbManager = new DBManager(Set.this);
                    dbManager.openDatabase();
                    String lastTime = dbManager.query_LastTime();

                    System.out
                            .println("����������ʱ��Ϊ:" + pestsDetail_lastTime_tmpt);
                    System.out.println("����������ʱ��Ϊ��" + lastTime);
                    if (lastTime == null
                            || !lastTime.equals(pestsDetail_lastTime_tmpt)) {
                        String kindsName = pestsDetail[0];
                        String kindsNumber = pestsDetail[1];

                        // �����ȡ����
                        System.out.println("��ȡ��kindsName: " + kindsName
                                + ",��õı�ţ�" + kindsNumber);

                        String stageName = pestsDetail[2];
                        String stageNumber = pestsDetail[3];

                        String amountName = pestsDetail[4];
                        String amountNumber = pestsDetail[5];

                        String levelName = pestsDetail[6];
                        String levelNumber = pestsDetail[7];

                        String adviseName = pestsDetail[8];
                        String adviseNumber = pestsDetail[9];

                        dbManager.insert_pestsKinds(kindsNumber, kindsName);
                        dbManager.insert_pestsStage(stageNumber, stageName);
                        dbManager.insert_pestsAmount(amountNumber, amountName);
                        dbManager.insert_pestsLevel(levelNumber, levelName);
                        dbManager.insert_pestsAdvise(adviseName, adviseNumber);
                        dbManager.insert_LastTime(pestsDetail_lastTime_tmpt);
                    }
                    dbManager.closeDatabase();
                }
            }
        };

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                update.setVisibility(View.GONE);
                try {
                    showUpdateDialog();
                }
                catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        updateUser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                long curTime = System.currentTimeMillis();

                long dt = (curTime - last_updateUserTime) / 1000;

                if (last_updateUserTime == 0 || dt > dt_standard) {
                    if (Forest.isNetConnect(Set.this)) {
                        getUser_thread.start();
                    }
                    last_updateUserTime = curTime;
                }
            }
        });

        updatePests.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                long curTime = System.currentTimeMillis();

                long dt = (curTime - last_updatePestsTime) / 1000;

                if (last_updateUserTime == 0 || dt > dt_standard) {
                    if (Forest.isNetConnect(Set.this)) {
                        getPests_thread.start();
                    }
                    last_updatePestsTime = curTime;
                }
            }
        });

        /**************** 20140912 ���ӡ�ȡ���Զ���¼����ť ��ʼ ***********************/
        // �򿪱����Ѿ����ڵ�sharedPreferences�ļ�
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                "saveContent", Context.MODE_PRIVATE);
        final Editor editor = sharedPreferences.edit();
        cancelAutoLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // cancelAutoLogin_thread.start();
                // �����ѱ��������
                editor.putString("username", "");
                editor.putString("password", "");
                editor.putInt("position", 0);
                // ȷ�ϱ��沢�ύ
                editor.commit();
            }
        });
        /**************** 20140912 ���ӡ�ȡ���Զ���¼����ť ���� ***********************/

        notification = new NotificationExtend(Set.this);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == Const.DOWNFINISH) {
                    // �������ʱҪ���������Ի���ȡ�����������Ƿ�װ��Ӧ�õ���ʾ
                    pBar.cancel();

                    // �����������ʾ�Ƿ�װ�µİ汾
                    Dialog installDialog = new AlertDialog.Builder(Set.this)
                            .setTitle("�������")
                            .setMessage("�Ƿ�װ�µ�Ӧ��")
                            .setPositiveButton("ȷ��",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            installNewApk();
                                            finish();
                                        }
                                    })
                            .setNegativeButton("ȡ��",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            // TODO Auto-generated method stub
                                        }
                                    }).create();
                    installDialog.show();
                }
                else if (msg.what == Const.NEWVERSION) {
                    // ������⣬�Ƿ����µİ汾��������µİ汾���������Ͻ���ʾNEWͼ�꣬��������ʾ
                    if (has_newVersion) {
                        // update.setImageResource(R.drawable.app_new);
                        update.setVisibility(View.VISIBLE);

                    }
                    else {
                        update.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Toast.makeText(Set.this, "��ǰʹ��������"+Forest.liuliang/1024+" K",
        // Toast.LENGTH_LONG).show();
        Thread newVer_Thread = new Thread() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                super.run();

                try {
                    has_newVersion = hasServerVersion();
                    newVerName = getNewVersionURL();

                    if (has_newVersion) {
                        handler.sendEmptyMessage(Const.NEWVERSION);
                    }

                }
                catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        if (Forest.isNetConnect(Set.this)) {
            newVer_Thread.start();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        // TODO Auto-generated method stub
        super.onUserLeaveHint();
        notification.showNotification();
        moveTaskToBack(true);
    }

    // ���ӷ�����version.xml��õ��ַ���������������Ҫ�İ汾��Ϣ
    private boolean hasServerVersion() throws NameNotFoundException {
        newVerCode = getNewVersionCode();
        if (newVerCode > CurrentVersion.getVerCode(Set.this))
            return true;
        else
            return false;
    }

    // �õ��°汾�İ汾��
    public int getNewVersionCode() {
        int newVersion = 1;
        String urlStr = "http://" + Login.SERVER_IP
                + ":8080/AndroidServer/version.xml";
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            String str = doc.getElementsByTagName("version").item(0)
                    .getFirstChild().getNodeValue();
            newVersion = Integer.parseInt(str);
        }
        catch (Exception e) {
            System.out.println(e.getClass().getName());
            e.printStackTrace();
        }
        return newVersion;
    }

    public String getNewVersionURL() {
        String result = "";
        String urlStr = "http://" + Login.SERVER_IP
                + ":8080/AndroidServer/version.xml";

        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            result = doc.getElementsByTagName("url").item(0).getFirstChild()
                    .getNodeValue();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // ��ʾ������ʾ��
    private void showUpdateDialog() throws NameNotFoundException {
        StringBuffer sb = new StringBuffer();
        sb.append("��ǰ�汾��");
        sb.append(CurrentVersion.getVerName(this) + "\n");
        sb.append("VerCode:");
        sb.append(CurrentVersion.getVerCode(this));
        sb.append("\n");
        sb.append("�����°汾��");
        sb.append(newVerName);
        sb.append("\nNewVerCode:");
        sb.append(newVerCode);
        sb.append("\n");
        sb.append("�Ƿ���£�");

        Dialog dialog = new AlertDialog.Builder(Set.this)
                .setTitle("�������")
                .setMessage(sb.toString())
                .setPositiveButton("����", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        showProgressBar();
                    }
                })
                .setNegativeButton("�ݲ�����",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // TODO Auto-generated method stub

                            }
                        }).create();
        dialog.show();

    }

    protected void showProgressBar() {
        pBar = new ProgressDialog(Set.this);
        pBar.setTitle("��������");
        pBar.setMessage("���Ժ�...");
        pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (Forest.isNetConnect(Set.this)) {
            downAppFile(getNewVersionURL());
        }
    }

    protected void downAppFile(final String urlStr) {
        pBar.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("apk����·����" + newVerName);
                    URL url = new URL("http://" + newVerName);
                    HttpURLConnection urlConn = (HttpURLConnection) url
                            .openConnection();
                    urlConn.setConnectTimeout(5000);
                    int size = urlConn.getContentLength();
                    System.out.println("apk�ļ���С��" + size + "�ֽ�");
                    InputStream inputStream = urlConn.getInputStream();
                    File file = new File(
                            Environment.getExternalStorageDirectory()
                                    + "/forest/newVersion.apk");
                    OutputStream output = new FileOutputStream(file);
                    byte buffer[] = new byte[4 * 1024];
                    int temp;
                    while ((temp = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, temp);
                        System.out.println("д��4k");
                    }
                    output.flush();
                    handler.sendEmptyMessage(Const.DOWNFINISH);

                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    System.out.println("apk�����쳣��" + e.getClass().getName());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // ��װ�µ�Ӧ��
    protected void installNewApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
                Uri.fromFile(new File(Environment.getExternalStorageDirectory()
                        + "/forest/newVersion.apk")),
                "application/vnd.android.package-archive");
        intent.setFlags(intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // ��������
        // editor.putInt("cycle", cycl[timeCycle.getSelectedItemPosition()]);
        // editor.commit();

        // boolean isIP = Util.isIpv4(ip.getText().toString());
        // if(isIP==false)
        // {
        // new
        // AlertDialog.Builder(Set.this).setTitle("��ʾ").setMessage("IP��ַ���Ϸ������������룡")
        // .setCancelable(false)
        // .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        // // TODO Auto-generated method stub
        // dialog.dismiss();
        // ip.setText("");
        // }
        // }).create().show();
        // }
        // else
        // {
        // editor.putString("server_ip", ip.getText().toString());
        // editor.commit();
        // }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ��������
        // editor.putInt("cycle", cycl[timeCycle.getSelectedItemPosition()]);
        // editor.commit();

        // boolean isIP = Util.isIpv4(ip.getText().toString());
        // if(isIP==false)
        // {
        // new
        // AlertDialog.Builder(Set.this).setTitle("��ʾ").setMessage("IP��ַ���Ϸ������������룡")
        // .setCancelable(false)
        // .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        // // TODO Auto-generated method stub
        // dialog.dismiss();
        // ip.setText("");
        // }
        // }).create().show();
        // }
        // else
        // {
        // editor.putString("server_ip", ip.getText().toString());
        // editor.commit();
        // }
    }

    public int getPosition(int[] a, int value) {
        int poi = 2;
        for (int i = 0; i < a.length; i++) {
            if (value == a[i]) {
                poi = i;
            }
        }
        return poi;
    }
}
