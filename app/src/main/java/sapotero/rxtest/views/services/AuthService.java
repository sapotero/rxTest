package sapotero.rxtest.views.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.bus.UpdateAuthTokenEvent;
import timber.log.Timber;

public class AuthService extends Service {
  @Inject JobManager jobManager;
  @Inject RxSharedPreferences settings;

  private String TOKEN;

  public AuthService() {
  }

  final String TAG = AuthService.class.getSimpleName();

  public void onCreate() {
    super.onCreate();

    if ( !EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().register(this);
    }
    EsdApplication.getComponent(this).inject(this);

    Timber.tag(TAG).d("onCreate");
  }

  public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.tag(TAG).d("onStartCommand");
    return super.onStartCommand(intent, flags, startId);
  }

  public void onDestroy() {
    super.onDestroy();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }

    Timber.tag(TAG).d("onDestroy");
  }

  public IBinder onBind(Intent intent) {
    Timber.tag(TAG).d("onBind");
    return null;
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateAuthTokenEvent event) {
    TOKEN = event.message;

    Toast.makeText(getApplicationContext(), "SERVICE " + TOKEN, Toast.LENGTH_SHORT).show();

    Timber.tag(TAG + " onMessageEvent TOKEN").v( TOKEN );

    saveSettings(TOKEN);
  }

  private void saveSettings(String TOKEN) {

    Preference<String> token = settings.getString("token");
    token.set(TOKEN);

    Preference<Integer> updated = settings.getInteger("date");
    updated.set( (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) );
  }
}
