package sapotero.rxtest.retrofit.utils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
  private final String host;
  private final OkHttpClient okHttpClient;

  public RetrofitManager(String HOST, OkHttpClient okHttpClient) {
    this.host = HOST;
    this.okHttpClient = okHttpClient;
  }

  public Retrofit process() {
    return new Retrofit.Builder()
      .client(okHttpClient)
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .addConverterFactory(new ToStringConverterFactory())
      .baseUrl( this.host )
      .build();
  }
}
