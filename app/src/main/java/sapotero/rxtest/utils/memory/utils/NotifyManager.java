package sapotero.rxtest.utils.memory.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.MainActivity;

/**
 * NotifyManager создаёт системные  уведомления ( Notification ) на основании коллекции докуметов, переданной в конструктор.
 * addedDocList - список UID докуметов. Ключи для documentsMap
 * documentsMap - HashMap документов
 * filter - STATUS_CODE документа
 */
public class NotifyManager {

    private final String TAG = NotifyManager.class.getSimpleName();
    /*порог количества новых документов в списке, больше которого, уведомления группируем*/
    private final int THRESHOLD_VALUE = 5;
    @Inject
    ISettings settings;
    private Context appContext = EsdApplication.getApplication();
    private List<String> addedDocList;
    private HashMap<String, Document> documentsMap;
    private String filter;
    private NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(appContext);

    public NotifyManager(List<String> addedDocList, HashMap<String, Document> documentsMap, String filter) {
        this.addedDocList = addedDocList;
        this.documentsMap = documentsMap;
        this.filter = filter;
        EsdApplication.getManagerComponent().inject(this);
    }

    /*Если от REST API получено больше THRESHOLD_VALUE документов -> группируем.
      Если меньше THRESHOLD_VALUE -> генерируем отдельное уведомление на каждый документ*/
    void generateNotifyMsg(String title) {
        int сurrentNotificationId = settings.getСurrentNotificationId();

        if (сurrentNotificationId > THRESHOLD_VALUE) {
            notificationManagerCompat.cancelAll();
            showGroupSummaryNotification("Вам поступило новых документов: ", addedDocList);
        } else {
            for (String uidDoc : addedDocList) {
                showSingleNotification(title, documentsMap.get(uidDoc).getTitle(), documentsMap.get(uidDoc), filter);
            }
        }
    }

    /*вызов одного уведомления со случайным notificationId. */
    private void showSingleNotification(String title, String msg, Document document, String filter) {
        int сurrentNotificationId = settings.getСurrentNotificationId() + 1;
        int requestCode = UUID.randomUUID().hashCode();

        Intent intent = InfoActivity.newIntent(appContext, document, filter);
        PendingIntent pendingIntentOpenDoc = PendingIntent.getActivity(appContext, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        builder.setContentTitle(title)
                .setContentText("№:" + msg)
                .setSmallIcon(R.drawable.ic_error)
                .setContentIntent(pendingIntentOpenDoc)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManagerCompat.notify(сurrentNotificationId, builder.build());
        settings.setСurrentNotificationId(сurrentNotificationId);
    }

    private void showGroupSummaryNotification(String title, List<String> addedDocList) {

        int сurrentNotificationId = settings.getСurrentNotificationId() + 1;

        Intent readDocIntent = MainActivity.newIntent(appContext);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, readDocIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        builder.setContentTitle(title + сurrentNotificationId)
                .setContentText("Итого требующих рассмотрения: " + сurrentNotificationId)
                .setNumber(addedDocList.size())
                .setSmallIcon(R.drawable.ic_error)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setGroupSummary(true)
                .setGroup("group")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent);
        notificationManagerCompat.notify(сurrentNotificationId, builder.build());
        settings.setСurrentNotificationId(сurrentNotificationId);
    }

}
