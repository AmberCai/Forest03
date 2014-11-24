package com.example.forest.dialog;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;

import com.example.forest.R;

/**
 * @content 对消息栏的具体设置（指按下home键等操作时，应用转入后台运行，通告栏的具体设置）
 * @author 覃双盼
 * 
 */

public class NotificationExtend {

    private final Activity context;

    public NotificationExtend(Activity context) {

        // TODO Auto-generated constructor stub
        this.context = context;
    }

    // 显示Notification
    @SuppressWarnings("deprecation")
    public void showNotification() {
        // 创建一个NotificationManager的引用---即获取系统服务
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        // // 使用Notification.Builder
        // Builder builder = new Notification.Builder(context);
        // builder.setSmallIcon(R.drawable.ic_launcher);
        // builder.setContentTitle("森林三防");
        // builder.setWhen(System.currentTimeMillis());
        // builder.build();

        // 定义Notification的各种属性
        Notification notification = new Notification(R.drawable.forest_notify,
                "森林三防", System.currentTimeMillis());
        // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        // 表明在点击了通知栏中的"清除通知"后，此通知自动清除。
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // 设置通知时的闪光灯效果
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        // 使用默认的闪光灯效果
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.ledARGB = Color.BLUE;
        notification.ledOnMS = 5000;

        // 设置通知的事件消息
        CharSequence contentTitle = "森林三防"; // 通知栏标题
        CharSequence contentText = "主功能界面，请查看……"; // 通知栏内容

        Intent notificationIntent = new Intent(context, context.getClass());
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // setLatestEventInfo设置用户在点击通知栏的这一项时，下拉列表所显示的内容，以及用户在
        // 点击时将要跳转的页面
        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);
        // 把Notification传递给NotificationManager
        notificationManager.notify(0, notification);
    }

    // 取消通知
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }
}