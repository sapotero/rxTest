package sapotero.rxtest.utils.memory.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.activities.InfoActivity;
import timber.log.Timber;

    /** NotifiManager создаёт системные  уведомления ( Notification ) на основании коллекции докуметов, переданной в конструктор.
        addedDocList - список UID докуметов
        documentsMap - Map документов
        currentNotifiId -  уникальный ID уведомления.
     **/
public class NotifiManager {

    @Inject Context appContext;
    private final String TAG = NotifiManager.class.getSimpleName();
    private List<String> addedDocList;
    private HashMap<String, Document> documentsMap;
    private int currentNotificationId;
    private final int THRESHOLD_VALUE = 5; /*величина больше которой, уведомления группируем. InboxStyle */

    public NotifiManager(List<String> addedDocList, HashMap<String, Document> documentsMap, int currentNotificationId) {
        this.addedDocList = addedDocList;
        this.documentsMap = documentsMap;
        this.currentNotificationId = currentNotificationId;
        EsdApplication.getNetworkComponent().inject(this);
    }

    void generateNotifyMsg() {
        if (addedDocList.size() > THRESHOLD_VALUE) {
            showGroupSummaryNotification("Вам поступили входящие документы:", addedDocList, currentNotificationId);
        } else {
            for (String uidDoc : addedDocList) {
                showSingleNotification("Вам поступил входящий документ:", documentsMap.get(uidDoc).getTitle(), currentNotificationId);
            }
        }
    }

    private void showSingleNotification(String title, String msg, int notificationId) {
        // TODO: вынести строки в ресурсы
        NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(appContext);

        ArrayList<String> documentsUidsList = new ArrayList<>();
        documentsUidsList.addAll(addedDocList);
        Intent intent = InfoActivity.newIntent(appContext, documentsUidsList);
        PendingIntent pireadDoc = PendingIntent.getActivity(appContext, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        builder.setContentTitle(title)
                .setContentText("№:" + msg)
                .setSmallIcon(R.drawable.ic_error)
                .setGroupSummary(true)
                .setGroup("group")
                .addAction(R.drawable.ic_error,
                        "Открыть документ",
                        pireadDoc)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        Timber.tag(TAG).d("notificationId = " + notificationId);
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    private void showGroupSummaryNotification(String title, List<String> addedDocList, int notificationId) {
        // TODO: вынести строки в ресурсы
        NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(appContext);

        Intent readDocIntent = new Intent(appContext, InfoActivity.class);
        PendingIntent pireadDoc = PendingIntent.getActivity(appContext, 0, readDocIntent, 0);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle("BigContentTitle")
                .setSummaryText("больше " + THRESHOLD_VALUE + " входящих документов...");
        for (String uidDoc : addedDocList) {
            inboxStyle.addLine(documentsMap.get(uidDoc).getTitle());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        builder.setContentTitle(title)
                .setContentText(addedDocList.size()+" входящих...")
                .setStyle(inboxStyle)
                .setNumber(addedDocList.size())
                .setSmallIcon(R.drawable.ic_error)
//              .setCategory(Notification.CATEGORY_EVENT)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .addAction(R.drawable.ic_error,
                        "Открыть документ",
                        pireadDoc)
                .setGroupSummary(true)
                .setGroup("group")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        notificationManagerCompat.notify(notificationId, builder.build());
    }

}
