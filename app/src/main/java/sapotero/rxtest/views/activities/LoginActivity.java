package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.retrofit.utils.RetrofitManager;

public class LoginActivity extends AppCompatActivity {

  @BindView(R.id.username) TextView LOGIN;
  @BindView(R.id.password) TextView PASSWORD;
  @BindView(R.id.progress) View     LOADER;

  @Inject OkHttpClient okHttpClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);
  }

  @Override
  public void onStop() {
    super.onStop();

  }

  public void tryToLogin(View view) {
    LOADER.setVisibility(ProgressBar.VISIBLE);

    Retrofit retrofit = new RetrofitManager( this, EsdApplication.HOST, okHttpClient).process();
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
