package sapotero.rxtest.retrofit.utils;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
  private final Context context;
  private final String host;
  private final OkHttpClient okHttpClient;

  public RetrofitManager(Context context, String HOST, OkHttpClient okHttpClient) {
    this.context = context;
    this.host = HOST;
    this.okHttpClient = okHttpClient;
  }

  public Retrofit process() {
    return new Retrofit.Builder()
      .client(okHttpClient)
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( this.host )
      .build();
  }
}
