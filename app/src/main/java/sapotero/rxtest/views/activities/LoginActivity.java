package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.views.services.AuthService;

public class LoginActivity extends AppCompatActivity {

  @BindView(R.id.username) TextView LOGIN;
  @BindView(R.id.password) TextView PASSWORD;
  @BindView(R.id.progress) View     LOADER;

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  private Observable<AuthToken> user;
  private Subscription subscription = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    startService(new Intent(this, AuthService.class));
  }

  @Override
  public void onStop() {
    super.onStop();

  }

  public void tryToLogin(View view) {
    LOADER.setVisibility(ProgressBar.VISIBLE);
    getCredentials();

  }

  public void getCredentials(){
    Retrofit retrofit = new RetrofitManager( this, Constant.HOST, okHttpClient).process();
    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );

    user = authTokenService.getAuth( LOGIN.getText().toString(), PASSWORD.getText().toString() );

    if (subscription != null){
      subscription.unsubscribe();
    }

    subscription = user.subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          String _token = data.getAuthToken();
          LOADER.setVisibility(ProgressBar.INVISIBLE);

          saveSettings(LOGIN.getText().toString(), PASSWORD.getText().toString(), _token);

          Intent intent = new Intent(this, MainActivity.class);
          startActivity(intent);

          finish();
        },
        error -> {
          Log.d( "_ERROR", error.getMessage() );
          LOADER.setVisibility(ProgressBar.INVISIBLE);
          Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
        }
      );
  }

  private void saveSettings(String LOGIN, String PASSWORD, String TOKEN) {
    Preference<String> username = settings.getString("login");
    username.set(LOGIN);

    Preference<String> password = settings.getString("password");
    password.set(PASSWORD);

    Preference<String> token = settings.getString("token");
    token.set(TOKEN);

    Preference<Integer> updated = settings.getInteger("date");
    updated.set( (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) );
  }
}
