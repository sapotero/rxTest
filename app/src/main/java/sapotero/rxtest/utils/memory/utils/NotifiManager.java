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
import java.util.UUID;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.activities.InfoActivity;
import timber.log.Timber;

      /**
      * NotifiManager создаёт системные  уведомления ( Notification ) на основании коллекции докуметов, переданной в конструктор.
      *  addedDocList - список UID докуметов
      *  documentsMap - Map документов
      */
public class NotifiManager {

    @Inject Context appContext;
    private final String TAG = NotifiManager.class.getSimpleName();
    private List<String> addedDocList;
    private HashMap<String, Document> documentsMap;
    private int notificationId;
    private final int THRESHOLD_VALUE = 1; /*величина больше которой, уведомления группируем. InboxStyle */

    public NotifiManager(List<String> addedDocList, HashMap<String, Document> documentsMap) {
        this.addedDocList = addedDocList;
        this.documentsMap = documentsMap;
        this.notificationId = Math.abs( UUID.randomUUID().hashCode() );
        EsdApplication.getNetworkComponent().inject(this);
    }

    void generateNotifyMsg(String title) {
        if (addedDocList.size() > THRESHOLD_VALUE) {
            showGroupSummaryNotification("Вам поступило новых документов: ", addedDocList);
        } else {
            for (String uidDoc : addedDocList) {
                showSingleNotification(title, documentsMap.get(uidDoc).getTitle());
            }
        }
    }

    private void showSingleNotification(String title, String msg) {
        NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(appContext);
        notificationId =  UUID.randomUUID().hashCode() ;

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
        Timber.tag(TAG).d("showSingleNotification. notificationId = " + notificationId);
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    private void showGroupSummaryNotification(String title, List<String> addedDocList) {
        NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(appContext);
        notificationId =  UUID.randomUUID().hashCode() ;

        Intent readDocIntent = new Intent(appContext, InfoActivity.class);
        PendingIntent pireadDoc = PendingIntent.getActivity(appContext, 0, readDocIntent, 0);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle("Вам поступило новых документов: "+addedDocList.size());

        for (String uidDoc : addedDocList) {
            inboxStyle.addLine(documentsMap.get(uidDoc).getTitle());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        builder.setContentTitle(title + addedDocList.size())
                .setContentText(addedDocList.size() + " входящих...")
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
