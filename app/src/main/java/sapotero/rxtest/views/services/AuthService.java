package sapotero.rxtest.views.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import timber.log.Timber;

public class AuthService extends Service {
//  @Inject JobManager jobManager;
//  @Inject RxSharedPreferences settings;

  private String TOKEN;

  public AuthService() {
  }

  final String TAG = AuthService.class.getSimpleName();

  public void onCreate() {
    super.onCreate();

//    if ( !EventBus.getDefault().isRegistered(this) ){
//      EventBus.getDefault().register(this);
//    }
//    EsdApplication.getComponent(this).inject(this);

    Timber.tag(TAG).d("onCreate");
  }
//
  public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.tag(TAG).d("onStartCommand");
    return super.onStartCommand(intent, flags, startId);
  }
//
  public void onDestroy() {
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
//
//    if ( EventBus.getDefault().isRegistered(this) ){
//      EventBus.getDefault().unregister(this);
//    }
//
//    Timber.tag(TAG).d("onDestroy");
//  }
//
//  public IBinder onBind(Intent intent) {
//    Timber.tag(TAG).d("onBind");
//    return null;
//  }
//
//  @Subscribe(threadMode = ThreadMode.MAIN)
//  public void onMessageEvent(UpdateAuthTokenEvent event) {
//    TOKEN = event.message;
//
//    Toast.makeText(getApplicationContext(), "SERVICE " + TOKEN, Toast.LENGTH_SHORT).show();
//
//    Timber.tag(TAG + " onMessageEvent TOKEN").v( TOKEN );
//
//    saveSettings(TOKEN);
//  }
//
//  private void saveSettings(String TOKEN) {
//
//    Preference<String> token = settings.getString("token");
//    token.set(TOKEN);
//
//    Preference<Integer> updated = settings.getInteger("date");
//    updated.set( (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) );
//  }
}
