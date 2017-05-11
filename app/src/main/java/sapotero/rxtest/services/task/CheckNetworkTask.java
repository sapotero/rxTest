package sapotero.rxtest.services.task;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.service.CheckNetworkResultEvent;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.utils.Settings;
import timber.log.Timber;

// Checks network connection by getting current user info
public class CheckNetworkTask implements Runnable {

  final String TAG = CheckNetworkTask.class.getSimpleName();

  @Inject Context context;
  @Inject RxSharedPreferences settings;
  @Inject Settings settings2;
  @Inject OkHttpClient okHttpClient;

  private Preference<Boolean> IS_CONNECTED;

  public CheckNetworkTask() {
    EsdApplication.getNetworkComponent().inject(this);
    initSettings();
  }

  private void initSettings() {
    IS_CONNECTED = settings.getBoolean("isConnectedToInternet");
  }

  @Override
  public void run() {
    Retrofit retrofit = new RetrofitManager(context, settings2.getHost(), okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);

    Timber.tag(TAG).d("Checking internet connectivity");

    auth.getUserInfoV2(settings2.getLogin(), settings2.getToken())
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.computation())
      .subscribe(
        v2 -> {
          Timber.tag(TAG).d("Internet connectivity: true");
          IS_CONNECTED.set( true );
          EventBus.getDefault().post(new CheckNetworkResultEvent( true ));
        },
        error -> {
          Timber.tag(TAG).d("Internet connectivity: false");
          IS_CONNECTED.set( false );
          EventBus.getDefault().post(new CheckNetworkResultEvent( false ));
        });
  }
}
