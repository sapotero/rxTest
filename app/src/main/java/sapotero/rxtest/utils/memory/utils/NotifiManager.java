package sapotero.rxtest.utils.memory.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.activities.InfoActivity;

/*NotifiManager создаёт системные уведомлениея ( Notification ) на основании коллекции докуметов переданной в конструктор.
        addedDocList - список UID докуметов
        documentsMap - Map документов
*/
public class NotifiManager {


    @Inject Context context;
    private final String TAG = NotifiManager.class.getSimpleName();
    private List<String> addedDocList;
    private HashMap<String, Document> documentsMap;

    public NotifiManager(List<String> addedDocList, HashMap<String, Document> documentsMap) {
        this.addedDocList = addedDocList;
        this.documentsMap = documentsMap;
        EsdApplication.getNetworkComponent().inject(this);
    }

    void generateNotifyMsg() {
        if (addedDocList.size() > 5) {
            showGroupSummaryNotification("Вам поступили входящие документы:",addedDocList, 0);
        } else {
            for (String item : addedDocList) {
                showSingleNotification("Вам поступил входящий документ:", documentsMap.get(item).getTitle(), 0);
            }
        }
    }


    private void showSingleNotification(String title,
                                        String message,
                                        int notificationId) {
        NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(context);

        Intent readDocIntent = new Intent(context, InfoActivity.class);
        PendingIntent pireadDoc = PendingIntent.getActivity(context, 0, readDocIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_error)
                .setGroupSummary(false)
                .setGroup("group")
                .addAction(R.drawable.ic_error,
                        "Открыть документ",
                        pireadDoc)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    private void showGroupSummaryNotification(String title,
                                              List<String> addedDocList,
                                              int notificationId) {
        NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(context);

        Intent readDocIntent = new Intent(context, InfoActivity.class);
        PendingIntent pireadDoc = PendingIntent.getActivity(context, 0, readDocIntent, 0);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setSummaryText("setSummaryText")
                .setBigContentTitle("BigContentTitle");

        for (String uidDoc : addedDocList) {
            inboxStyle.addLine(documentsMap.get(uidDoc).getTitle());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
//              .setContentText(message)
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
