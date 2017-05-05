package sapotero.rxtest.services.task;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.schedulers.Schedulers;
import sapotero.rxtest.events.service.CheckNetworkResultEvent;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import timber.log.Timber;

// Checks network connection by getting current user info
public class CheckNetworkTask implements Runnable {

  final String TAG = CheckNetworkTask.class.getSimpleName();

  private Context context;
  private RxSharedPreferences settings;
  private OkHttpClient okHttpClient;

  private Preference<String> HOST;
  private Preference<String> LOGIN;
  private Preference<String> TOKEN;
  private Preference<Boolean> IS_CONNECTED;

  public CheckNetworkTask(Context context, RxSharedPreferences settings, OkHttpClient okHttpClient) {
    this.context = context;
    this.settings = settings;
    this.okHttpClient = okHttpClient;

    initSettings();
  }

  private void initSettings() {
    HOST = settings.getString("settings_username_host");
    LOGIN = settings.getString("login");
    TOKEN = settings.getString("token");
    IS_CONNECTED = settings.getBoolean("isConnectedToInternet");
  }

  @Override
  public void run() {
    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);

    Timber.tag(TAG).d("Checking internet connectivity");

    auth.getUserInfoV2(LOGIN.get(), TOKEN.get())
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
