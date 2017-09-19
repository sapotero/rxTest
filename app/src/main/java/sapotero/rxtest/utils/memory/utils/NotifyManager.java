package sapotero.rxtest.utils.memory.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.events.notification.RemoveAllNotificationEvent;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.models.NotifyMessageModel;
import sapotero.rxtest.views.activities.MainActivity;
import timber.log.Timber;

/**
 * NotifyManager создаёт системные  уведомления ( Notification ).
 */
public class NotifyManager {

  private final String TAG = NotifyManager.class.getSimpleName();
  @Inject ISettings settings;
  private Context appContext = EsdApplication.getApplication();
  private NotificationManagerCompat notificationManagerCompat = MainService.getNotificationManagerCompat();
  private int notViewedDocumentQuantity = 0;

  public NotifyManager() {
    EsdApplication.getManagerComponent().inject(this);
    EventBus.getDefault().register(this);
  }

  public void subscribeOnNotifyEvents(PublishSubject<NotifyMessageModel> notifyPubSubject) {
    if (!notifyPubSubject.hasObservers()) {
      notifyPubSubject
        .filter(notifyMessageModel -> !notifyMessageModel.isFirstRunApp())
        .filter(notifyMessageModel -> !Objects.equals(notifyMessageModel.getSource().name(), "FOLDER"))
        .filter(notifyMessageModel -> {
          /*приводим строку index к виду JournalStatus. Проверяем в разрешенных журналах*/
          JournalStatus itemJournalStatus = getJournal(notifyMessageModel);
          return checkAllowedJournal(itemJournalStatus);
        })
        .buffer(5 ,TimeUnit.SECONDS)
        .filter(new Func1<List<NotifyMessageModel>, Boolean>() {
          @Override
          public Boolean call(List<NotifyMessageModel> notifyMessageModels) {
            return !notifyMessageModels.isEmpty();
          }
        })
        .subscribe(new Action1<List<NotifyMessageModel>>() {
          @Override
          public void call(List<NotifyMessageModel> notifyMessageModels) {
            if(notifyMessageModels.size() > 1) {
              notViewedDocumentQuantity = notViewedDocumentQuantity + notifyMessageModels.size();
              String contentTitle = "Вам поступило новых документов: " + notifyMessageModels.size();
              String contentText = "Итого требующих рассмотрения: " + notViewedDocumentQuantity;
              generateNotifyMsg(contentTitle, contentText, 1);
            } else {
              for (NotifyMessageModel item : notifyMessageModels){
                notViewedDocumentQuantity = notViewedDocumentQuantity + notifyMessageModels.size();
                String contentTitle = getTitle(getJournal(item)) + item.getDocument().getTitle();
                String contentText = "Итого требующих рассмотрения: " + notViewedDocumentQuantity;
                generateNotifyMsg(contentTitle, contentText, 1);
              }
            }
          }
        }, new Action1<Throwable>() {
          @Override
          public void call(Throwable throwable) {
            Timber.tag(TAG).e("throwable = " + throwable);
          }
        });
    }
  }

  private void generateNotifyMsg(String contentTitle, String contentText, int currentNotificationId ){
    NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
    
    Intent openIntent = MainActivity.newIntent(appContext);
    PendingIntent openPendingIntent = PendingIntent.getActivity(appContext, 0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    Intent intentDismiss = new Intent(appContext, NotificationDismissedReceiver.class);
    PendingIntent pendingIntentDismiss = PendingIntent.getBroadcast(appContext, 0, intentDismiss, PendingIntent.FLAG_CANCEL_CURRENT);

    builder
      .setContentTitle(contentTitle)
      .setContentText(contentText)
      .setSmallIcon(R.drawable.ic_error)
      .setDeleteIntent(pendingIntentDismiss)
      .setAutoCancel(true)
      .setDefaults(Notification.DEFAULT_ALL)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setVisibility(Notification.VISIBILITY_PUBLIC)
      .setContentIntent(openPendingIntent);
    notificationManagerCompat.notify(currentNotificationId, builder.build());
  }

  /*проверяем, включён ли checkBox для журнала*/
  private boolean checkAllowedJournal(JournalStatus itemJournal){
    return settings.getNotificatedJournals().contains(itemJournal.getStringIndex());
  }

  private String getTitle(JournalStatus itemJournal){
    return itemJournal.getFormattedName() + itemJournal.getSingle();
  }

  private JournalStatus getJournal(NotifyMessageModel notifyMessageModel){
    return getJournalStatus(notifyMessageModel.getIndex(), notifyMessageModel.getFilter());
  }

  private JournalStatus getJournalStatus(String index, String filter) {
    JournalStatus result = null;

    if ( index != null ) {
      result = JournalStatus.getByNameForApi( index );
    } else if ( filter != null ) {
      result = JournalStatus.getByNameForApi( filter );
    }

    return result;
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onMessageEvent(RemoveAllNotificationEvent event) {
    notViewedDocumentQuantity = 0;
    EventBus.getDefault().removeStickyEvent(event);
  }

}
