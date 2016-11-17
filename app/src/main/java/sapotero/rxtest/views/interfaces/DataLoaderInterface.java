package sapotero.rxtest.views.interfaces;

import android.content.Context;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.retrofit.models.me.UserInfo;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.retrofit.utils.UserInfoService;
import sapotero.rxtest.views.activities.LoginActivity;
import timber.log.Timber;

public class DataLoaderInterface {

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  private Preference<String> TOKEN;

  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> HOST;

  private final Context context;
  private final String TAG = this.getClass().getSimpleName();


  Callback callback;
  public interface Callback {
    void onAuthTokenSuccess();
    void onAuthTokenError(Throwable error);

    void onGetUserInformationSuccess();
    void onGetUserInformationError(Throwable error);
  }

  public DataLoaderInterface(LoginActivity loginActivity) {
    this.context = loginActivity.getApplicationContext();

    EsdApplication.getComponent(context).inject(this);

    initialize();
  }

  private void initialize() {
    LOGIN    = settings.getString("login");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    HOST     = settings.getString("settings_username_host");
  }

  private void saveToken( String token ){
    TOKEN.set(token);
  }

  private void saveUser( String token ){
    TOKEN.set(token);
  }


  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  public void getAuthToken(){

    Retrofit retrofit = new RetrofitManager( context, HOST.get(), okHttpClient).process();
    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );

    Observable<AuthToken> user = authTokenService.getAuth( LOGIN.get(), PASSWORD.get() );

    user.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.i( "LOGIN: %s\nTOKEN: %s", LOGIN.get(), data.getAuthToken() );
          saveToken( data.getAuthToken() );
          callback.onAuthTokenSuccess();
        },
        error -> {
          Toast.makeText( context, error.getMessage(), Toast.LENGTH_SHORT).show();
          callback.onAuthTokenError(error);
        }
      );
  }

  public void getUserInformation() {
    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    UserInfoService userInfoService = retrofit.create(UserInfoService.class);

    Observable<UserInfo> info = userInfoService.load( LOGIN.get(), TOKEN.get() );

    info.subscribeOn(Schedulers.computation())
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).d("ME " + data.getMe().getName() );

          Preference<String> current_user = settings.getString("current_user");
          String user_data = new Gson().toJson( data , UserInfo.class);

          Timber.tag(TAG).d("JSON %s", user_data );
          current_user.set( user_data );

          callback.onGetUserInformationSuccess();
        },
        error -> {
          Timber.tag(TAG).d("ERROR " + error.getMessage());
          callback.onGetUserInformationError(error);
        });

  }

}
