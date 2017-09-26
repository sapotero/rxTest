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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
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

  public NotifyManager() {
    EsdApplication.getManagerComponent().inject(this);
    EventBus.getDefault().register(this);
  }

  public void subscribeOnNotifyEvents(PublishSubject<NotifyMessageModel> notifyPubSubject) {
    if (!notifyPubSubject.hasObservers()) {
      notifyPubSubject
        .filter(notifyMessageModel -> !notifyMessageModel.isFirstRunApp())
        .filter(notifyMessageModel ->
          {
            Timber.tag(TAG).e("notifyMessageModel.getFilter() =" + notifyMessageModel.getFilter());
            Timber.tag(TAG).e("notifyMessageModel .getIndex() = " + notifyMessageModel .getIndex());
            Timber.tag(TAG).e("notifyMessageModel.getDocumentType() =" + notifyMessageModel.getDocumentType());
            Timber.tag(TAG).e("notifyMessageModel.getSource() = " + notifyMessageModel.getSource());
            Timber.tag(TAG).e("notifyMessageModel.getSource() = " + notifyMessageModel.getDocument().getUid());
            Timber.tag(TAG).e("notifyMessageModel.getInMemoryDocument() = " + notifyMessageModel.getInMemoryDocument());

//            Timber.tag(TAG).e("notifyMessageModel.getInMemoryDocument() = "+ notifyMessageModel.getInMemoryDocument().getState());
//            Timber.tag(TAG).e("notifyMessageModel.getInMemoryDocument().getDocument().isProcessed() = " + notifyMessageModel.getInMemoryDocument().getDocument().isProcessed());
//            Timber.tag(TAG).e("notifyMessageModel.getInMemoryDocument().getDocument().getChanged() = " + notifyMessageModel.getInMemoryDocument().getDocument().getChanged());
//            Timber.tag(TAG).e("notifyMessageModel.getInMemoryDocument().getDocument().statusCode = " + notifyMessageModel.getInMemoryDocument().getDocument().statusCode);

           return  !Objects.equals(notifyMessageModel.getSource().name(), "FOLDER");
          }
        )
        .filter(new Func1<NotifyMessageModel, Boolean>() {
          @Override
          public Boolean call(NotifyMessageModel notifyMessageModel) {
            if (notifyMessageModel.getInMemoryDocument() != null) {
              Timber.tag(TAG).e(" notifyMessageModel.getInMemoryDocument().getDocument().getChanged() = " + notifyMessageModel.getInMemoryDocument().getDocument().getChanged() );
              Timber.tag(TAG).e(" notifyMessageModel.getInMemoryDocument().getDocument().isProcessed() = " + notifyMessageModel.getInMemoryDocument().getDocument().isProcessed() );
              Timber.tag(TAG).e(" notifyMessageModel.getInMemoryDocument().hasDecision() = " + notifyMessageModel.getInMemoryDocument().hasDecision() );
              Timber.tag(TAG).e(" notifyMessageModel.getInMemoryDocument().getDocument().isFromProcessedFolder() = " + notifyMessageModel.getInMemoryDocument().getDocument().isFromProcessedFolder() );
              Timber.tag(TAG).e(" notifyMessageModel.getInMemoryDocument().getDocument().getShortDescription() = " + notifyMessageModel.getInMemoryDocument().getDocument().getShortDescription() );
            }
          return true;
          }


        })

        .filter(new Func1<NotifyMessageModel, Boolean>() {
          @Override
          public Boolean call(NotifyMessageModel notifyMessageModel) {
            return Objects.equals(notifyMessageModel.getDocumentType().name(), "DOCUMENT");
          }
        })
//        .filter(new Func1<NotifyMessageModel, Boolean>() {
//          @Override
//          public Boolean call(NotifyMessageModel notifyMessageModel) {
//            return !notifyMessageModel.getDocument().getChanged();
//          }
//        })

//        .filter(new Func1<NotifyMessageModel, Boolean>() {
//          @Override
//          public Boolean call(NotifyMessageModel notifyMessageModel) {
//            notifyMessageModel.getDocument().isProcessed();
//            return !Objects.equals(notifyMessageModel.getDocumentType().name(), "PROCESSED");
//
//          }
//        })

//        .filter(new Func1<NotifyMessageModel, Boolean>() {
//          @Override
//          public Boolean call(NotifyMessageModel notifyMessageModel) {
//            return !Objects.equals(notifyMessageModel.getFilter(), "signing");
//          }
//        })

//        .distinct(new Func1<NotifyMessageModel, String>() {
//          @Override
//          public String call(NotifyMessageModel notifyMessageModel) {
//            return notifyMessageModel.getDocument().getMd5();
//
//          }
//        })

        .filter(notifyMessageModel -> {
          /*приводим строку index к виду JournalStatus. Проверяем в разрешенных журналах*/
          JournalStatus itemJournalStatus = getJournal(notifyMessageModel);
          return checkAllowedJournal(itemJournalStatus);
        })
        .buffer(5 ,TimeUnit.SECONDS)
        .filter(notifyMessageModels -> !notifyMessageModels.isEmpty())
//        .flatMap(new Func1<List<NotifyMessageModel>, Observable<List<NotifyMessageModel>>>() {
//          @Override
//          public Observable<List<NotifyMessageModel>> call(List<NotifyMessageModel> notifyMessageModels) {
//
//            return null;
//          }
//        })


        .subscribe(notifyMessageModels -> {
          for (NotifyMessageModel item : notifyMessageModels){
            Document document = item.getDocument();
            Timber.tag(TAG).e("document.getMd5(); ="+document.getMd5()+"   document.getUid(); ="+document.getUid());
          }


          if(notifyMessageModels.size() > 1) {
            notViewedDocumentQuantity = notViewedDocumentQuantity + notifyMessageModels.size();
            String contentTitle = "Вам поступило новых документов: " + notifyMessageModels.size();
            String contentText = "Итого требующих рассмотрения: " + notViewedDocumentQuantity;
            generateSummaryNotifyMsg(contentTitle, contentText, 1);
          } else {
            for (NotifyMessageModel item : notifyMessageModels){
              Document document = item.getDocument();
              String filter = item.getFilter();
              notViewedDocumentQuantity = notViewedDocumentQuantity + notifyMessageModels.size();
              String contentTitle = getTitle(getJournal(item)) + item.getDocument().getTitle();
              String contentText = "Итого требующих рассмотрения: " + notViewedDocumentQuantity;
              generateSingleNotifyMsg(contentTitle, contentText, document, filter, 1);
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
      .setSmallIcon(R.drawable.ic_error)
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
      .setSmallIcon(R.drawable.ic_error)
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
