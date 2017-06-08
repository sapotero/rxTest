package sapotero.rxtest.services.task;

import android.content.Context;

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
  @Inject Settings settings;
  @Inject OkHttpClient okHttpClient;

  public CheckNetworkTask() {
    EsdApplication.getNetworkComponent().inject(this);
  }

  @Override
  public void run() {
    Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);

    Timber.tag(TAG).d("Checking internet connectivity");

    auth.getUserInfoV2(settings.getLogin(), settings.getToken())
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.computation())
      .subscribe(
        v2 -> {
          Timber.tag(TAG).d("Internet connectivity: true");
          EventBus.getDefault().post(new CheckNetworkResultEvent( true ));
          settings.setOnline(true);
        },
        error -> {
          Timber.tag(TAG).d("Internet connectivity: false");
          EventBus.getDefault().post(new CheckNetworkResultEvent( false ));
          settings.setOnline(false);
        });
  }
}
