package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.EsdApplication;
import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.retrofit.AuthTokenService;

public class LoginActivity extends AppCompatActivity {

  private EditText LOGIN;
  private EditText PASSWORD;
  private static View LOADER;

  private OkHttpClient okHttpClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    LOADER   = findViewById(R.id.loginProgress);
    LOGIN    = (EditText) findViewById(R.id.username);
    PASSWORD = (EditText) findViewById(R.id.password);

    okHttpClient = new OkHttpClient.Builder()
        .readTimeout(60,    TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .addNetworkInterceptor(
            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
        )
        .build();
  }

  public void tryToLogin(View view) {
    LOADER.setVisibility(ProgressBar.VISIBLE);

    Retrofit retrofit = new Retrofit.Builder()
        .client(okHttpClient)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl( EsdApplication.HOST )
        .build();
    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );

    Observable<AuthToken> user = authTokenService.getAuth( LOGIN.getText().toString(), PASSWORD.getText().toString() );

    user.subscribeOn( Schedulers.newThread() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
            data -> {
              String _token = data.getAuthToken();
              LOADER.setVisibility(ProgressBar.INVISIBLE);

              Intent intent = new Intent(this, MainActivity.class);
              intent.putExtra( EsdApplication.LOGIN,    LOGIN.getText().toString() );
              intent.putExtra( EsdApplication.PASSWORD, PASSWORD.getText().toString() );
              intent.putExtra( EsdApplication.TOKEN,    _token );
              startActivity(intent);

              finish();
            },
            error -> {
              Log.d( "_ERROR", error.getMessage() );
              LOADER.setVisibility(ProgressBar.INVISIBLE);
              Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
  }
}
