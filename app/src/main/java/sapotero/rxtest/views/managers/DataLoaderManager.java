package sapotero.rxtest.views.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.auth.AuthDcCheckFailEvent;
import sapotero.rxtest.events.auth.AuthDcCheckSuccessEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckFailEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckSuccessEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckEvent;
import sapotero.rxtest.jobs.bus.AddAssistantJob;
import sapotero.rxtest.jobs.bus.AddFavoriteUsersJob;
import sapotero.rxtest.jobs.bus.AddFoldersJob;
import sapotero.rxtest.jobs.bus.AddPrimaryConsiderationJob;
import sapotero.rxtest.jobs.bus.AddTemplatesJob;
import sapotero.rxtest.jobs.bus.SyncDocumentsJob;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.AuthSignToken;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;
import sapotero.rxtest.retrofit.models.v2.V2UserInfo;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import sapotero.rxtest.views.utils.TDmodel;
import timber.log.Timber;

public class DataLoaderManager {

  private final String TAG = this.getClass().getSimpleName();

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  private Preference<String> TOKEN;
  private Preference<String> CURRENT_USER;
  private Preference<String> CURRENT_USER_ORGANIZATION;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> HOST;
  private Preference<Integer> COUNT;
  private Preference<String> CURRENT_USER_ID;


  private String processed_folder;

  private SimpleDateFormat dateFormat;
  private CompositeSubscription subscription;
  private final Context context;

  public DataLoaderManager(Context context) {
    this.context = context;

    EsdApplication.getComponent(context).inject(this);

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    initialize();
  }

  private void initialize() {
    LOGIN    = settings.getString("login");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    HOST     = settings.getString("settings_username_host");
    COUNT    = settings.getInteger("documents.count");
    CURRENT_USER = settings.getString("current_user");
    CURRENT_USER_ID = settings.getString("current_user_id");
    CURRENT_USER_ORGANIZATION = settings.getString("current_user_organization");
  }

  public void unregister(){
    if ( isRegistered() ){
      EventBus.getDefault().unregister(this);
    }
  }

  public Boolean isRegistered(){
    return EventBus.getDefault().isRegistered(this);
  }


  private void setToken( String token ){
    TOKEN.set(token);
  }

  private void setLogin( String login ){
    LOGIN.set(login);
  }

  private void setHost( String host ){
    HOST.set(host);
  }

  private void setCurrentUser( String user ){
    CURRENT_USER.set(user);
  }


  public void setCurrentUserId(String currentUserId) {
    CURRENT_USER_ID.set(currentUserId);
  }


  public void setPassword(String password) {
    PASSWORD.set(password);
  }

  private void unsubscribe(){
    if ( subscription == null ){
      subscription = new CompositeSubscription();
    }
    if (subscription.hasSubscriptions()){
      subscription.clear();
    }
  }



  public void updateAuth( String sign ){
    Timber.tag(TAG).i("updateAuth: %s", sign );

    Retrofit retrofit = new RetrofitManager( context, HOST.get(), okHttpClient).process();
    AuthService auth = retrofit.create( AuthService.class );

    Map<String, Object> map = new HashMap<>();
    map.put( "sign", sign );

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      new JSONObject( map ).toString()
    );

    Timber.tag(TAG).i("json: %s", json .toString());


    Observable<AuthSignToken> authSubscription = sign == null ? auth.getAuth( LOGIN.get(), PASSWORD.get() ) : auth.getAuthBySign(json);

    unsubscribe();
    subscription.add(

      authSubscription
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            Timber.tag(TAG).i("updateAuth: token" + data.getAuthToken());
            setToken( data.getAuthToken() );
          },
          error -> {
            Timber.tag(TAG).i("updateAuth error: %s" , error );
          }
        )
    );
  }

  public void tryToSignWithDc(String sign){
    Timber.tag(TAG).i("tryToSignWithDc: %s", sign );

    Retrofit retrofit = new RetrofitManager( context, HOST.get(), okHttpClient).process();
    AuthService auth = retrofit.create( AuthService.class );

    Map<String, Object> map = new HashMap<>();
    map.put( "sign", sign );

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      new JSONObject( map ).toString()
    );

    Timber.tag(TAG).i("json: %s", json .toString());

    unsubscribe();
    subscription.add(

      auth
        .getAuthBySign( json )
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            Timber.tag(TAG).i("tryToSignWithDc: token + " + data.getAuthToken());
            Timber.tag(TAG).i("tryToSignWithDc: login" + data.getLogin());

            setLogin( data.getLogin() );
            setToken( data.getAuthToken() );

            EventBus.getDefault().post( new AuthDcCheckSuccessEvent() );

            updateDocuments();
          },
          error -> {
            Timber.tag(TAG).i("tryToSignWithDc error: %s" , error );

            EventBus.getDefault().post( new AuthDcCheckFailEvent( error.getMessage() ) );

            // если в офлайне то всё равно идём дальше
            if ( !isOnline() ){
              EventBus.getDefault().post( new AuthDcCheckSuccessEvent() );
            }
          }
        )
    );
  }

  public void tryToSignWithLogin(String login, String password, String host) {
    Timber.v("tryToSignWithLogin %s", host);
    if (validateHost(host)) {
      EventBus.getDefault().post(new AuthLoginCheckFailEvent("Wrong Host address"));
      return;
    }

    Retrofit retrofit = new RetrofitManager(context, host, okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);

    unsubscribe();
    subscription.add(
      auth
        .getAuth(login, password)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .unsubscribeOn(Schedulers.io())
        .subscribe(
          data -> {
            Timber.tag(TAG).i("tryToSignWithLogin: token %s", data.getAuthToken());

            setHost(host);
            setLogin(login);
            setPassword(password);
            setToken(data.getAuthToken());

            EventBus.getDefault().post(new AuthLoginCheckSuccessEvent());

            updateDocuments();
          },
          error -> {
            Timber.tag(TAG).i("tryToSignWithLogin error: %s", error);
            EventBus.getDefault().post(new AuthLoginCheckFailEvent(error.getMessage()));
          }
        )
    );
  }

  private boolean validateHost(String host) {
    Boolean error = false;

    if (host == null){
      error = true;
    }

    return error;
  }

  private void updateDocuments() {

    Timber.tag(TAG).i("getAuthToken");

    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);
    DocumentsService docService = retrofit.create(DocumentsService.class);

    COUNT.set(0);

    unsubscribe();
    subscription.add(
      // получаем данные о пользователе
      auth.getUserInfoV2(LOGIN.get(), TOKEN.get())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          data -> {

            try {
              V2UserInfo user = data.get(0);
              setCurrentUser(user.getName());
              setCurrentUserId(user.getId());
              setCurrentUserOrganization(user.getOrganization());
            } catch (Exception e) {
              e.printStackTrace();
            }
          },
          error -> {
            Timber.tag("USER_INFO").e( "ERROR: %s", error);
          })
    );

    subscription.add(

      Observable.just(TOKEN.get())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .unsubscribeOn(Schedulers.io())
        // получаем шаблоны
        .concatMap(data -> auth.getTemplates(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
        .doOnNext(templates -> jobManager.addJobInBackground(new AddTemplatesJob(templates)))

        // получаем данные о пользователе
//        .concatMap(data -> auth.getUserInfo(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
//        .doOnNext(info -> {
//          setCurrentUser(info.getMe().getName());
//          setCurrentUserId(info.getMe().getId());
//          setCurrentUserId(info.getMe().getId());
//        })



        // получаем папки
        .concatMap(data -> auth.getFolders(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()))
        .doOnNext(folders -> jobManager.addJobInBackground(new AddFoldersJob(folders)))

        // получаем группу первичного рассмотрения
        .concatMap(data -> auth.getPrimaryConsiderationUsers(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()))
        .doOnNext(users -> jobManager.addJobInBackground(new AddPrimaryConsiderationJob(users)))

        // получаем группу Избранное(МП)
        .concatMap(data -> auth.getFavoriteUsers(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()))
        .doOnNext(users -> jobManager.addJobInBackground(new AddFavoriteUsersJob(users)))

        // Доработка api для возврата ВРИО/по поручению
        // https://tasks.n-core.ru/browse/MVDESD-11453
        .concatMap(data -> auth.getAssistant(LOGIN.get(), TOKEN.get(), CURRENT_USER_ID.get()).subscribeOn(Schedulers.io()))
        .doOnNext(users -> jobManager.addJobInBackground(new AddAssistantJob(users)))

        // получаем список документов по статусам
        .concatMap(data -> {
          Fields.Status[] new_filter_types = Fields.Status.INDEX;

          Observable<Fields.Status> types = Observable.from(new_filter_types);
          Observable<Documents> count = Observable
            .from(new_filter_types)
            .flatMap(status -> docService.getDocuments(LOGIN.get(), TOKEN.get(), status.getValue(), 500, 0));

          return Observable.zip(types, count, (type, docs) -> new TDmodel(type, docs.getDocuments()))
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .toList();
        })
        .doOnNext(raw -> {
          for (TDmodel data : raw) {
            Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size());

            COUNT.set( COUNT.get() + data.getDocuments().size());

            for (Document doc : data.getDocuments()) {
              String type = data.getType();
              Timber.tag(TAG).d("%s | %s", type, doc.getUid());

              jobManager.addJobInBackground(new SyncDocumentsJob(doc.getUid(), Fields.getStatus(type)));
            }
          }
          //        callback.onGetDocumentsInfoSuccess();
        })

        // получаем список обработанных документов по статусам
        .concatMap(data -> {

          processed_folder = dataStore
            .select(RFolderEntity.class)
            .where(RFolderEntity.TYPE.eq("processed"))
            .get().first().getUid();

          dateFormat = new SimpleDateFormat("dd.MM.yyyy");
          Calendar cal = Calendar.getInstance();
          cal.add(Calendar.HOUR, -48);
          String date = dateFormat.format(cal.getTime());

          Fields.Status[] new_filter_types = Fields.Status.values();

          Observable<Fields.Status> types = Observable.from(new_filter_types);
          Observable<Documents> count = Observable
            .from(new_filter_types)
            .flatMap(status -> docService.getByFolders(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0, processed_folder, date));

          return Observable.zip(types, count, (type, docs) -> new TDmodel(type, docs.getDocuments()))
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .toList();
        })
        .doOnNext(raw -> {
          for (TDmodel data : raw) {
            Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size());

            for (Document doc : data.getDocuments()) {
              String type = data.getType();
              Timber.tag(TAG).d("%s | %s", type, doc.getUid());

              jobManager.addJobInBackground(new SyncDocumentsJob(doc.getUid(), Fields.getStatus(type), processed_folder, false, true), () -> {
                Timber.e("complete");
              });
            }
          }
          //        callback.onGetProcessedInfoSuccess();
        })


        .subscribe(
          data -> {
            Timber.tag(TAG).w("subscribe %s", data);
            //          callback.onGetProcessedInfoSuccess();
          }, error -> {
            //          callback.onError(error);
          }
        )
    );

  }

  private void setCurrentUserOrganization(String organization) {
    CURRENT_USER_ORGANIZATION.set(organization);
  }

  public void updateByStatus(MainMenuItem items) {

    String sign = null;

    Retrofit retrofit = new RetrofitManager( context, HOST.get(), okHttpClient).process();
    AuthService auth = retrofit.create( AuthService.class );

    Map<String, Object> map = new HashMap<>();
    map.put( "sign", sign );

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      new JSONObject( map ).toString()
    );

    Timber.tag(TAG).i("json: %s", json .toString());


    Observable<AuthSignToken> authSubscription = auth.getAuth( LOGIN.get(), PASSWORD.get() );

    unsubscribe();
    subscription.add(

      authSubscription
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          token -> {
            Timber.tag(TAG).i("updateAuth: token" + token.getAuthToken());
            setToken( token.getAuthToken() );

            ArrayList<Fields.Status> filter_types = new ArrayList<>();

            for ( ButtonBuilder button: items.getMainMenuButtons() ){
              for ( ConditionBuilder condition: button.getConditions() ){
                if ( condition.getField().getLeftOperand() == RDocumentEntity.FILTER ){
//                  List<String> tmp = (ArrayList<String>) condition.getField().getRightOperand();
                  List<String> tmp = new ArrayList<>((Collection<? extends String>) condition.getField().getRightOperand());

                  Timber.tag(TAG).w("list: %s", tmp);

                  for (String str: tmp) {
                    filter_types.add( Fields.getStatus( str ) );
                  }
                }
              }
            }


            Retrofit retrofits = new RetrofitManager( context, HOST.get() + "/v3/", okHttpClient).process();
            DocumentsService documentsService = retrofits.create(DocumentsService.class);

            Timber.tag("updateByStatus").i( "%s ", filter_types );

            Observable<Fields.Status> types = Observable.from(filter_types);
            Observable<Documents> count = Observable
              .from(filter_types)
              .flatMap(status -> documentsService.getDocuments(LOGIN.get(), TOKEN.get(), status.getValue(), 1000, 0));

              Observable.zip( types, count, (type, docs) -> new TDmodel( type, docs.getDocuments() ))
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

                        jobManager.addJobInBackground(new SyncDocumentsJob( doc.getUid(), Fields.getStatus(type) ));
                      }
                    }
                  },
                  error -> {
                    Timber.e(error);
                  });

          },
          error -> {
            Timber.tag(TAG).i("updateAuth error: %s" , error );
          }
        )
    );


  }


  private boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnectedOrConnecting();
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(StepperDcCheckEvent event) throws Exception {
    String token = event.pin;
  }

  public void updateDocument(String uid) {
    new Handler().postDelayed( () -> {
      jobManager.addJobInBackground(new SyncDocumentsJob( uid, null ));
    }, 2000L);
  }
}
