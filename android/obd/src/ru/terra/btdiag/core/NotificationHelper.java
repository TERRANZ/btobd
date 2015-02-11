package ru.terra.btdiag.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import com.google.inject.Inject;
import roboguice.inject.ContextSingleton;

/**
 * Created with IntelliJ IDEA.
 * User: terranz
 * Date: 25.06.13
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
@ContextSingleton
public class NotificationHelper {

    private NotificationManager notifier;
    private NotificationCompat.Builder builder;

    private Context context;

    @Inject
    public NotificationHelper(Context context) {
        this.context = context;
    }

    @Inject
    private SettingsService settingsService;

    private void prepeareBuilder(String text) {
        notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context);
        builder.setContentText(text);
        builder.setContentTitle(text);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setTicker(text);
    }

//    public void notify(String text, boolean showList) {
//        prepeareBuilder(text);
//        if (showList) {
//            Intent intent = new Intent(context, FeedPostsListActivity.class);
//            intent.putExtra("from_date", settingsService.getSetting("last_sync_time", "0"));
//            settingsService.saveSetting("last_sync_time", String.valueOf(new Date().getTime()));
//            intent.putExtra("from_notify", true);
//            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
//            builder.setContentIntent(pIntent);
//        }
//        notifier.notify(0, builder.build());
//    }
//
//    public void notifyStartUpdate() {
//        prepeareBuilder("Обновление...");
//        builder.setProgress(100, 100, true);
//        notifier.notify(0, builder.build());
//    }

}
