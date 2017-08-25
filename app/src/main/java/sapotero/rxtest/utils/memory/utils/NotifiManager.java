package sapotero.rxtest.utils.memory.utils;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import javax.inject.Inject;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;

public class NotifiManager {

    @Inject Context context;
    private final String TAG = NotifiManager.class.getSimpleName();
    private int countIncomingDoc ;



    public NotifiManager(int countIncomingDocs) {
        EsdApplication.getNetworkComponent().inject(this);
        this.countIncomingDoc = countIncomingDocs;
    }

    void generateNotifyMsg() {
//        Timber.tag(TAG).d("generateNotifyMsg()");
//
//        Intent readIntent = new Intent(context, LoginActivity.class);
//        PendingIntent piRead = PendingIntent.getActivity(context, 0, readIntent, 0);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.ic_error)
//                .setContentTitle("My notification")
//                .setContentText("content Text")
//
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setVisibility(Notification.VISIBILITY_PUBLIC)
//
//                .addAction(R.drawable.ic_error, "Read", piRead) // #0
//                .addAction(R.drawable.ic_error, "Cancel", piRead);  // #1
//
//        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(0, mBuilder.build());
    }


    private void showSingleNotification(NotificationManagerCompat notificationManagerCompat,
                                        String title,
                                        String message,
                                        int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_error)
                .setGroupSummary(false)
                .setGroup("group")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManagerCompat.notify(notificationId,  builder.build());
    }

    private void showGroupSummaryNotification(NotificationManagerCompat notificationManager) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Dummy content title")
                .setContentText("Dummy content text")
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Line 1")
                        .addLine("Line 2")
                        .setSummaryText("Inbox summary text")
                        .setBigContentTitle("Big content title"))
//                .setNumber(2)
                .setSmallIcon(R.drawable.ic_error)
//                .setCategory(Notification.CATEGORY_EVENT)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setGroupSummary(true)
                .setGroup("group")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        notificationManager.notify(123456,  builder.build());
    }


}
