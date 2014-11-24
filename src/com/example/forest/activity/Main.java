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

    /*********** �������� *********************************************/
    private GridView gridView;
    private LinearLayout gridviewLinear;
    private TextView curLocation, curAltitude;
    Timer timer = new Timer();

    /*********** ���Է���������Ϣ������ **********************************/
    LinearLayout linearLayout;
    TextView msgcontent, msgnum;
    int count = 1;
    GifView notice;
    MsgListDialog msgListDialog; // ��Ϣ�б����ʾ�Ի���

    /************ �������Ի��� ******************************************/
    ProgressBar progressDialog = null;

    /************ ��ʱ����λ����Ϣ�������� ******************************/
    // SharedPreferences config_preferences;
    // SharedPreferences.Editor editor;
    AlarmManager aManager;

    /************ ���°ఴť *******************************************/
    Button shangban, xiaban;

    /************ ��С������̨ *********************************/
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
         * @content ע�����´��룬������������ӡ�������ֻ�ڵ�½������ʾ���Ժ�����ʾ
         */
        // if (!Forest.isNetConnect(Main.this)) {
        // showDialogMsg("����������ӣ�");
        // }

        notifivationTimerTask();

        msglistDismissListener();

        sendLocationTask();

        shangban.setOnClickListener(this);
        xiaban.setOnClickListener(this);
        linearLayout.setOnClickListener(this);
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
        // ����ͨ�����Ķ�̬ͼƬ
        notice = (GifView) findViewById(R.id.notice);
        notice.setGifImage(R.drawable.laba);
        // �������Ի������
        progressDialog = new ProgressBar(Main.this, R.style._ProgressBar);

        progressDialog.setContentView(R.layout.progress);

        notification = new NotificationExtend(Main.this);

        // ��������Ϣͨ����
        linearLayout = (LinearLayout) findViewById(R.id.msgarea);
        msgcontent = (TextView) findViewById(R.id.msg_content);
        msgcontent.setText("��ʱ������֪ͨ������ʱע������ͨ�棬���������õĹ�����");
        msgnum = (TextView) findViewById(R.id.msg_num);
        msgnum.setText("0");
        // ͨ����Ϣ��ʾ��
        msgListDialog = new MsgListDialog(Main.this, R.style._MsgListDialog);

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
         *       long period)��ʾdelay
         *       /1000���ִ��task,Ȼ�����period/1000���ٴ�ִ��task���������ѭ������ִ�������Σ���Ȼ
         *       ���������timer.cancel();ȡ����ʱ����ִ�С�
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
                // �������δ����Ϣ
                if (notify.size() > 0) {
                    // ��δָ�����µ�δ����Ϣ��ʵ��
                    if (lastNotification == null) {
                        // ��ȡδ����Ϣ������
                        lastnotify_num = notify.size();
                        // ��ȡ���µ�δ����Ϣ
                        lastNotification = notify.get(notify.size() - 1);
                        // ��������Ϣ��δ����Ϣ����ͨ����Ϣ���ƴ�����ҳ�������ʾ
                        msg.obj = notify.get(notify.size() - 1) + "#"
                                + notify.size();
                        msg.what = Const.NEW_MSG;
                        handler.sendMessage(msg);
                    }
                    // ������µ�δ����Ϣ����ָ���ʵ���������ݿ������µ�δ����Ϣ
                    // &&δ����Ϣ����������ָ���Ҳ����ʵ�����ݿ���δ����Ϣ������
                    if (!lastNotification.equals(notify.get(notify.size() - 1))
                            && lastnotify_num != notify.size()) {
                        // ������δ����Ϣ������δ����Ϣ����������ֵ���£���֪ͨ���߳���ʾ����
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
        // ������ʱ��������
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

    // ��½��֤��ʾ�Ի���
    public void showDialogMsg(String msg) {
        new AlertDialog.Builder(Main.this).setTitle("��ʾ").setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
    }

    // ��Ϣ������У�������UI�̵߳Ľ���ʵʱ������ʾ
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Const.NEW_LOCATION) {
                // ����
                curLocation.setText("E:" + ((Location) msg.obj).getLongitude());
                // γ��
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

    // mainҳ���е�6��ͼƬ��Ӧ6��������
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
                // ʵ����imageView����
                imageView = new ImageView(context);
                // ����ImageView���󲼾�
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                // ���ÿ̶�����
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // ���ü��
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

    // �𾯼�����
    OnClickListener fireReportListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new Builder(Main.this);
            builder.setTitle("�������");
            builder.setMessage("��ȷ����Ҫ���ͻ��鱨����");

            builder.setPositiveButton("ȷ��",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            progressDialog.init(progressDialog, "���ڷ��Ͷ�λ��Ϣ������");
                            progressDialog.show();

                            // ��ȡ��λ��Ϣ��Ȼ���͸���������������ź�δ���ͣ�����Ϣ˳��洢���ڴ濨��
                            // �ȴ����źź��Զ��ط������ͳɹ����߳������洢��Ϣ���洢��ʽSQLite
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
                            builder.setTitle("����");
                            builder.setMessage("�Ƿ���Ҫ���գ�");

                            builder.setPositiveButton("ȷ��",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            // Ϊ�˱�֤��Ƭ���Ͳ��溦��������ڡ�ʱ���붨λ��Ϣһ�£���������
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
                            builder.setNegativeButton("ȡ��",
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

            builder.setNegativeButton("ȡ��",
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
            builder.setTitle("���没�溦");
            builder.setMessage("��ȷ����Ҫ���Ͳ��溦������");

            builder.setPositiveButton("ȷ��",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            progressDialog.init(progressDialog, "���ڷ��Ͷ�λ��Ϣ������");
                            progressDialog.show();
                            // ��ȡ��λ��Ϣ��Ȼ���͸���������������ź�δ���ͣ�����Ϣ˳��洢���ڴ濨��
                            // �ȴ����źź��Զ��ط������ͳɹ����߳������洢��Ϣ���洢��ʽSQLite
                            final Util util = new Util(Main.this);
                            final String msg = util.obtainLocation(Const.PESTS,
                                    Main.this);
                            System.out.println("FireReport:------------" + msg);

                            Thread pests_thread = new Thread() {
                                @Override
                                public void run() {
                                    // �����溦��λ��Ϣ��������������
                                    boolean sendsuc_flag = util
                                            .sendLocation(msg);
                                    // ������ʧ�ܣ��򽫶�λ��Ϣ˳����뱾�����ݿ�
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
                            // else�����pest_thresd()�߼��ظ���
                            else {
                                DBManager dbManager = new DBManager(Main.this);
                                dbManager.openDatabase();
                                dbManager.insert_location(msg, 0);
                                dbManager.closeDatabase();
                                handler.sendEmptyMessage(Const.SENDED);
                            }

                            AlertDialog.Builder builder = new Builder(Main.this);
                            builder.setTitle("����");
                            builder.setMessage("�Ƿ���Ҫ���գ�");

                            builder.setPositiveButton("ȷ��",
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
                            /**
                             * @date 20141024
                             * @content 
                             *          ���溦����ȡ�����գ����ڱ������ݿ��в鿴�Ƿ���ڲ��溦��Ϣ�����У���ֱ��ת��pest
                             *          ���� detailҳ��
                             * @notes:��תҳ��ʱע�⴫�ݵ�Bundleֵ�����͡�����������յĶ�Ӧ
                             */
                            builder.setNegativeButton("ȡ��",
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
            builder.setNegativeButton("ȡ��",
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
            builder.setTitle("�����ķ�");
            builder.setMessage("��ȷ����Ҫ�����Ŀ��ķ�������");

            builder.setPositiveButton("ȷ��",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            progressDialog.init(progressDialog, "���ڷ��Ͷ�λ��Ϣ������");
                            progressDialog.show();
                            // ��ȡ��λ��Ϣ��Ȼ���͸���������������ź�δ���ͣ�����Ϣ˳��洢���ڴ濨��
                            // �ȴ����źź��Զ��ط������ͳɹ����߳������洢��Ϣ���洢��ʽSQLite
                            final Util util = new Util(Main.this);
                            final String msg = util.obtainLocation(Const.CUT,
                                    Main.this);
                            System.out
                                    .println("deforestationReport:------------"
                                            + msg);

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
                            builder.setTitle("����");
                            builder.setMessage("�Ƿ���Ҫ���գ�");

                            builder.setPositiveButton("ȷ��",
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
                            builder.setNegativeButton("ȡ��",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            /**
                                             * @date 20141017
                                             * @content �����������ѶԻ���
                                             */
                                            dialog.dismiss();
                                        }
                                    });
                            builder.show();
                        }
                    });
            builder.setNegativeButton("ȡ��",
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
            builder.setTitle("һ������");
            builder.setMessage("��ȷ�����������");

            builder.setPositiveButton("ȷ��",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            progressDialog.init(progressDialog, "���ڷ��Ͷ�λ��Ϣ������");
                            progressDialog.show();

                            // ��ȡ��λ��Ϣ��Ȼ���͸���������������ź�δ���ͣ�����Ϣ˳��洢���ڴ濨��
                            // �ȴ����źź��Զ��ط������ͳɹ����߳������洢��Ϣ���洢��ʽSQLite
                            final Util util = new Util(Main.this);
                            final String msg = util.obtainLocation(Const.SOS,
                                    Main.this);
                            System.out.println("sosReport:------------" + msg);

                            Thread sos_thread = new Thread() {
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
                                sos_thread.start();
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
            builder.setNegativeButton("ȡ��",
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
            builder.setTitle("�˳�");
            builder.setMessage("ȷ���˳���");

            builder.setPositiveButton("ȷ��",
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
            builder.setNegativeButton("ȡ��",
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
