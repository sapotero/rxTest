package sapotero.rxtest.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
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
import sapotero.rxtest.jobs.bus.AddTemplatesJob;
import sapotero.rxtest.jobs.bus.InvalidateDocumentsJob;
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

    subscription.add(
      auth.getTemplates(LOGIN.get(), TOKEN.get())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( templates -> {
          jobManager.addJobInBackground(new AddTemplatesJob(templates));
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

//    updateProcessed();
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
      .and(RFolderEntity.USER.eq( settings.getString("current_user").get() ))
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

  public void updateByCurrentStatus(MainMenuItem items, MainMenuButton button) {
    Timber.tag(TAG).e("updateByCurrentStatus: %s %s", items, button );

    ArrayList<String> indexes = new ArrayList<String>();

    switch (items){
      case CITIZEN_REQUESTS:
        indexes.add("citizen_requests_production_db_core_cards_citizen_requests_cards");
        break;
      case INCOMING_DOCUMENTS:
        indexes.add("incoming_documents_production_db_core_cards_incoming_documents_cards");
        break;
      case ORDERS_DDO:
        indexes.add("orders_ddo_production_db_core_cards_orders_ddo_cards");
        break;
      case ORDERS:
        indexes.add("orders_production_db_core_cards_orders_cards");
        break;
      case IN_DOCUMENTS:
        indexes.add("outgoing_documents_production_db_core_cards_outgoing_documents_cards");
        break;
      case INCOMING_ORDERS:
        indexes.add("incoming_orders_production_db_core_cards_incoming_orders_cards");
        break;
    }

    ArrayList<String> sp = new ArrayList<String>();
    ArrayList<String> statuses = new ArrayList<String>();
    switch (button){
      case APPROVAL:
        sp.add("approval");
        break;
      case ASSIGN:
        sp.add("signing");
        break;
      case PRIMARY_CONSIDERATION:
        statuses.add("primary_consideration");
        break;
      case PERFORMANCE:
        statuses.add("sent_to_the_report");
        break;

    }




    Timber.tag(TAG).e("data: %s %s", indexes, statuses );

    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    DocumentsService docService = retrofit.create(DocumentsService.class);

    jobManager.cancelJobsInBackground(null, TagConstraint.ANY);

    unsubscribe();

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
                    jobManager.addJobInBackground( new SyncDocumentsJob(doc.getUid(), index, status, true) );
                  }
                  if ( settings.getString("is_first_run").get() != null ){
                    jobManager.addJobInBackground( new InvalidateDocumentsJob(data.getDocuments(), index, status) );
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


  }


  public void updateByStatus(MainMenuItem items) {
    Timber.tag(TAG).e("UPDATE BY STATUS: %s", items.getName() );

    Observable<AuthSignToken> authSubscription = getAuthSubscription();


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
//              updateProcessed();
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

  private Observable<AuthSignToken> getAuthSubscription() {
    Retrofit retrofit = new RetrofitManager(context, HOST.get(), okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);

    Observable<AuthSignToken> authSubscription;

    if ( settings.getBoolean("SIGN_WITH_DC").get() ){

      String sign = "";
      try {
        sign = MainService.getFakeSign( context, settings.getString("PIN").get(), null );
      } catch (Exception e) {
        e.printStackTrace();
      }

      Map<String, Object> map = new HashMap<>();
      map.put( "sign", sign );

      RequestBody json = RequestBody.create(
        MediaType.parse("application/json"),
        new JSONObject( map ).toString()
      );

      authSubscription = auth.getAuthBySign(json);
    } else {
      authSubscription = auth.getAuth( LOGIN.get(), PASSWORD.get() );
    }
    
    return authSubscription;
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
//            updateProcessed();
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
    jobManager.addJobInBackground(new SyncDocumentsJob( uid, "" ));
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(StepperDcCheckEvent event) throws Exception {
    String token = event.pin;
  }
}
