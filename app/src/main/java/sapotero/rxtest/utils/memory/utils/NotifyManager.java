package sapotero.rxtest.utils.memory.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.notification.RemoveAllNotificationEvent;
import sapotero.rxtest.events.notification.RemoveIdNotificationEvent;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.models.NotifyMessageModel;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.MainActivity;
import timber.log.Timber;

/**
 * NotifyManager создаёт системные  уведомления ( Notification ).
 */
public class NotifyManager {

  private final String TAG = NotifyManager.class.getSimpleName();
  /*порог количества новых документов в списке, больше которого, уведомления группируем*/
  private final int THRESHOLD_VALUE = 4;
  @Inject ISettings settings;
  private Context appContext = EsdApplication.getApplication();
  private NotificationManagerCompat notificationManagerCompat = MainService.getNotificationManagerCompat();

  private HashSet<Integer> notificationIdSet;
  private int notViewedDocumentQuantity;

  public NotifyManager() {
    EsdApplication.getManagerComponent().inject(this);
    this.notificationIdSet = new HashSet<>();
    EventBus.getDefault().register(this);
  }

  public void subscribeOnNotifyEvents(PublishSubject<NotifyMessageModel> notifyPubSubject) {
    if (!notifyPubSubject.hasObservers()) {
      notifyPubSubject
//      .throttleLast(5, TimeUnit.SECONDS)
        .filter(new Func1<NotifyMessageModel, Boolean>() {
          @Override
          public Boolean call(NotifyMessageModel notifyMessageModel) {
            return !notifyMessageModel.isFirstRunApp();
          }
        })
        .filter(new Func1<NotifyMessageModel, Boolean>() {
          @Override
          public Boolean call(NotifyMessageModel notifyMessageModel) {
            return !Objects.equals(notifyMessageModel.getSource().name(), "FOLDER");
          }
        })
        .filter(new Func1<NotifyMessageModel, Boolean>() {
          @Override
          public Boolean call(NotifyMessageModel notifyMessageModel) {
            /*приводим строку index к виду Fields.Journal*/
            Fields.Journal itemJournal = getJournal(notifyMessageModel);
            return checkAllowedJournal(itemJournal);
          }
        })
        .subscribe(new Action1<NotifyMessageModel>() {
          @Override
          public void call(NotifyMessageModel notifyMessageModel) {
            String filtr = notifyMessageModel.getFilter();
            List<String> docUIDList = notifyMessageModel.getUidDocsLIst();
            HashMap<String, Document> documentsMap = notifyMessageModel.getDocumentsMap();

            String Title =  getTitle(getJournal(notifyMessageModel));


            if (docUIDList.size() >= THRESHOLD_VALUE){
              notificationManagerCompat.cancelAll();
              notificationIdSet.clear();

              int notificationId =  UUID.randomUUID().hashCode();
              notificationIdSet.add(notificationId);


              showGroupSummaryNotificationTest("Вам поступило новых документов: " + docUIDList.size(), "Вам поступило новых документов: " + docUIDList.size(),
                  notificationId, NotificationCompat.PRIORITY_HIGH);


            } else {
                for (String uid : docUIDList) {
                  int notificationId =  UUID.randomUUID().hashCode();
                  showSingleNotification(Title, documentsMap.get(uid).getTitle(), documentsMap.get(uid), filtr, notificationId);
                  notificationIdSet.add(notificationId);
                  notViewedDocumentQuantity ++;


                }
                if(notificationIdSet.size() >= THRESHOLD_VALUE){
                  notificationManagerCompat.cancelAll();
                  notificationIdSet.clear();
                  int notificationId =  UUID.randomUUID().hashCode();
                  showWithoutHeadsUpNotificationTest("Вам поступило новых документов: " + notViewedDocumentQuantity, notificationId);
                  notificationIdSet.add(notificationId);

                }
              }


            }
          }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
              Timber.tag(TAG).e("!!!throwable = " + throwable);
            }
          });
    }
  }





  /*проверяем, включён ли checkBox для журнала. -> генерируем уведомление */
  private boolean checkAllowedJournal(Fields.Journal itemJournal){
    return settings.getNotificatedJournals().contains(itemJournal.getValue());
  }

  private String getShortJournalName(String longJournalName, String filter) {
    String shortJournalName = "";

    if (longJournalName != null) {
      String[] index = longJournalName.split("_production_db_");
      shortJournalName = index[0];
    } else if (Objects.equals(filter, "approval")) {
      shortJournalName = "APPROVE";
    } else if (Objects.equals(filter, "signing")) {
      shortJournalName = "SIGN";
    }
    return shortJournalName;
  }

  private String getTitle(Fields.Journal itemJournal){
   return itemJournal.getFormattedName() + itemJournal.getSingle();
  }

  private Fields.Journal getJournal(NotifyMessageModel notifyMessageModel){
    String shortNameJournal = getShortJournalName(notifyMessageModel.getIndex(), notifyMessageModel.getFilter()).toUpperCase();
    return Fields.Journal.valueOf(shortNameJournal);
  }

  /*вызов одного уведомления со случайным notificationId. */
  private void showSingleNotification(String title, String msg, Document document, String filter, int notificationId) {
    int requestCode = UUID.randomUUID().hashCode();
    Intent intent = InfoActivity.newIntent(appContext, document, filter, notificationId);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext)
      .addParentStack(InfoActivity.class)
      .addNextIntent(intent);

    PendingIntent pendingIntentOpenDoc = stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_CANCEL_CURRENT);


    Intent intentDismiss = new Intent(appContext, NotificationDismissedReceiver.class);
    intentDismiss.putExtra("notificationId",notificationId);
    PendingIntent pendingIntentDismiss = PendingIntent.getBroadcast(appContext, requestCode, intentDismiss, PendingIntent.FLAG_CANCEL_CURRENT );

    NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
    builder.setContentTitle(title)
      .setContentText(msg)
      .setSmallIcon(R.drawable.ic_error)
      .setContentIntent(pendingIntentOpenDoc)
      .setDeleteIntent(pendingIntentDismiss)
      .setAutoCancel(true)
      .setDefaults(Notification.DEFAULT_ALL)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setVisibility(Notification.VISIBILITY_PUBLIC);
    notificationManagerCompat.notify(notificationId, builder.build());
  }

  private void showGroupSummaryNotificationTest(String bigContentTitle, String contentTitle , int notificationId, int notificationCompatPriority ) {
    Intent openDocIntent = MainActivity.newIntent(appContext);
    PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, openDocIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    Intent intentDismiss = new Intent(appContext, NotificationDismissedReceiver.class);
    intentDismiss.putExtra("notificationId", notificationId);
    PendingIntent pendingIntentDismiss = PendingIntent.getBroadcast(appContext, 0, intentDismiss, 0);

    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
      .setBigContentTitle(bigContentTitle)
      .addLine(bigContentTitle);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
    builder.setContentTitle(contentTitle)
//        .setContentText("Итого требующих рассмотрения: " + notViewedDocQuantity)
        .setStyle(inboxStyle)
        .setSmallIcon(R.drawable.ic_error)
        .setCategory(Notification.CATEGORY_MESSAGE)
        .setGroupSummary(true)
        .setGroup("group")
        .setDeleteIntent(pendingIntentDismiss)
        .setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL)
        .setPriority(notificationCompatPriority)
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setContentIntent(pendingIntent);
    notificationManagerCompat.notify(notificationId, builder.build());
  }

  private void showWithoutHeadsUpNotificationTest(String contentTitle, int notificationId) {
    int requestCode = UUID.randomUUID().hashCode();
    Intent IntentOpenDoc = MainActivity.newIntent(appContext);
    PendingIntent pendingIntentOpenDoc = PendingIntent.getActivity(appContext, 0, IntentOpenDoc, PendingIntent.FLAG_CANCEL_CURRENT);

    Intent intentDismiss = new Intent(appContext, NotificationDismissedReceiver.class);
    intentDismiss.putExtra("notificationId", notificationId);
    PendingIntent pendingIntentDismiss = PendingIntent.getBroadcast(appContext, requestCode, intentDismiss, PendingIntent.FLAG_CANCEL_CURRENT );

    NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
    builder.setContentTitle(contentTitle)
      .setSmallIcon(R.drawable.ic_error)
      .setCategory(Notification.CATEGORY_MESSAGE)
      .setGroupSummary(true)
      .setGroup("group")
      .setDeleteIntent(pendingIntentDismiss)
      .setAutoCancel(true)
      .setDefaults(Notification.DEFAULT_ALL)
      .setPriority(NotificationCompat.DEFAULT_ALL)
      .setVisibility(Notification.VISIBILITY_PUBLIC)
      .setContentIntent(pendingIntentOpenDoc);
    notificationManagerCompat.notify(notificationId, builder.build());
  }


  private void removeAllNotification(){
    notificationManagerCompat.cancelAll();
  }

  private void removeIdNotification(int notificationId){
    notificationIdSet.remove(notificationId);
    Timber.tag(TAG).e(" 22 removeIdNotification.=" + notificationIdSet.size());
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onMessageEvent(RemoveAllNotificationEvent event) {
    Timber.tag(TAG).e("RemoveAllNotification(). Event =" + event.isRemoveAllNotification);
    removeAllNotification();
    notViewedDocumentQuantity = 0;
    EventBus.getDefault().removeStickyEvent(event);
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onMessageEvent(RemoveIdNotificationEvent event) {
    Timber.tag(TAG).e("RemoveIdNotificationEvent. event.notificationId =" + event.notificationId);
    removeIdNotification(event.notificationId);
    notViewedDocumentQuantity --;
    EventBus.getDefault().removeStickyEvent(event);
  }

}
