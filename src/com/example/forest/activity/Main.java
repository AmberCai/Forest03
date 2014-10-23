package com.example.forest.activity;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eton.czh.GifView;
import com.example.forest.R;
import com.example.forest.bean.Location;
import com.example.forest.dialog.MsgListDialog;
import com.example.forest.dialog.NotificationExtend;
import com.example.forest.dialog.ProgressBar;
import com.example.forest.service.SendLocationService;
import com.example.forest.util.Const;
import com.example.forest.util.DBManager;
import com.example.forest.util.Forest;
import com.example.forest.util.Util;

public class Main extends Activity implements OnClickListener {

    /*********** 主界面框架 *********************************************/
    private GridView gridView;
    private LinearLayout gridviewLinear;
    private TextView curLocation, curAltitude;
    Timer timer = new Timer();

    /*********** 来自服务器的消息公告栏 **********************************/
    LinearLayout linearLayout;
    TextView msgcontent, msgnum;
    int count = 1;
    GifView notice;
    MsgListDialog msgListDialog; // 消息列表的显示对话框

    /************ 进度条对话框 ******************************************/
    ProgressBar progressDialog = null;

    /************ 定时发送位置信息任务启动 ******************************/
    // SharedPreferences config_preferences;
    // SharedPreferences.Editor editor;
    AlarmManager aManager;

    /************ 上下班按钮 *******************************************/
    Button shangban, xiaban;

    /************ 最小化，后台 *********************************/
    public static NotificationExtend notification;
    String lastNotification = null;
    int lastnotify_num = 0;

    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        /**
         * @date 20140911
         * @content 注释以下代码，“请打开数据连接”的提醒只在登陆界面显示，以后不做显示
         */
        // if (!Forest.isNetConnect(Main.this)) {
        // showDialogMsg("请打开数据连接！");
        // }

        notifivationTimerTask();

        msglistDismissListener();

        sendLocationTask();

    }

    public void initView() {

        gridviewLinear = (LinearLayout) findViewById(R.id.gridlinear);
        gridView = (GridView) findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(this);
        gridView.setAdapter(adapter);
        gridviewLinear.getBackground().setAlpha(30);
        gridView.setNumColumns(3);

        curLocation = (TextView) findViewById(R.id.curlocation);
        curAltitude = (TextView) findViewById(R.id.curaltitude);

        shangban = (Button) findViewById(R.id.shangban);
        xiaban = (Button) findViewById(R.id.xiaban);
        // 设置通告栏的动态图片
        notice = (GifView) findViewById(R.id.notice);
        notice.setGifImage(R.drawable.laba);
        // 进度条对话框加载
        progressDialog = new ProgressBar(Main.this, R.style._ProgressBar);

        progressDialog.setContentView(R.layout.progress);

        notification = new NotificationExtend(Main.this);

        // 服务器消息通告栏
        linearLayout = (LinearLayout) findViewById(R.id.msgarea);
        msgcontent = (TextView) findViewById(R.id.msg_content);
        msgcontent.setText("暂时无最新通知！请随时注意最新通告，帮助您更好的工作！");
        msgnum = (TextView) findViewById(R.id.msg_num);
        msgnum.setText("0");
        // 通告消息显示框
        msgListDialog = new MsgListDialog(Main.this, R.style._MsgListDialog);

        shangban.setOnClickListener(this);
        xiaban.setOnClickListener(this);
        linearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.shangban:
                Util.shangbanFlag = true;
                shangban.setEnabled(false);
                xiaban.setEnabled(true);
                break;
            case R.id.xiaban:
                Util.shangbanFlag = false;
                xiaban.setEnabled(false);
                shangban.setEnabled(true);
                break;
            case R.id.msgarea:
                if (count > 0) {
                    msgListDialog.show();
                }
                break;
            default:
                break;
        }
    }

    public void msglistDismissListener() {
        msgListDialog.setOnDismissListener(new Dialog.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                Message msg = new Message();
                List<String> notify;
                DBManager dbManager = new DBManager(Main.this);
                dbManager.openDatabase();
                notify = dbManager.query_unreadMsg();
                dbManager.closeDatabase();

                if (notify.size() > 0) {
                    if (lastNotification == null) {
                        lastnotify_num = notify.size();
                        lastNotification = notify.get(notify.size() - 1);
                        msg.obj = notify.get(notify.size() - 1) + "#"
                                + notify.size();
                        msg.what = Const.NEW_MSG;
                        handler.sendMessage(msg);
                    }
                    if (!lastNotification.equals(notify.get(notify.size() - 1))
                            || lastnotify_num != notify.size()) {
                        lastNotification = notify.get(notify.size() - 1);
                        msg.obj = notify.get(notify.size() - 1) + "#"
                                + notify.size();
                        msg.what = Const.NEW_MSG;
                        handler.sendMessage(msg);
                    }
                }
            }
        });
    }

    public void notifivationTimerTask() {
        /**
         * @date 20140915 java.util.Timer.schedule(TimerTask task, long delay,
         *       long period)表示delay
         *       /1000秒后执行task,然后进过period/1000秒再次执行task，这个用于循环任务，执行无数次，当然
         *       ，你可以用timer.cancel();取消计时器的执行。
         */
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message newLocation_msg = new Message();
                newLocation_msg.what = Const.NEW_LOCATION;
                newLocation_msg.obj = Const.cur_location;
                handler.sendMessage(newLocation_msg);

                Message msg = new Message();
                List<String> notify;
                DBManager dbManager = new DBManager(Main.this);
                dbManager.openDatabase();
                notify = dbManager.query_unreadMsg();
                dbManager.closeDatabase();
                System.out.println("notify.size()" + notify.size());
                // 如果存在未读消息
                if (notify.size() > 0) {
                    // 还未指向最新的未读消息的实例
                    if (lastNotification == null) {
                        // 获取未读消息的数量
                        lastnotify_num = notify.size();
                        // 获取最新的未读消息
                        lastNotification = notify.get(notify.size() - 1);
                        // 将最新消息和未读消息数量通过消息机制传给主页面更新显示
                        msg.obj = notify.get(notify.size() - 1) + "#"
                                + notify.size();
                        msg.what = Const.NEW_MSG;
                        handler.sendMessage(msg);
                    }
                    // 如果最新的未读消息变量指向的实例不是数据库中最新的未读消息
                    // &&未读消息的数量变量指向的也不是实际数据库中未读消息的数量
                    if (!lastNotification.equals(notify.get(notify.size() - 1))
                            && lastnotify_num != notify.size()) {
                        // 将最新未读消息变量、未读消息的数量变量值更新，并通知主线程显示更新
                        lastNotification = notify.get(notify.size() - 1);
                        msg.obj = notify.get(notify.size() - 1) + "#"
                                + notify.size();
                        msg.what = Const.NEW_MSG;
                        handler.sendMessage(msg);
                    }
                }
            }
        }, 0, 3000);
    }

    public void sendLocationTask() {
        // 开启定时发送任务
        aManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
        Date now = new Date();

        intent = new Intent(Main.this, SendLocationService.class);
        final PendingIntent pi = PendingIntent.getService(Main.this, 0, intent,
                0);
        aManager.setRepeating(AlarmManager.RTC_WAKEUP, now.getTime(),
                3 * 60 * 1000, pi);
        now = null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // if(config_preferences.getBoolean("shangban", false))
        if (Util.shangbanFlag) {
            shangban.setEnabled(false);
            xiaban.setEnabled(true);
        }
        else {
            shangban.setEnabled(true);
            xiaban.setEnabled(false);
        }
    };

    @Override
    protected void onUserLeaveHint() {
        // TODO Auto-generated method stub
        super.onUserLeaveHint();
        notification.showNotification();
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            notification.showNotification();
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    // 登陆验证提示对话框
    public void showDialogMsg(String msg) {
        new AlertDialog.Builder(Main.this).setTitle("提示").setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
    }

    // 消息处理队列，来控制UI线程的界面实时更新显示
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Const.NEW_LOCATION) {
                // 经度
                curLocation.setText("E:" + ((Location) msg.obj).getLongitude());
                // 纬度
                curAltitude.setText("N:" + ((Location) msg.obj).getLatitude());
            }
            else if (msg.what == Const.SENDED) {
                progressDialog.cancel();
            }
            else if (msg.what == Const.NEW_MSG) {
                String tmp[] = ((String) msg.obj).split("#");
                msgcontent.setText(tmp[0]);
                msgnum.setText(tmp[1]);
            }
        }
    };

    // main页面中的6个图片对应6个监听器
    public class ImageAdapter extends BaseAdapter {
        private final Context context;

        private final int[] images = { R.drawable.orange_fire,
                R.drawable.orange_pests, R.drawable.orange_deforest,
                R.drawable.orange_set, R.drawable.orange_sos,
                R.drawable.orange_exit };

        public ImageAdapter(Context c) {
            this.context = c;
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(3, 10, 3, 10);
            }
            else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(images[position]);
            switch (position) {
                case 0:
                    imageView.setOnClickListener(fireReportListener);
                    break;
                case 1:
                    imageView.setOnClickListener(pestsReportListener);
                    break;
                case 2:
                    imageView.setOnClickListener(deforestationListener);
                    break;
                case 3:
                    imageView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(Main.this, Set.class);
                            i.setFlags(i.FLAG_ACTIVITY_NO_USER_ACTION);
                            startActivity(i);
                            i = null;
                        }
                    });
                    break;
                case 4:
                    imageView.setOnClickListener(sosListener);
                    break;
                case 5:
                    imageView.setOnClickListener(exitListener);
                    break;
                default:
                    break;
            }
            return imageView;
        }
    }

    // 火警监听器
    OnClickListener fireReportListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new Builder(Main.this);
            builder.setTitle("报告火情");
            builder.setMessage("您确认需要发送火情报告吗？");

            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            progressDialog.init(progressDialog, "正在发送定位信息。。。");
                            progressDialog.show();

                            // 获取定位信息，然后发送给服务器，（如果信号未发送，则将信息顺序存储在内存卡，
                            // 等待有信号后，自动重发，发送成功后檫除该条存储信息）存储方式SQLite
                            final Util util = new Util(Main.this);
                            final String msg = util.obtainLocation(Const.FIRE,
                                    Main.this);

                            Thread fire_thread = new Thread() {
                                @Override
                                public void run() {

                                    boolean sendsuc_flag = util
                                            .sendLocation(msg);

                                    if (sendsuc_flag == false) {
                                        DBManager dbManager = new DBManager(
                                                Main.this);
                                        dbManager.openDatabase();
                                        dbManager.insert_location(msg, 0);
                                        dbManager.closeDatabase();
                                    }
                                    handler.sendEmptyMessage(Const.SENDED);
                                };
                            };

                            if (Forest.isNetConnect(Main.this)) {
                                fire_thread.start();
                            }
                            else {
                                DBManager dbManager = new DBManager(Main.this);
                                dbManager.openDatabase();
                                dbManager.insert_location(msg, 0);
                                dbManager.closeDatabase();
                                handler.sendEmptyMessage(Const.SENDED);
                            }

                            AlertDialog.Builder builder = new Builder(Main.this);
                            builder.setTitle("拍照");
                            builder.setMessage("是否需要拍照？");

                            builder.setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            // 为了保证照片名和病虫害详情的日期、时间与定位信息一致，传递数据
                                            Intent i = new Intent(Main.this,
                                                    CaptureImage.class);
                                            Bundle data = new Bundle();
                                            data.putString("datetime",
                                                    util.loc_dat
                                                            + util.loc_time);
                                            data.putString("type", "A");
                                            i.putExtras(data);
                                            i.setFlags(i.FLAG_ACTIVITY_NO_USER_ACTION);
                                            startActivity(i);
                                            i = null;
                                        }
                                    });
                            builder.setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            builder.show();

                        }
                    });

            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();

        }
    };
    OnClickListener pestsReportListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new Builder(Main.this);
            builder.setTitle("报告病虫害");
            builder.setMessage("您确认需要发送病虫害报告吗？");

            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            progressDialog.init(progressDialog, "正在发送定位信息。。。");
                            progressDialog.show();
                            // 获取定位信息，然后发送给服务器，（如果信号未发送，则将信息顺序存储在内存卡，
                            // 等待有信号后，自动重发，发送成功后檫除该条存储信息）存储方式SQLite
                            final Util util = new Util(Main.this);
                            final String msg = util.obtainLocation(Const.PESTS,
                                    Main.this);
                            System.out.println("FireReport:------------" + msg);

                            Thread pests_thread = new Thread() {
                                @Override
                                public void run() {
                                    // 将病虫害定位信息发送至服务器端
                                    boolean sendsuc_flag = util
                                            .sendLocation(msg);
                                    // 若发送失败，则将定位信息顺序插入本地数据库
                                    if (sendsuc_flag == false) {
                                        DBManager dbManager = new DBManager(
                                                Main.this);
                                        dbManager.openDatabase();
                                        dbManager.insert_location(msg, 0);
                                        dbManager.closeDatabase();
                                    }
                                    handler.sendEmptyMessage(Const.SENDED);
                                };
                            };
                            if (Forest.isNetConnect(Main.this)) {
                                pests_thread.start();
                            }
                            // else语句与pest_thresd()逻辑重复？
                            else {
                                DBManager dbManager = new DBManager(Main.this);
                                dbManager.openDatabase();
                                dbManager.insert_location(msg, 0);
                                dbManager.closeDatabase();
                                handler.sendEmptyMessage(Const.SENDED);
                            }

                            AlertDialog.Builder builder = new Builder(Main.this);
                            builder.setTitle("拍照");
                            builder.setMessage("是否需要拍照？");

                            builder.setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            Bundle from = new Bundle();
                                            from.putBoolean("pests", true);
                                            Intent i = new Intent(Main.this,
                                                    CaptureImage.class);
                                            from.putString("datetime",
                                                    util.loc_dat
                                                            + util.loc_time);
                                            from.putString("type", "B");
                                            i.putExtras(from);
                                            i.setFlags(i.FLAG_ACTIVITY_NO_USER_ACTION);
                                            startActivity(i);
                                            i = null;
                                        }
                                    });
                            // 病虫害，若取消拍照，则在本地数据库中查看是否存在病虫害信息，若有，则直接转入pest——detail页面
                            builder.setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            DBManager dbManager = new DBManager(
                                                    Main.this);
                                            dbManager.openDatabase();
                                            boolean hasPestsInfo = dbManager
                                                    .haspestsKinds();
                                            dbManager.closeDatabase();
                                            if (hasPestsInfo) {
                                                Bundle from = new Bundle();
                                                from.putBoolean("pests", true);
                                                Intent i = new Intent(
                                                        Main.this,
                                                        PestsDetail.class);
                                                from.putString("datetime",
                                                        util.loc_dat
                                                                + util.loc_time);
                                                from.putString("type", "B");
                                                i.putExtras(from);
                                                i.setFlags(i.FLAG_ACTIVITY_NO_USER_ACTION);
                                                startActivity(i);
                                                i = null;
                                            }
                                        }
                                    });
                            builder.show();

                        }
                    });
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
    };
    OnClickListener deforestationListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new Builder(Main.this);
            builder.setTitle("报告滥伐");
            builder.setMessage("您确认需要发送滥砍滥伐报告吗？");

            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            progressDialog.init(progressDialog, "正在发送定位信息。。。");
                            progressDialog.show();
                            // 获取定位信息，然后发送给服务器，（如果信号未发送，则将信息顺序存储在内存卡，
                            // 等待有信号后，自动重发，发送成功后檫除该条存储信息）存储方式SQLite
                            final Util util = new Util(Main.this);
                            final String msg = util.obtainLocation(Const.CUT,
                                    Main.this);
                            System.out.println("FireReport:------------" + msg);

                            Thread deforest_thread = new Thread() {
                                @Override
                                public void run() {

                                    boolean sendsuc_flag = util
                                            .sendLocation(msg);

                                    if (sendsuc_flag == false) {
                                        DBManager dbManager = new DBManager(
                                                Main.this);
                                        dbManager.openDatabase();
                                        dbManager.insert_location(msg, 0);
                                        dbManager.closeDatabase();
                                    }
                                    handler.sendEmptyMessage(Const.SENDED);
                                };
                            };
                            if (Forest.isNetConnect(Main.this)) {
                                deforest_thread.start();
                            }
                            else {
                                DBManager dbManager = new DBManager(Main.this);
                                dbManager.openDatabase();
                                dbManager.insert_location(msg, 0);
                                dbManager.closeDatabase();
                                handler.sendEmptyMessage(Const.SENDED);
                            }

                            AlertDialog.Builder builder = new Builder(Main.this);
                            builder.setTitle("拍照");
                            builder.setMessage("是否需要拍照？");

                            builder.setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            Bundle from = new Bundle();
                                            Intent i = new Intent(Main.this,
                                                    CaptureImage.class);
                                            from.putString("datetime",
                                                    util.loc_dat
                                                            + util.loc_time);
                                            from.putString("type", "C");
                                            i.putExtras(from);
                                            i.setFlags(i.FLAG_ACTIVITY_NO_USER_ACTION);
                                            startActivity(i);
                                            i = null;
                                        }
                                    });
                            builder.setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            /**
                                             * @date 20141017
                                             * @content 增加消除提醒对话框
                                             */
                                            dialog.dismiss();
                                        }
                                    });
                            builder.show();
                        }
                    });
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
    };

    OnClickListener sosListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new Builder(Main.this);
            builder.setTitle("一键呼救");
            builder.setMessage("您确认请求救助吗？");

            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            progressDialog.init(progressDialog, "正在发送定位信息。。。");
                            progressDialog.show();

                            // 获取定位信息，然后发送给服务器，（如果信号未发送，则将信息顺序存储在内存卡，
                            // 等待有信号后，自动重发，发送成功后檫除该条存储信息）存储方式SQLite
                            final Util util = new Util(Main.this);
                            final String msg = util.obtainLocation(Const.SOS,
                                    Main.this);
                            System.out.println("FireReport:------------" + msg);

                            Thread deforest_thread = new Thread() {
                                @Override
                                public void run() {

                                    boolean sendsuc_flag = util
                                            .sendLocation(msg);

                                    if (sendsuc_flag == false) {
                                        DBManager dbManager = new DBManager(
                                                Main.this);
                                        dbManager.openDatabase();
                                        dbManager.insert_location(msg, 0);
                                        dbManager.closeDatabase();
                                    }
                                    handler.sendEmptyMessage(Const.SENDED);
                                };
                            };
                            if (Forest.isNetConnect(Main.this)) {
                                deforest_thread.start();
                            }
                            else {
                                DBManager dbManager = new DBManager(Main.this);
                                dbManager.openDatabase();
                                dbManager.insert_location(msg, 0);
                                dbManager.closeDatabase();
                                handler.sendEmptyMessage(Const.SENDED);
                            }

                        }
                    });
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
    };

    OnClickListener exitListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new Builder(Main.this);
            builder.setTitle("退出");
            builder.setMessage("确认退出？");

            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopService(intent);
                            Main.this.finish();

                            android.os.Process.killProcess(android.os.Process
                                    .myPid());
                            System.exit(0);
                        }
                    });
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
    };

}
