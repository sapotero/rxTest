package sapotero.rxtest.utils.memory.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.greenrobot.eventbus.EventBus;
import sapotero.rxtest.events.notification.RemoveIdNotificationEvent;
import timber.log.Timber;

public class NotificationDismissedReceiver extends BroadcastReceiver{
  private final String TAG = this.getClass().getSimpleName();

  public NotificationDismissedReceiver() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Timber.tag(TAG).e("intent.getExtras() = "+intent.getExtras());
    int dismissedId = intent.getIntExtra("notificationId", -1);
    Timber.tag(TAG).e("dismissedId = "+ dismissedId);
    if(dismissedId != -1){
      EventBus.getDefault().postSticky( new RemoveIdNotificationEvent(dismissedId));
    } else {
      Timber.tag(TAG).e("Error. DismissedId = " + dismissedId);
    }

  }
}
