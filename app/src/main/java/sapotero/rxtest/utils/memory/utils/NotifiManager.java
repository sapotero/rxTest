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
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.MainActivity;

      /**
      * NotifiManager создаёт системные  уведомления ( Notification ) на основании коллекции докуметов, переданной в конструктор.
      *  addedDocList - список UID докуметов. Ключи для documentsMap
      *  documentsMap - HashMap документов
      *  filter - STATUS_CODE документа
      */
public class NotifiManager {

    @Inject Context appContext;
   //@Inject ISettings settings;

    private final String TAG = NotifiManager.class.getSimpleName();
    private List<String> addedDocList;
    private HashMap<String, Document> documentsMap;
    private String filter;
    private final int THRESHOLD_VALUE = 5; /*порог количества новых документов в списке, больше которого, уведомления группируем*/

    public NotifiManager(List<String> addedDocList, HashMap<String, Document> documentsMap, String filter) {
        this.addedDocList = addedDocList;
        this.documentsMap = documentsMap;
        this.filter = filter;
        EsdApplication.getNetworkComponent().inject(this);
        EsdApplication.getManagerComponent().inject(this);
    }

    /*Если от REST API получено больше THRESHOLD_VALUE документов -> группируем.
      Если меньше THRESHOLD_VALUE -> генерируем отдельное уведомление на каждый документ*/
    void generateNotifyMsg(String title) {
        if (addedDocList.size() > THRESHOLD_VALUE) {
            showGroupSummaryNotification("Вам поступило новых документов: ", addedDocList);
        } else {
            for (String uidDoc : addedDocList) {
                showSingleNotification(title, documentsMap.get(uidDoc).getTitle(), documentsMap.get(uidDoc), filter);
            }
        }
    }
    /*вызов одного уведомления со случайным notificationId. */
    private void showSingleNotification(String title, String msg, Document document, String filter) {
        NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(appContext);
        int notificationId = UUID.randomUUID().hashCode() ;
        int requestCode = UUID.randomUUID().hashCode() ;

        Intent intent = InfoActivity.newIntent(appContext, document, filter );
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
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    private void showGroupSummaryNotification(String title, List<String> addedDocList) {
        NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(appContext);
        int notificationId =  UUID.randomUUID().hashCode() ;

        Intent readDocIntent = new Intent(appContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, readDocIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        builder.setContentTitle(title + addedDocList.size())
                .setContentText(addedDocList.size() + " входящих...")
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
        notificationManagerCompat.notify(notificationId, builder.build());
    }

}
