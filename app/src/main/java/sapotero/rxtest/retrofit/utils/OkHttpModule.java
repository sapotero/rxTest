package sapotero.rxtest.retrofit.utils;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
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
import sapotero.rxtest.utils.ISettings;

@Module
public class OkHttpModule {
  @Provides
  @NetworkScope
  OkHttpClient provideOkHttpModule(ISettings settings) {
    return  new OkHttpClient.Builder()
      .readTimeout(60,    TimeUnit.SECONDS)
      .connectTimeout(60, TimeUnit.SECONDS)
      .addNetworkInterceptor(
        new HttpLoggingInterceptor().setLevel(
          HttpLoggingInterceptor.Level.BASIC
        )
      )
      .addNetworkInterceptor(
        new HttpLoggingInterceptor()
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

          try {
            Response response = chain.proceed(request);

            // resolved https://tasks.n-core.ru/browse/MVDESD-13625
            // Если не авторизовано, то заново логиниться

            if ( response.code() == HttpURLConnection.HTTP_UNAUTHORIZED ) {
              settings.setOnline(false);
              settings.setUnauthorized(true);
            } else {
              settings.setOnline(true);
              settings.setUnauthorized(false);
            }

            return response;

          } catch (IOException e) {
            if (e instanceof UnknownHostException) {
              settings.setOnline(false);
            }
            throw e;
          }
        })
      .build();
  }

}
