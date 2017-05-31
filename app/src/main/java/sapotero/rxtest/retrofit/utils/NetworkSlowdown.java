package sapotero.rxtest.retrofit.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;
import timber.log.Timber;

public class NetworkSlowdown implements Interceptor {
  private String TAG = this.getClass().getSimpleName();

  @Override
  public Response intercept(Chain chain) throws IOException {
    this.sleep();
    Timber.tag(TAG).d("Network slowdown done. Proceeding chain");

    return chain.proceed(chain.request());
  }

  private void sleep()
  {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Timber.tag(TAG).e("Error %s", e);
    }
  }
}