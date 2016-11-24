package sapotero.rxtest.views.interfaces;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.jobs.bus.AddPrimaryConsiderationJob;
import sapotero.rxtest.jobs.bus.SyncDocumentsJob;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.PrimaryConsiderationService;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;
import sapotero.rxtest.retrofit.models.me.UserInfo;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.retrofit.utils.UserInfoService;
import sapotero.rxtest.views.activities.LoginActivity;
import sapotero.rxtest.views.utils.TDmodel;
import timber.log.Timber;

public class DataLoaderInterface {

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;

  private Preference<String> TOKEN;

  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> HOST;
  private Preference<String> COUNT;

  private CompositeSubscription subscription;

  private final Context context;


  private final String TAG = this.getClass().getSimpleName();
  Callback callback;

  public interface Callback {
    void onAuthTokenSuccess();
    void onAuthTokenError(Throwable error);

    void onGetUserInformationSuccess();
    void onGetUserInformationError(Throwable error);

    void onGetDocumentsCountSuccess();
    void onGetDocumentsCountError(Throwable error);

    void onGetDocumentsInfoSuccess();
    void onGetDocumentsInfoError(Throwable error);
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
    COUNT    = settings.getString("documents.count");
  }

  private void unsubscribe(){
    if ( subscription != null && subscription.hasSubscriptions() ){
      subscription.unsubscribe();
    }
    subscription = new CompositeSubscription();
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  private void saveToken( String token ){
    TOKEN.set(token);
  }

  public void getAuthToken(){


    Retrofit retrofit = new RetrofitManager( context, HOST.get(), okHttpClient).process();
    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );

    Observable<AuthToken> user = authTokenService.getAuth( LOGIN.get(), PASSWORD.get() );

    unsubscribe();
    subscription.add(
      user.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            Timber.i( "LOGIN: %s\nTOKEN: %s", LOGIN.get(), data.getAuthToken() );
            saveToken( data.getAuthToken() );

            getPrimaryConsiderationUsers();

            callback.onAuthTokenSuccess();
          },
          error -> {
            Toast.makeText( context, error.getMessage(), Toast.LENGTH_SHORT).show();
            callback.onAuthTokenError(error);
          }
        )
    );
  }



  public void getPrimaryConsiderationUsers() {
    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    PrimaryConsiderationService primaryConsiderationService = retrofit.create(PrimaryConsiderationService.class);

    Observable<ArrayList<Oshs>> info = primaryConsiderationService.getUsers( LOGIN.get(), TOKEN.get() );

    unsubscribe();
    subscription.add(
      info.subscribeOn(Schedulers.computation())
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          users -> {
            jobManager.addJobInBackground(new AddPrimaryConsiderationJob(users));
          },
          error -> {
            Timber.tag(TAG).d("ERROR " + error.getMessage());
          })
    );

  }

  public void getUserInformation() {
    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    UserInfoService userInfoService = retrofit.create(UserInfoService.class);

    Observable<UserInfo> info = userInfoService.load( LOGIN.get(), TOKEN.get() );

    unsubscribe();
    subscription.add(
      info.subscribeOn(Schedulers.computation())
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {

            settings.getString("current_user").set( data.getMe().getName() );
            // Preference<String> current_user = settings.getString("current_user");
            // String user_data = new Gson().toJson( data , UserInfo.class);
            // current_user.set( user_data );

            callback.onGetUserInformationSuccess();
          },
          error -> {
            Timber.tag(TAG).d("ERROR " + error.getMessage());
            callback.onGetUserInformationError(error);
          })
    );

  }

  public void getDocumentsCount() {

    Retrofit retrofit = new RetrofitManager(context, HOST.get() + "/v3/", okHttpClient).process();
    DocumentsService documentsService = retrofit.create(DocumentsService.class);


    String[] filter_types = context.getResources().getStringArray(R.array.FILTER_TYPES_VALUE);

    unsubscribe();
    subscription.add(
      Observable
        .from(filter_types)
        .flatMap(type -> documentsService.getDocuments(LOGIN.get(), TOKEN.get(), type, 0, 0))
        .toList()
        .subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          documents -> {
            int count = 0;

            if (documents.size() > 0) {
              for (Documents document : documents) {
                if (document.getMeta() != null && document.getMeta().getTotal() != null) {
                  count += Integer.valueOf(document.getMeta().getTotal());
                }
              }
            }

            if (count != 0) {
              COUNT.set(String.valueOf(count));
              callback.onGetDocumentsCountSuccess();
            }


          },
          error -> {
            callback.onGetDocumentsCountError(error);
          }
        )
    );
  }

  public void getDocumentsInfo(){

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);


    Retrofit retrofit = new RetrofitManager(context, HOST.get() + "/v3/", okHttpClient).process();
    DocumentsService documentsService = retrofit.create(DocumentsService.class);


    String[] filter_types = context.getResources().getStringArray(R.array.FILTER_TYPES_VALUE);


    Observable<String> types = Observable.from(filter_types);
    Observable<Documents> count = Observable
      .from(filter_types)
      .flatMap(type -> documentsService.getDocuments(LOGIN.get(), TOKEN.get(), type, 1000, 0));


    unsubscribe();
    subscription.add(
      Observable.zip( types, count, (type, docs) -> {
        return new TDmodel( type, docs.getDocuments() );
      })
        .subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .toList()
        .subscribe(
          raw -> {
            Timber.tag(TAG).i(" RECV: %s", raw.size());

            for (TDmodel data: raw) {
              Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size() );

              for (Document doc: data.getDocuments() ) {
                String type = data.getType();
                Timber.tag(TAG).d( "%s | %s", type, doc.getUid() );

                jobManager.addJobInBackground(new SyncDocumentsJob(doc.getUid(), type), () -> {
                  Timber.e("complete");
                });
                callback.onGetDocumentsInfoSuccess();
              }
            }
          },
          error -> {
            callback.onGetDocumentsInfoError(error);
          })
    );
  }

}
