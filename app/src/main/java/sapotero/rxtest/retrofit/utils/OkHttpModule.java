package sapotero.rxtest.retrofit.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class OkHttpModule {
  @Provides
  @Singleton
  OkHttpClient provideOkHttpModule(Context context) {
    return  new OkHttpClient.Builder()
      .cache(
        new Cache( context.getCacheDir(), 10 * 1024 * 1024)
      )
      .readTimeout(60,    TimeUnit.SECONDS)
      .connectTimeout(60, TimeUnit.SECONDS)
      .addNetworkInterceptor(
        new HttpLoggingInterceptor().setLevel(
          HttpLoggingInterceptor.Level.BASIC
        )
      )
      .addInterceptor(chain -> {
        Request request = chain.request();

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if ( networkInfo != null && networkInfo.isConnected() ) {
          request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build();
        } else {
          request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
        }

        return chain.proceed(request);
      })
      .addInterceptor(
        chain -> {
          Request original = chain.request();
          HttpUrl originalHttpUrl = original.url();

          HttpUrl url = originalHttpUrl.newBuilder()
            .addQueryParameter("request_uid",  UUID.randomUUID().toString() )
            .build();

          Request.Builder requestBuilder = original.newBuilder().url(url);

          Request request = requestBuilder.build();
          return chain.proceed(request);
        })
      .build();
  }
}
