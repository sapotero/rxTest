package sapotero.rxtest.retrofit.utils;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import sapotero.rxtest.application.scopes.NetworkScope;
import sapotero.rxtest.utils.Settings;

@Module
public class OkHttpModule {
  @Provides
  @NetworkScope
  OkHttpClient provideOkHttpModule(Settings settings) {
    return  new OkHttpClient.Builder()
      .readTimeout(60,    TimeUnit.SECONDS)
      .connectTimeout(60, TimeUnit.SECONDS)
      .addNetworkInterceptor(
        new HttpLoggingInterceptor().setLevel(
          HttpLoggingInterceptor.Level.BASIC
        )
      )
      // refactor
      // for test only!!!
//      .addInterceptor( new NetworkSlowdown() )

      .addInterceptor( new StethoInterceptor())
      .addInterceptor(
        chain -> {
          Request original = chain.request();
          HttpUrl originalHttpUrl = original.url();

          HttpUrl url = originalHttpUrl.newBuilder()
            .addQueryParameter("request_uid",  UUID.randomUUID().toString() )
            .build();

          Request.Builder requestBuilder = original.newBuilder().url(url);
          Request request = requestBuilder.build();

          Response response = chain.proceed(request);

          // resolved https://tasks.n-core.ru/browse/MVDESD-13625
          // Если не авторизовано, то заново логиниться

          // Закомментировано, т.к. если сервер возвращает код 500, у нас ставится Не в сети
//          if ( response.code() == HttpURLConnection.HTTP_OK ) {
//            settings.setOnline(true);
//          } else {
//            settings.setOnline(false);
//          }

          if ( response.code() == HttpURLConnection.HTTP_UNAUTHORIZED ) {
            settings.setUnauthorized(true);
          } else {
            settings.setUnauthorized(false);
          }

          return response;
        })
      .build();
  }

}
