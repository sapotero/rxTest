package sapotero.rxtest.utils.memory.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.notification.RemoveAllNotificationEvent;

public class NotificationDismissedReceiver extends BroadcastReceiver{
  private final String TAG = this.getClass().getSimpleName();

  public NotificationDismissedReceiver() {
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    EventBus.getDefault().postSticky( new RemoveAllNotificationEvent());
  }
}
