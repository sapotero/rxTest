package sapotero.rxtest.managers;

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
import java.util.HashMap;
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
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.auth.AuthDcCheckFailEvent;
import sapotero.rxtest.events.auth.AuthDcCheckSuccessEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckFailEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckSuccessEvent;
import sapotero.rxtest.events.document.UpdateUnprocessedDocumentsEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckEvent;
import sapotero.rxtest.jobs.bus.AddAssistantJob;
import sapotero.rxtest.jobs.bus.AddFavoriteUsersJob;
import sapotero.rxtest.jobs.bus.AddFoldersJob;
import sapotero.rxtest.jobs.bus.AddPrimaryConsiderationJob;
import sapotero.rxtest.jobs.bus.SyncDocumentsJob;
import sapotero.rxtest.jobs.bus.SyncFavoritesDocumentsJob;
import sapotero.rxtest.jobs.bus.SyncProcessedDocumentsJob;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.AuthSignToken;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;
import sapotero.rxtest.retrofit.models.v2.v2UserOshs;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.views.adapters.utils.TDmodel;
import sapotero.rxtest.views.menu.fields.MainMenuButton;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
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

  private SimpleDateFormat dateFormat;
  private CompositeSubscription subscription;
  private final Context context;
  private ArrayList<String> v2Journals;
  private ArrayList<String> v2Statuses;

  public DataLoaderManager(Context context) {
    this.context = context;

    EsdApplication.getComponent(context).inject(this);

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);


    initialize();

  }

  private void initV2() {
//    v2Journals = new ArrayList<String>();
//    v2Journals.add("incoming_documents");
//    v2Journals.add("citizen_requests");
//    v2Journals.add("incoming_orders");
//    v2Journals.add("outgoing_documents");
//    v2Journals.add("orders");
//    v2Journals.add("orders_ddo");
//
//    v2Statuses = new ArrayList<String>();
//    v2Statuses.add("for_report");
//    v2Statuses.add("for_primary_consideration");
//    v2Statuses.add("for_sign");
//    v2Statuses.add("for_approval");

    ArrayList<String> indexes = new ArrayList<String>();
    indexes.add("incoming_documents_production_db_core_cards_incoming_documents_cards");
    indexes.add("outgoing_documents_production_db_core_cards_outgoing_documents_cards");
    indexes.add("orders_production_db_core_cards_orders_cards");
    indexes.add("orders_ddo_production_db_core_cards_orders_ddo_cards");
    indexes.add("incoming_orders_production_db_core_cards_incoming_orders_cards");
    indexes.add("citizen_requests_production_db_core_cards_citizen_requests_cards");

    ArrayList<String> statuses = new ArrayList<String>();
    statuses.add("sent_to_the_report");
    statuses.add("primary_consideration");

    ArrayList<String> sp = new ArrayList<String>();
    sp.add("approval");
    sp.add("signing");


    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    DocumentsService docService = retrofit.create(DocumentsService.class);

    if (subscription != null) {
      subscription.clear();
    }

    for (String index: indexes ) {
      for (String status: statuses ) {



        subscription.add(
          docService
            .getDocumentsByIndexes(LOGIN.get(), TOKEN.get(), index, status, 500)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
              data -> {
                if (data.getDocuments().size() > 0){
                  for (Document doc: data.getDocuments() ) {
                    jobManager.addJobInBackground( new SyncDocumentsJob(doc.getUid(), index, status) );
                  }
                }
              },
              error -> {
                Timber.tag(TAG).e(error);
              })
        );
      }
    }

    for (String code: sp ) {
      subscription.add(
        docService
          .getDocuments(LOGIN.get(), TOKEN.get(), code, 500, 0)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            data -> {
              if (data.getDocuments().size() > 0){
                for (Document doc: data.getDocuments() ) {
                  jobManager.addJobInBackground( new SyncDocumentsJob(doc.getUid(), code) );
                }
              }
            },
            error -> {
              Timber.tag(TAG).e(error);
            })
      );
    }


    AuthService auth = retrofit.create(AuthService.class);
    // получаем папки
    subscription.add(
      auth.getFolders(LOGIN.get(), TOKEN.get())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
      .subscribe( data -> {
        jobManager.addJobInBackground(new AddFoldersJob(data));
      }, error -> {
        Timber.tag(TAG).e(error);
      })
    );


    subscription.add(
      auth.getPrimaryConsiderationUsers(LOGIN.get(), TOKEN.get())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( data -> {
          jobManager.addJobInBackground(new AddPrimaryConsiderationJob(data));
        }, error -> {
          Timber.tag(TAG).e(error);
        })
    );

    // получаем группу Избранное(МП)
    subscription.add(
      auth.getFavoriteUsers(LOGIN.get(), TOKEN.get())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( data -> {
          jobManager.addJobInBackground(new AddFavoriteUsersJob(data));
        }, error -> {
          Timber.tag(TAG).e(error);
        })
    );

    // Доработка api для возврата ВРИО/по поручению
    // https://tasks.n-core.ru/browse/MVDESD-11453
    subscription.add(
      auth.getAssistant(LOGIN.get(), TOKEN.get(), CURRENT_USER_ID.get())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( data -> {
          jobManager.addJobInBackground(new AddAssistantJob(data));
        }, error -> {
          Timber.tag(TAG).e(error);
        })
    );

    updateProcessed();
    updateFavorites();

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

            updateDocuments(null);
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

            updateDocuments(null);
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

  private void updateDocuments(MainMenuItem items) {


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
              v2UserOshs user = data.get(0);
              setCurrentUser(user.getName());
              setCurrentUserId(user.getId());
              setCurrentUserOrganization(user.getOrganization());

              initV2();
            } catch (Exception e) {
              e.printStackTrace();
            }
          },
          error -> {
            Timber.tag("USER_INFO").e( "ERROR: %s", error);
          })
    );

//    subscription.add(
//
//      Observable.just(TOKEN.get())
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .unsubscribeOn(Schedulers.io())
//        // получаем шаблоны
//
//        .concatMap(data -> auth.getTemplates(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
//        .doOnNext(templates -> jobManager.addJobInBackground(new AddTemplatesJob(templates)))
//
//        // получаем данные о пользователе
////        .concatMap(data -> auth.getUserInfo(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
////        .doOnNext(info -> {
////          setCurrentUser(info.getMe().getName());
////          setCurrentUserId(info.getMe().getId());
////          setCurrentUserId(info.getMe().getId());
////        })
//
//
//
//        // получаем папки
//        .concatMap(data -> auth.getFolders(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()))
//        .doOnNext(folders -> jobManager.addJobInBackground(new AddFoldersJob(folders)))
//
//        // получаем группу первичного рассмотрения
//        .concatMap(data -> auth.getPrimaryConsiderationUsers(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()))
//        .doOnNext(users -> jobManager.addJobInBackground(new AddPrimaryConsiderationJob(users)))
//
//        // получаем группу Избранное(МП)
//        .concatMap(data -> auth.getFavoriteUsers(LOGIN.get(), TOKEN.get()).subscribeOn(Schedulers.io()))
//        .doOnNext(users -> jobManager.addJobInBackground(new AddFavoriteUsersJob(users)))
//
//        // Доработка api для возврата ВРИО/по поручению
//        // https://tasks.n-core.ru/browse/MVDESD-11453
//        .concatMap(data -> auth.getAssistant(LOGIN.get(), TOKEN.get(), CURRENT_USER_ID.get()).subscribeOn(Schedulers.io()))
//        .doOnNext(users -> jobManager.addJobInBackground(new AddAssistantJob(users)))
//
//        // получаем список документов по статусам
//        .concatMap(data -> {
////          Fields.Status[] new_filter_types = Fields.Status.INDEX;
//
//          ArrayList<Fields.Status> new_filter_types = new ArrayList<Fields.Status>();
//
//          if (items == null){
////            new_filter_types.add( Fields.Status.SENT_TO_THE_REPORT );
////            new_filter_types.add( Fields.Status.PRIMARY_CONSIDERATION );
//            new_filter_types.add( Fields.Status.APPROVAL );
//            new_filter_types.add( Fields.Status.SIGNING );
//          } else {
//            if (items.getButtons() != null && items.getButtons().length > 0){
//
//              ArrayList<ButtonBuilder> getButtonList = items.getButtonList();
//
//              if (getButtonList != null && getButtonList.size() > 0){
//
//                for (ButtonBuilder builder: getButtonList) {
//                  if ( builder.isActive() ){
//                    Timber.tag(TAG).d("ACTIVE BUTTON INDEX: %s", builder.getIndex() );
//                    Timber.tag(TAG).d("ACTIVE BUTTON: %s", MainMenuButton.getByIndex( builder.getIndex() ).toString() );
//
//                    MainMenuButton button = MainMenuButton.getByIndex(builder.getIndex());
//
//                    if (button != null) {
//
//                      getButtonType(new_filter_types, button);
//                    }
//                  }
//                }
//              } else {
//                for (MainMenuButton button: items.getButtons() ) {
//                  getButtonType(new_filter_types, button);
//                }
//              }
//
//            }
//          }
//
//          Timber.tag(TAG).d("new_filter_types: %s", new_filter_types );
//
//          Observable<Fields.Status> types = Observable.from(new_filter_types);
//          Observable<Documents> count = Observable
//            .from(new_filter_types)
//            .flatMap(status -> docService.getDocuments(LOGIN.get(), TOKEN.get(), status.getValue(), 500, 0));
//
//          return Observable.zip(types, count, (type, docs) -> new TDmodel(type, docs.getDocuments()))
//            .subscribeOn(Schedulers.computation())
//            .observeOn(AndroidSchedulers.mainThread())
//            .toList();
//        })
//        .doOnNext(raw -> {
//          for (TDmodel data : raw) {
//            Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size());
//
//            COUNT.set( COUNT.get() + data.getDocuments().size());
//
//            for (Document doc : data.getDocuments()) {
//              String type = data.getType();
//              Timber.tag(TAG).d("%s | %s", type, doc.getUid());
//
//              jobManager.addJobInBackground( new SyncDocumentsJob(doc.getUid(), Fields.getStatus(type)) );
//            }
//          }
//        })
//        .subscribe(
//          data -> {
//            Timber.tag(TAG).w("subscribe %s", data);
//            updateUnprocessed();
//          }, error -> {
//            //          callback.onError(error);
//          }
//        )
//    );

  }

  private void updateUnprocessed() {
    EventBus.getDefault().post( new UpdateUnprocessedDocumentsEvent() );
  }

  private void getButtonType(ArrayList<Fields.Status> new_filter_types, MainMenuButton button) {
    switch (button){
      case APPROVAL:
        new_filter_types.add( Fields.Status.APPROVAL );
        break;
      case PRIMARY_CONSIDERATION:
        new_filter_types.add( Fields.Status.PRIMARY_CONSIDERATION );
        break;
      case ASSIGN:
        new_filter_types.add( Fields.Status.SIGNING );
        break;
      case PERFORMANCE:
        new_filter_types.add( Fields.Status.SENT_TO_THE_REPORT );
        break;
      default:
        new_filter_types.add( Fields.Status.SENT_TO_THE_REPORT );
        new_filter_types.add( Fields.Status.SIGNING );
        new_filter_types.add( Fields.Status.PRIMARY_CONSIDERATION );
        new_filter_types.add( Fields.Status.APPROVAL );
        break;
    }
  }

  private void setCurrentUserOrganization(String organization) {
    CURRENT_USER_ORGANIZATION.set(organization);
  }

  private void updateProcessed(){
    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    DocumentsService docService = retrofit.create(DocumentsService.class);

    String processed_folder = dataStore
      .select(RFolderEntity.class)
      .where(RFolderEntity.TYPE.eq("processed"))
      .get().first().getUid();

    dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, -30*24);
    String date = dateFormat.format(cal.getTime());

    Timber.tag("PROCESSED").e("FROM DATE: %s | %s", date, processed_folder);

    Fields.Status[] new_filter_types = Fields.Status.values();

    Observable<Fields.Status> types = Observable.from(new_filter_types);
    Observable<Documents> count = Observable
      .from(new_filter_types)
      .flatMap(status -> docService.getByFolders(LOGIN.get(), TOKEN.get(), status.getValue(), 500, 0, processed_folder, date));

//    unsubscribe();
    subscription.add(
        Observable.zip(types, count, (type, docs) -> new TDmodel(type, docs.getDocuments()))
          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .toList()
          .doOnNext(raw -> {
            for (TDmodel data : raw) {
              Timber.tag(TAG).i(" DocumentType: %s | %s", data.getType(), data.getDocuments().size());

              for (Document doc : data.getDocuments()) {
                String type = data.getType();
                Timber.tag("PROCESSED").e("TYPE: %s | UID: %s", type, doc.getUid());

                jobManager.addJobInBackground(new SyncProcessedDocumentsJob(doc.getUid(), Fields.getStatus(type), processed_folder ) );
              }
            }
          })
          .subscribe(
            data -> {
              Timber.tag(TAG).w("subscribe %s", data);
              //          callback.onGetProcessedInfoSuccess();
            }, error -> {
              Timber.tag(TAG).e(error);
            }
          )
    );
  }

  private void updateFavorites(){
    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    DocumentsService docService = retrofit.create(DocumentsService.class);

    String favorites_folder = dataStore
      .select(RFolderEntity.class)
      .where(RFolderEntity.TYPE.eq("favorites"))
      .get().first().getUid();

//    unsubscribe();
    subscription.add(
      docService.getByFolders(LOGIN.get(), TOKEN.get(), null, 500, 0, favorites_folder, null)
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            if ( data.getDocuments().size() > 0 ) {
              Timber.tag("FAVORITES").e("DOCUMENTS COUNT: %s", data.getDocuments().size() );
              for (Document doc : data.getDocuments()) {
                jobManager.addJobInBackground(new SyncFavoritesDocumentsJob(doc.getUid(), Fields.Status.PROCESSED, favorites_folder ) );
              }
            }
          }, error -> {
            Timber.tag(TAG).e(error);
          }
        )
    );
  }

  public void updateByStatus(MainMenuItem items) {
    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);

    Timber.tag(TAG).e("UPDATE BY STATUS: %s", items.getName() );

    String sign = "";

    try {
      sign = MainService.getFakeSign(context, "12341234", null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Map<String, Object> map = new HashMap<>();
    map.put( "sign", sign );

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      new JSONObject( map ).toString()
    );

    Observable<AuthSignToken> authSubscription = settings.getBoolean("SIGN_WITH_DC").get() == null ? auth.getAuth( LOGIN.get(), PASSWORD.get() ) : auth.getAuthBySign(json);


    switch ( items ){
      case FAVORITES:
        authSubscription
          .subscribeOn( Schedulers.io() )
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(
            token -> {
              Timber.tag(TAG).i("updateAuth: token" + token.getAuthToken());
              setToken(token.getAuthToken());
              updateFavorites();
            },
            error -> {
              Timber.tag("getAuth").e( "ERROR: %s", error);
            }
          );
        break;
      case PROCESSED:
        authSubscription
          .subscribeOn( Schedulers.io() )
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(
            token -> {
              Timber.tag(TAG).i("updateAuth: token" + token.getAuthToken());
              setToken(token.getAuthToken());
              updateProcessed();
            },
            error -> {
              Timber.tag("getAuth").e( "ERROR: %s", error);
            }
          );
        break;
      default:
        updateByDefault(authSubscription, items);
        break;
    }

  }

  private void updateByDefault(Observable<AuthSignToken> auth, MainMenuItem items) {
    unsubscribe();
    subscription.add(
      auth
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          token -> {
            Timber.tag(TAG).i("updateAuth: token" + token.getAuthToken());
            setToken(token.getAuthToken());
            updateDocuments( MainMenuItem.ALL );
            updateProcessed();
            updateFavorites();
          },
          error -> {
            Timber.tag("getAuth").e( "ERROR: %s", error);
          }
        )
    );
  }


  private boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnectedOrConnecting();
  }

  public void updateDocument(String uid) {
    new Handler().postDelayed( () -> {
      jobManager.addJobInBackground(new SyncDocumentsJob( uid, "" ));
    }, 2000L);
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(StepperDcCheckEvent event) throws Exception {
    String token = event.pin;
  }
}
