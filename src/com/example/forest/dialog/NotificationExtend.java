package com.example.forest.dialog;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;

import com.example.forest.R;

/**
 * @content ����Ϣ���ľ������ã�ָ����home���Ȳ���ʱ��Ӧ��ת���̨���У�ͨ�����ľ������ã�
 * @author ��˫��
 * 
 */

public class NotificationExtend {

    private final Activity context;

    public NotificationExtend(Activity context) {

        // TODO Auto-generated constructor stub
        this.context = context;
    }

    // ��ʾNotification
    @SuppressWarnings("deprecation")
    public void showNotification() {
        // ����һ��NotificationManager������---����ȡϵͳ����
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        // // ʹ��Notification.Builder
        // Builder builder = new Notification.Builder(context);
        // builder.setSmallIcon(R.drawable.ic_launcher);
        // builder.setContentTitle("ɭ������");
        // builder.setWhen(System.currentTimeMillis());
        // builder.build();

        // ����Notification�ĸ�������
        Notification notification = new Notification(R.drawable.forest_notify,
                "ɭ������", System.currentTimeMillis());
        // ����֪ͨ�ŵ�֪ͨ����"Ongoing"��"��������"����
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        // �����ڵ����֪ͨ���е�"���֪ͨ"�󣬴�֪ͨ�Զ������
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // ����֪ͨʱ�������Ч��
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        // ʹ��Ĭ�ϵ������Ч��
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.ledARGB = Color.BLUE;
        notification.ledOnMS = 5000;

        // ����֪ͨ���¼���Ϣ
        CharSequence contentTitle = "ɭ������"; // ֪ͨ������
        CharSequence contentText = "�����ܽ��棬��鿴����"; // ֪ͨ������

        Intent notificationIntent = new Intent(context, context.getClass());
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // setLatestEventInfo�����û��ڵ��֪ͨ������һ��ʱ�������б�����ʾ�����ݣ��Լ��û���
        // ���ʱ��Ҫ��ת��ҳ��
        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);
        // ��Notification���ݸ�NotificationManager
        notificationManager.notify(0, notification);
    }

    // ȡ��֪ͨ
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }
}