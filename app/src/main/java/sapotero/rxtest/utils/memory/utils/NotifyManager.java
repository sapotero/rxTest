package sapotero.rxtest.utils.memory.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.models.NotifyMessageModel;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.MainActivity;
import timber.log.Timber;

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
  @Inject ISettings settings;
  private Context appContext = EsdApplication.getApplication();
  private List<String> addedDocList;
  private HashMap<String, Document> documentsMap;
  private String filter;
  private NotificationManagerCompat notificationManagerCompat = (NotificationManagerCompat) NotificationManagerCompat.from(appContext);
  private Processor.Source source = Processor.Source.EMPTY;
  private Set<String> allowJournals;



//  public NotifyManager(List<String> addedDocList, HashMap<String, Document> documentsMap, String filter) {
//    EsdApplication.getManagerComponent().inject(this);
//    this.addedDocList = addedDocList;
//    this.documentsMap = documentsMap;
//    this.filter = filter;
//    this.isFirstRunApp = settings.isFirstRun();
//  }

  public NotifyManager() {
  }

  public void subscribeOnNotifyEvents(PublishSubject<NotifyMessageModel> notifyPubSubject) {
    if (! notifyPubSubject.hasObservers()){
      notifyPubSubject
          .distinct(new Func1<NotifyMessageModel, Object>() {
            @Override
            public Object call(NotifyMessageModel notifyMessageModel) {
              return null;
            }
          })
          .skipWhile(new Func1<NotifyMessageModel, Boolean>() {
            @Override
            public Boolean call(NotifyMessageModel notifyMessageModel) {
              return null;
            }
          })
          .throttleLast(5, TimeUnit.SECONDS)
          .subscribe(new Action1<NotifyMessageModel>() {
        @Override
        public void call(NotifyMessageModel notifyMessageModel) {
           





        }
      }, new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {

        }
      });
    }

//    return this;
  }


  /*Если от REST API получено больше THRESHOLD_VALUE документов -> группируем.
    Если меньше THRESHOLD_VALUE -> генерируем отдельное уведомление на каждый документ*/
  void generate(String title) {
    int сurrentNotificationId = settings.getСurrentNotificationId();

    if (!settings.isFirstRun() && сurrentNotificationId > THRESHOLD_VALUE - 1 ) {
      notificationManagerCompat.cancelAll();
      showGroupSummaryNotification("Вам поступило новых документов: ", addedDocList);
    } else if(!settings.isFirstRun()){
      for (String uidDoc : addedDocList) {
        showSingleNotification(title, documentsMap.get(uidDoc).getTitle(), documentsMap.get(uidDoc), filter);
      }
    }
  }

  private String getShortJournalName(String longJournalName){

//        String shortNameJournal = getShortJournalName(index).toUpperCase();
//    Fields.Journal itemJournal = Fields.Journal.valueOf(shortNameJournal);


    String shortJournalName = "";

    if (  longJournalName != null ) {
      String[] index = longJournalName.split("_production_db_");
      shortJournalName = index[0];
    }else if (Objects.equals(this.filter, "approval")){
      shortJournalName = "APPROVE" ;
    }else if (Objects.equals(this.filter, "signing")){
      shortJournalName = "SIGN" ;
    }
    return shortJournalName;
  }


  /*вызов одного уведомления со случайным notificationId. */
  private void showSingleNotification(String title, String msg, Document document, String filter) {
    int сurrentNotificationId = settings.getСurrentNotificationId() + 1;
    int requestCode = UUID.randomUUID().hashCode();

    Intent intent = InfoActivity.newIntent(appContext, document, filter);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext);
    stackBuilder.addParentStack(InfoActivity.class);
    stackBuilder.addNextIntent(intent);

    PendingIntent pendingIntentOpenDoc = stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_CANCEL_CURRENT);
//  PendingIntent.getActivity(appContext, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
    builder.setContentTitle(title)
      .setContentText(msg)
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
