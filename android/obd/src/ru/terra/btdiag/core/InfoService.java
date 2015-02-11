package ru.terra.btdiag.core;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.google.inject.Inject;
import roboguice.service.RoboIntentService;
import ru.terra.btdiag.R;

/**
 * Date: 11.02.15
 * Time: 13:58
 */
public class InfoService extends RoboIntentService {
    private final static String TAG = InfoService.class.getName();
    public static final String INFO_BROADCAST_ACTION = "ru.terra.btdiag.core.info_action";
    private InfoBroadcastReceiver infoBroadcastReceiver;
    public static final int NOTIFICATION_ID = 1;
    @Inject
    protected NotificationManager notificationManager;
    protected LocalBroadcastManager localBroadcastManager;
    private String chatInfo = "";
    private String obdInfo = "";

    public InfoService() {
        super("Information service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter sendMsgFilter = new IntentFilter(INFO_BROADCAST_ACTION);
        sendMsgFilter.addCategory(Intent.CATEGORY_DEFAULT);
        infoBroadcastReceiver = new InfoBroadcastReceiver();
        localBroadcastManager.registerReceiver(infoBroadcastReceiver, sendMsgFilter);

        showNotification("Starting...");
        while (true)
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(infoBroadcastReceiver);
        notificationManager.cancelAll();
    }

    private class InfoBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showNotification(intent.getStringExtra("text"));
        }
    }

    protected void showNotification(String contentText) {
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("BTDiag info")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
//        notificationBuilder.setOngoing(true);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.getNotification());
    }
}
