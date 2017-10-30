package sapotero.rxtest.utils.memory.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import javax.inject.Inject;

import rx.subjects.PublishSubject;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.events.notification.RemoveAllNotificationEvent;
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
  @Inject ISettings settings;
  private Context appContext = EsdApplication.getApplication();
  private NotificationManagerCompat notificationManagerCompat = MainService.getNotificationManagerCompat();
  private int notViewedDocumentQuantity = 0;
  /*ограничитель для буфера уведомлений. Вызывает flush буффера при каждом эмите. Эмитится, с разной периодичностью в зависимости от SubstituteMode */
  private PublishSubject<Boolean> bufferBoundary = PublishSubject.create();
  private Handler Handler = new  Handler();

  public NotifyManager() {
    EsdApplication.getManagerComponent().inject(this);
    EventBus.getDefault().register(this);
    Handler.postDelayed(updateTimerThread, 0);
  }

  private Runnable updateTimerThread = new Runnable() {
    public void run() {
      int timeSpan;
      if(settings.getSubstituteModePreference().get()){
       timeSpan = 30_000;
      } else {
        timeSpan = 5_000;
      }

      bufferBoundary.onNext(true);
      Handler.postDelayed(this, timeSpan);
    }
  };

  public void subscribeOnNotifyEvents(PublishSubject<NotifyMessageModel> notifyPubSubject) {
    if (!notifyPubSubject.hasObservers()) {
      notifyPubSubject
        .filter(notifyMessageModel -> !notifyMessageModel.isFirstRunApp())
        .filter(notifyMessageModel -> !Objects.equals(notifyMessageModel.getSource().name(), "FOLDER"))
        .filter(notifyMessageModel -> Objects.equals(notifyMessageModel.getDocumentType().name(), "DOCUMENT"))
        .filter(notifyMessageModel -> {
          /*приводим строку index к виду JournalStatus. Проверяем в разрешенных журналах*/
          JournalStatus itemJournalStatus = getJournal(notifyMessageModel);
          return checkAllowedJournal(itemJournalStatus);
        })
        .buffer(bufferBoundary)
        .filter(notifyMessageModels -> !notifyMessageModels.isEmpty())
        .subscribe(notifyMessageModels -> {
          if(notifyMessageModels.size() > 1) {
            notViewedDocumentQuantity = notViewedDocumentQuantity + notifyMessageModels.size();
            String contentTitle = "Вам поступило новых документов: " + notifyMessageModels.size();
            String contentText = "Итого требующих рассмотрения: " + notViewedDocumentQuantity;
            generateSummaryNotifyMsg(contentTitle, contentText, 1);
            Timber.tag(TAG).e("-> SummaryNotifyMsg");
          } else {
            for (NotifyMessageModel item : notifyMessageModels){
              Document document = item.getDocument();
              String filter = item.getFilter();
              notViewedDocumentQuantity = notViewedDocumentQuantity + notifyMessageModels.size();
              String contentTitle = getTitle(getJournal(item)) + item.getDocument().getTitle();
              String contentText = "Итого требующих рассмотрения: " + notViewedDocumentQuantity;
              generateSingleNotifyMsg(contentTitle, contentText, document, filter, 1);
              Timber.tag(TAG).e("-> SingleNotifyMsg. getChanged() = %s | Md5 = %s | Uid() = %s",item.getDocument().getChanged(),item.getDocument().getMd5(),item.getDocument().getUid());
            }
          }
        }, throwable -> Timber.tag(TAG).e("throwable = " + throwable));
    }
  }

  private void generateSummaryNotifyMsg(String contentTitle, String contentText, int currentNotificationId ){
    NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
    
    Intent openIntent = MainActivity.newIntent(appContext)
      .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  );

    PendingIntent openPendingIntent = PendingIntent.getActivity(appContext, 0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    Intent intentDismiss = new Intent(appContext, NotificationDismissedReceiver.class);
    PendingIntent pendingIntentDismiss = PendingIntent.getBroadcast(appContext, 0, intentDismiss, PendingIntent.FLAG_CANCEL_CURRENT);

    builder
      .setContentTitle(contentTitle)
      .setContentText(contentText)
      .setSmallIcon(R.drawable.gerb)
      .setDeleteIntent(pendingIntentDismiss)
      .setAutoCancel(true)
      .setDefaults(Notification.DEFAULT_ALL)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setVisibility(Notification.VISIBILITY_PUBLIC)
      .setContentIntent(openPendingIntent);
    notificationManagerCompat.notify(currentNotificationId, builder.build());
  }
  private void generateSingleNotifyMsg(String contentTitle, String contentText, Document document,String filter, int currentNotificationId){
    NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);

    Intent openIntent = InfoActivity.newIntent(appContext, document, filter, currentNotificationId);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext)
      .addParentStack(InfoActivity.class)
      .addNextIntent(openIntent);
    PendingIntent pendingIntentOpenDoc = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

    Intent intentDismiss = new Intent(appContext, NotificationDismissedReceiver.class);
    PendingIntent pendingIntentDismiss = PendingIntent.getBroadcast(appContext, 0, intentDismiss, PendingIntent.FLAG_CANCEL_CURRENT);

    builder
      .setContentTitle(contentTitle)
      .setContentText(contentText)
      .setSmallIcon(R.drawable.gerb)
      .setDeleteIntent(pendingIntentDismiss)
      .setAutoCancel(true)
      .setDefaults(Notification.DEFAULT_ALL)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setVisibility(Notification.VISIBILITY_PUBLIC)
      .setContentIntent(pendingIntentOpenDoc);
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
    notificationManagerCompat.cancelAll();
    EventBus.getDefault().removeStickyEvent(event);
  }

}
