package sapotero.rxtest.services.task;

import android.content.Context;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.utils.ISettings;
import timber.log.Timber;

// Checks network connection by getting current user info
public class CheckNetworkTask implements Runnable {

  final String TAG = CheckNetworkTask.class.getSimpleName();

  @Inject Context context;
  @Inject ISettings settings;
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
          settings.setOnline(true);
        },
        error -> {
          Timber.tag(TAG).d("Internet connectivity: false");
          settings.setOnline(false);
        });
  }
}
