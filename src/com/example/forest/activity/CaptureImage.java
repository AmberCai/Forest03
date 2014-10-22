package com.example.forest.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.forest.R;
import com.example.forest.dialog.ProgressBar;
import com.example.forest.util.Const;
import com.example.forest.util.DBManager;
import com.example.forest.util.Forest;
import com.example.forest.util.PicUtil;
import com.example.forest.util.Util;

public class CaptureImage extends Activity {
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    ProgressBar progressbar;

    Button take;
    SurfaceView sView;
    SurfaceHolder surfaceHolder;
    int screenWidth, screenHeight;
    // 定义系统所用的照相机
    Camera camera;
    // 是否在浏览中
    boolean isPreview = false;

    Bundle from;

    // 照片名和病虫害详情中的日期和时间
    String datetime;

    // 照片对应的警报类型
    String type;

    // 同一定位信息对应的多张照片的序号
    int count = 0;

    static String photoName;

    Handler handler;

    // 定义位图
    Bitmap bm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED,
                FLAG_HOMEKEY_DISPATCHED);
        setContentView(R.layout.main);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        Intent intent = getIntent();
        from = intent.getExtras();

        datetime = from.getString("datetime");
        type = from.getString("type");

        Display display = wm.getDefaultDisplay();
        // 获取屏幕的宽和高
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        // 获取界面中SurfaceView组件
        sView = (SurfaceView) findViewById(R.id.sView);

        take = (Button) findViewById(R.id.take);
        take.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                // 拍 照
                camera.takePicture(null, null, myjpegCallback);
            }
        });

        // 获得SurfaceView的SurfaceHolder
        surfaceHolder = sView.getHolder();
        // 为surfaceHolder添加一个回调监听器
        surfaceHolder.addCallback(new Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                    int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // 打开摄像头
                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 如果camera不为null ,释放摄像头
                if (camera != null) {
                    if (isPreview)
                        camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        });
        // 设置该SurfaceView自己不维护缓冲
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 上传进度框提示
        progressbar = new ProgressBar(CaptureImage.this, R.style._ProgressBar);
        progressbar.setContentView(R.layout.progress);
        progressbar.init(progressbar, "正在上传图片。。。");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 显示进度条
                if (msg.what == Const.SHOWDIALOG) {
                    progressbar.show();
                }
                // 若图片发送出去，则取消进度条，若网络不通则一直显示？？？
                else if (msg.what == Const.SENDED) {
                    progressbar.cancel();
                }
                // 终止拍照，跳转到哪个界面？？？
                else if (msg.what == Const.FINISH) {
                    CaptureImage.this.finish();
                }
            }
        };
    }

    private void initCamera() {
        if (!isPreview) {
            camera = Camera.open();
        }
        if (camera != null && !isPreview) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                // 设置预览照片的大小
                parameters.setPreviewSize(screenHeight, screenWidth);
                // 每秒显示4帧
                parameters.setPreviewFrameRate(4);
                // 设置图片格式
                parameters.setPictureFormat(PixelFormat.JPEG);
                // 设置JPG照片的质量
                parameters.set("jpeg-quality", 85);
                // 设置照片的大小
                parameters.setPictureSize(screenHeight, screenWidth);
                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);
                // 通过SurfaceView显示取景画面
                camera.setPreviewDisplay(surfaceHolder);
                // 开始预览
                camera.startPreview();
                // 自动对焦
                camera.autoFocus(null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            isPreview = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        // 当用户单击照相键、中央键时执行拍照
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_CAMERA:
                if (camera != null && event.getRepeatCount() == 0) {
                    // 拍照
                    camera.takePicture(null, null, myjpegCallback);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_HOME:
                System.out.println("HOME键被按下");
        }
        return super.onKeyDown(keyCode, event);
    }

    PictureCallback myjpegCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 根据拍照所得的数据创建位图
            final Bitmap bm0 = BitmapFactory.decodeByteArray(data, 0,
                    data.length);

            Matrix m = new Matrix();
            m.setRotate(90, (float) bm0.getWidth() / 2,
                    (float) bm0.getHeight() / 2);
            bm = Bitmap.createBitmap(bm0, 0, 0, bm0.getWidth(),
                    bm0.getHeight(), m, true);

            // 加载/layout/save.xml文件对应的布局资源
            View saveDialog = getLayoutInflater().inflate(R.layout.save, null);

            // 获取saveDialog对话框上的ImageView组件
            ImageView show = (ImageView) saveDialog.findViewById(R.id.show);
            // 显示刚刚拍得的照片
            show.setImageBitmap(bm);

            // 使用对话框显示saveDialog组件
            new AlertDialog.Builder(CaptureImage.this).setView(saveDialog)
                    .setPositiveButton("继续拍照", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /**
                             * @date 20141017
                             * @content 优化代码
                             */
                            uoloadPhoto();

                        }
                    }).setNegativeButton("完成", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            // 删除当前提醒框
                            arg0.dismiss();

                            uoloadPhoto();

                            // 若是pests报警，则点击完成后转到pestDetail页面
                            if (from != null && from.getBoolean("pests")) {
                                DBManager dbManager = new DBManager(
                                        CaptureImage.this);
                                dbManager.openDatabase();
                                boolean hasPestsInfo = dbManager
                                        .haspestsKinds();
                                dbManager.closeDatabase();
                                if (hasPestsInfo) {
                                    Bundle data = new Bundle();
                                    data.putString("datetime", datetime);
                                    Intent intent = new Intent(
                                            CaptureImage.this,
                                            PestsDetail.class);
                                    intent.putExtras(data);
                                    intent.setFlags(intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                    startActivity(intent);
                                    intent = null;
                                }
                            }
                            else {
                                Intent intent = new Intent(CaptureImage.this,
                                        Main.class);
                                intent.setFlags(intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                startActivity(intent);
                                intent = null;
                            }
                            handler.sendEmptyMessage(Const.FINISH);
                        }
                    }).show();

            // 重新浏览
            camera.stopPreview();
            camera.startPreview();
            isPreview = true;
        }

    };

    // public void stopCamera() {
    // // TODO Auto-generated method stub
    // camera.stopPreview();
    //
    // }
    //
    // public void resetCamera() {
    // // TODO Auto-generated method stub
    // if (camera != null && isPreview) {
    // camera.stopPreview();
    // camera.release();
    // camera = null;
    // isPreview = false;
    // }
    // }

    /**
     * @date 20141017
     * @content 优化代码
     * @return
     */
    public void uoloadPhoto() {

        handler.sendEmptyMessage(Const.SHOWDIALOG);

        StringBuilder photoNameStr = new StringBuilder();
        // photoNameStr.append(Forest.config_preferences.getString("phoneID",
        // "xxxxxxxx"));
        photoNameStr.append(Util.phoneID);
        photoNameStr.append(type);
        photoNameStr.append(datetime);
        photoNameStr.append("E");
        photoNameStr.append(customFormat(Const.cur_location.getLongitude()));
        photoNameStr.append("N");
        photoNameStr.append(customFormat(Const.cur_location.getLatitude()));
        photoNameStr.append(count);
        count++;

        photoName = photoNameStr.toString();
        Log.d("Forest-------", photoName);

        // 创建一个位于SD卡上的文件
        final File file = new File(Environment.getExternalStorageDirectory()
                + "/forest/msg/", photoName + ".jpg");

        Util.newName = photoName + ".jpg";

        FileOutputStream outStream = null;
        try {
            // 打开指定文件对应的输出流
            outStream = new FileOutputStream(file);

            // 压缩然后存放本地
            PicUtil.comp(bm).compress(CompressFormat.JPEG, 50, outStream);
            outStream.close();

            Thread sendPicThread = new Thread() {
                @Override
                public void run() {
                    if (Util.uploadFile(file))// 上传完毕
                    {
                        handler.sendEmptyMessage(Const.SENDED);
                        file.delete();
                    }
                    else // 未上传成功的话，将文件名保存到数据库
                    {
                        DBManager dbManager = new DBManager(CaptureImage.this);
                        dbManager.openDatabase();
                        dbManager.insert_photos(photoName, 0);
                        dbManager.closeDatabase();
                        /**
                         * @date 20141017
                         * @content 增加以下代码：未上传成功也消除进度条
                         */
                    }
                };
            };
            if (Forest.isNetConnect(CaptureImage.this)) {
                sendPicThread.start();
            }
            else {
                DBManager dbManager = new DBManager(CaptureImage.this);
                dbManager.openDatabase();
                dbManager.insert_photos(photoName, 0);
                dbManager.closeDatabase();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将double型数格式化输出
    public String customFormat(double value) {
        String pattern = "0000000000000000";
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        String output = myFormatter.format(value * Math.pow(10, 13));
        return output;
    }

}
