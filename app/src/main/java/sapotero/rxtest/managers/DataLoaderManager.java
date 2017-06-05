package sapotero.rxtest.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Tuple;
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
import sapotero.rxtest.events.auth.AuthDcCheckFailEvent;
import sapotero.rxtest.events.auth.AuthDcCheckSuccessEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckFailEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckSuccessEvent;
import sapotero.rxtest.events.stepper.load.StepperDocumentCountReadyEvent;
import sapotero.rxtest.jobs.bus.CreateAssistantJob;
import sapotero.rxtest.jobs.bus.CreateFavoriteUsersJob;
import sapotero.rxtest.jobs.bus.CreateFoldersJob;
import sapotero.rxtest.jobs.bus.CreatePrimaryConsiderationJob;
import sapotero.rxtest.jobs.bus.CreateTemplatesJob;
import sapotero.rxtest.jobs.bus.CreateUrgencyJob;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.AuthSignToken;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.v2.v2UserOshs;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.views.menu.fields.MainMenuButton;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

public class DataLoaderManager {

  private final String TAG = this.getClass().getSimpleName();

  @Inject OkHttpClient okHttpClient;
  @Inject Settings settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject MemoryStore store;

  private SimpleDateFormat dateFormat;
  private CompositeSubscription subscription;
  private CompositeSubscription subscriptionInitV2;
  private CompositeSubscription subscriptionUpdateAuth;
  private final Context context;
  private ArrayList<String> v2Journals;
  private ArrayList<String> v2Statuses;

  // Network request counter. Incremented when request created, decremented when response received.
  private int requestCount;

  private int jobCount;
  private int jobCountFavorites;

  public DataLoaderManager(Context context) {

    this.context = context;
    EsdApplication.getManagerComponent().inject(this);

  }

  private void initV2() {
    Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();

    AuthService auth = retrofit.create(AuthService.class);

    unsubscribeInitV2();

    subscriptionInitV2.add(
      // получаем данные о пользователе
      auth.getUserInfoV2(settings.getLogin(), settings.getToken())
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          v2 -> {
//            try {
              v2UserOshs user = v2.get(0);
              setCurrentUser(user.getName());
              setCurrentUserId(user.getId());
              setCurrentUserOrganization(user.getOrganization());

              // получаем папки
              subscriptionInitV2.add(
                auth.getFolders(settings.getLogin(), settings.getToken())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe( data -> {
                    jobManager.addJobInBackground(new CreateFoldersJob(data));
                  }, error -> {
                    Timber.tag(TAG).e(error);
                  })
              );


              subscriptionInitV2.add(
                auth.getPrimaryConsiderationUsers(settings.getLogin(), settings.getToken())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe( data -> {
                    jobManager.addJobInBackground(new CreatePrimaryConsiderationJob(data));
                  }, error -> {
                    Timber.tag(TAG).e(error);
                  })
              );

              // загрузка срочности
              subscriptionInitV2.add(
                auth.getUrgency(settings.getLogin(), settings.getToken(), "urgency")
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe( urgencies -> {
                    jobManager.addJobInBackground(new CreateUrgencyJob(urgencies));
                  }, error -> {
                    Timber.tag(TAG).e(error);
                  })
              );

              // загрузка шаблонов резолюции
              subscriptionInitV2.add(
                auth.getTemplates(settings.getLogin(), settings.getToken(), null)
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe( templates -> {
                    jobManager.addJobInBackground(new CreateTemplatesJob(templates, null));
                  }, error -> {
                    Timber.tag(TAG).e(error);
                  })
              );

              // загрузка шаблонов отклонения
              subscriptionInitV2.add(
                auth.getTemplates(settings.getLogin(), settings.getToken(), "rejection")
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe( templates -> {
                    jobManager.addJobInBackground(new CreateTemplatesJob(templates, "rejection"));
                  }, error -> {
                    Timber.tag(TAG).e(error);
                  })
              );

              // получаем группу Избранное(МП)
              subscriptionInitV2.add(
                auth.getFavoriteUsers(settings.getLogin(), settings.getToken())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe( data -> {
                    jobManager.addJobInBackground(new CreateFavoriteUsersJob(data));
                  }, error -> {
                    Timber.tag(TAG).e(error);
                  })
              );

              // Доработка api для возврата ВРИО/по поручению
              // https://tasks.n-core.ru/browse/MVDESD-11453
              subscriptionInitV2.add(
                auth.getAssistant(settings.getLogin(), settings.getToken(), settings.getCurrentUserId())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe( data -> {
                    jobManager.addJobInBackground(new CreateAssistantJob(data));
                  }, error -> {
                    Timber.tag(TAG).e(error);
                  })
              );

//            } catch (Exception e) {
//              e.printStackTrace();
//            }
          },
          error -> {
            Timber.tag("USER_INFO").e( "ERROR: %s", error);
          })
    );

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
    settings.setToken(token);
  }

  private void setLogin( String login ){
    settings.setLogin(login);
  }

  private void setHost( String host ){
    settings.setHost(host);
  }

  private void setCurrentUser( String user ){
    settings.setCurrentUser(user);
  }

  private void setCurrentUserId(String currentUserId) {
    settings.setCurrentUserId(currentUserId);
  }

  private void setCurrentUserOrganization(String organization) {
    settings.setCurrentUserOrganization(organization);
  }

  public void setPassword(String password) {
    settings.setPassword(password);
  }


  private boolean validateHost(String host) {
    Boolean error = false;

    if (host == null){
      error = true;
    }

    return error;
  }

  private boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnectedOrConnecting();
  }

  private void unsubscribe(){
    if ( !isSubscriptionExist() ){
      subscription = new CompositeSubscription();
    }
    if (subscription.hasSubscriptions()){
      subscription.clear();
    }
  }

  private boolean isSubscriptionExist() {
    return subscription != null;
  }

  private void unsubscribeInitV2() {
    if ( subscriptionInitV2 == null ){
      subscriptionInitV2 = new CompositeSubscription();
    }
    if (subscriptionInitV2.hasSubscriptions()){
      subscriptionInitV2.clear();
    }
  }

  private void unsubscribeUpdateAuth() {
    if ( subscriptionUpdateAuth == null ){
      subscriptionUpdateAuth = new CompositeSubscription();
    }
    if (subscriptionUpdateAuth.hasSubscriptions()){
      subscriptionUpdateAuth.clear();
    }
  }


  public void updateAuth( String sign ){
    Timber.tag(TAG).i("updateAuth: %s", sign );

    Retrofit retrofit = new RetrofitManager( context, settings.getHost(), okHttpClient).process();
    AuthService auth = retrofit.create( AuthService.class );

    Map<String, Object> map = new HashMap<>();
    map.put( "sign", sign );

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      new JSONObject( map ).toString()
    );

    Timber.tag(TAG).i("json: %s", json .toString());


    Observable<AuthSignToken> authSubscription = getAuthSubscription();

    unsubscribeUpdateAuth();

    subscriptionUpdateAuth.add(
      authSubscription
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            Timber.tag(TAG).i("updateAuth: token" + data.getAuthToken());
            setToken( data.getAuthToken() );

            initV2();
          },
          error -> {
            Timber.tag(TAG).i("updateAuth error: %s" , error );
          }
        )
    );
  }

  public void tryToSignWithDc(String sign){
    Timber.tag(TAG).i("tryToSignWithDc: %s", sign );

    Retrofit retrofit = new RetrofitManager( context, settings.getHost(), okHttpClient).process();
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

            initV2();

            updateByCurrentStatus(MainMenuItem.ALL, null, false);
//            updateByCurrentStatus(MainMenuItem.ALL, null, true);

//            updateFavoritesAndProcessed();
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
        .subscribeOn(Schedulers.computation())
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

            initV2();
            updateByCurrentStatus(MainMenuItem.ALL, null, false);
//            updateFavoritesAndProcessed();
          },
          error -> {
            Timber.tag(TAG).i("tryToSignWithLogin error: %s", error);
            EventBus.getDefault().post(new AuthLoginCheckFailEvent(error.getMessage()));
          }
        )
    );
  }

  public void updateByCurrentStatus(MainMenuItem items, MainMenuButton button, Boolean firstRunShared) {
    Timber.tag(TAG).e("updateByCurrentStatus: %s %s", items, button );

    if ( !isSubscriptionExist() ) {
      unsubscribe();
    }

    if (items == MainMenuItem.PROCESSED){
      updateProcessed();
    } else if (items == MainMenuItem.FAVORITES){
      updateFavorites();
    } else {

      ArrayList<String> indexes = new ArrayList<>();

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

      if (button != null) {
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
      }


      // обновляем всё
      if (items == MainMenuItem.ALL || items.getIndex() == 11 ){
        statuses.add("primary_consideration");
        statuses.add("sent_to_the_report");
        sp.add("approval");
        sp.add("signing");

        indexes.add("citizen_requests_production_db_core_cards_citizen_requests_cards");
        indexes.add("incoming_documents_production_db_core_cards_incoming_documents_cards");
        indexes.add("orders_ddo_production_db_core_cards_orders_ddo_cards");
        indexes.add("orders_production_db_core_cards_orders_cards");
        indexes.add("outgoing_documents_production_db_core_cards_outgoing_documents_cards");
        indexes.add("incoming_orders_production_db_core_cards_incoming_orders_cards");
      }

      if (button == null){
        statuses.add("primary_consideration");
        statuses.add("sent_to_the_report");
        sp.add("approval");
        sp.add("signing");
      }




      Timber.tag(TAG).e("data: %s %s", indexes, statuses );

      Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
      DocumentsService docService = retrofit.create(DocumentsService.class);

      // resolved https://tasks.n-core.ru/browse/MVDESD-13343
      // если общие документы
//
//      boolean shared = false;
//      if (items.getIndex() == 11){
//        shared = true;
//      }


      jobManager.cancelJobsInBackground(null, TagConstraint.ANY, "SyncDocument");

      requestCount = 0;
      jobCount = 0;
      settings.setJobCount(0);


      if (subscription != null) {
        subscription.clear();
      }

      for (String index: indexes ) {
        for (String status: statuses ) {
          requestCount++;

          subscription.add(
            docService
              .getDocumentsByIndexes(settings.getLogin(), settings.getToken(), index, status, null , 500)
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(
                data -> {
                  requestCount--;
                  if (data.getDocuments().size() > 0){

//                    for (Document doc: data.getDocuments() ) {
//                      store.process(doc, index, status);
//                      jobCount++;
//                    }

//                    Observable<List<String>> api = Observable
//                      .from(data.getDocuments())
//                      .map(Document::getUid)
//                      .toList();

//                    store.process( api, status, index );
                    HashMap<String, Document> doc_hash = new HashMap<String, Document>();
                    for (Document doc: data.getDocuments() ) {
                      doc_hash.put( doc.getUid(), doc );
                    }
                    store.process( doc_hash, status, index );
                  }


                  updatePrefJobCount();
                },
                error -> {
                  requestCount--;
                  updatePrefJobCount();
                  Timber.tag(TAG).e(error);
                })
          );
        }
      }

      for (String code: sp ) {
        requestCount++;

        subscription.add(
          docService
            .getDocuments(settings.getLogin(), settings.getToken(), code, null , 500, 0)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
              data -> {

                requestCount--;
                if (data.getDocuments().size() > 0){
//                  for (Document doc: data.getDocuments() ) {
////                    Timber.tag(TAG).i("project -> %s | %s", doc.getUid(), code);
//                    store.process(doc, null, code);
//                    jobCount++;
//
//                  }
//
//                  Observable<List<String>> api = Observable
//                    .from(data.getDocuments())
//                    .map(Document::getUid)
//                    .toList();

//                  store.process( api, code );

                  HashMap<String, Document> doc_hash = new HashMap<String, Document>();
                  for (Document doc: data.getDocuments() ) {
                    doc_hash.put( doc.getUid(), doc );
                  }
                  store.process( doc_hash, code, null );
                }
                updatePrefJobCount();
              },
              error -> {
                requestCount--;
                updatePrefJobCount();
                Timber.tag(TAG).e(error);
              })
        );
      }



    }
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13145
  // Передача количества документов в экран загрузки
  private void updatePrefJobCount() {
    if (0 == requestCount) {
      // Received responses on all requests, now jobCount contains total initial job count value.
      // Update counter in preferences with this value.
      settings.addJobCount(jobCount);
      EventBus.getDefault().post( new StepperDocumentCountReadyEvent() );
    }
  }

  private boolean isDocumentMd5Changed(String uid, String md5) {

    Boolean result = false;

    Tuple doc = dataStore
      .select(RDocumentEntity.MD5)
      .where(RDocumentEntity.UID.eq(uid))
      .get()
      .firstOrNull();

    if (doc != null){
      if (doc.get(0).equals(md5)){
        result = true;
      }
    }

    return result;
  }

  private boolean isExist(Document doc) {
    return dataStore
      .count(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(doc.getUid()))
      .get().value() > 0;
  }

  private Observable<AuthSignToken> getAuthSubscription() {
    Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);

    Observable<AuthSignToken> authSubscription;

    if ( settings.isSignedWithDc() ){

      String sign = "";
      try {
        sign = MainService.getFakeSign( settings.getPin(), null );
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
      authSubscription = auth.getAuth( settings.getLogin(), settings.getPassword() );
    }

    return authSubscription;
  }

  public void updateDocument(String uid) {
//    jobManager.addJobInBackground(new UpdateDocumentJob( uid, "" ));
  }

  public void updateFavorites() {
//    Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
//    DocumentsService docService = retrofit.create(DocumentsService.class);
//
//    RFolderEntity favorites_folder = dataStore
//            .select(RFolderEntity.class)
//            .where(RFolderEntity.TYPE.eq("favorites"))
//            .and(RFolderEntity.USER.eq( settings.getLogin() ))
//            .get().firstOrNull();
//
//    if ( favorites_folder != null ) {
//      Timber.tag(TAG).e("FAVORITES EXIST!");
//
//      jobCountFavorites = 0;
//
//      subscription.add(
//        docService.getByFolders(settings.getLogin(), settings.getToken(), null, 500, 0, favorites_folder.getUid(), null)
//          .subscribeOn( Schedulers.io() )
//          .observeOn( AndroidSchedulers.mainThread() )
//          .subscribe(
//            data -> {
//              if ( data.getDocuments().size() > 0 ) {
//                Timber.tag("FAVORITES").e("DOCUMENTS COUNT: %s", data.getDocuments().size() );
//                for (Document doc : data.getDocuments()) {
//                  if ( isExist(doc) ) {
//                    if ( !isDocumentMd5Changed( doc.getUid(), doc.getMd5() ) ) {
//                      jobCountFavorites++;
//                      jobManager.addJobInBackground(new UpdateDocumentJob( doc.getUid() ) );
//                    }
//                  } else {
//                    jobCountFavorites++;
//                    jobManager.addJobInBackground( new CreateFavoriteDocumentsJob( doc.getUid(), favorites_folder.getUid() ) );
//                  }
//                }
//              }
//              settings.addJobCount(jobCountFavorites);
//            }, error -> {
//              Timber.tag(TAG).e(error);
//            }
//          )
//      );
//    }
  }

  public void updateProcessed() {
//    Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
//    DocumentsService docService = retrofit.create(DocumentsService.class);
//
//    RFolderEntity processed_folder = dataStore
//      .select(RFolderEntity.class)
//      .where(RFolderEntity.TYPE.eq("processed"))
//      .and(RFolderEntity.USER.eq( settings.getLogin() ))
//      .get().firstOrNull();
//
//    if ( processed_folder != null ) {
//
//      dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("RU"));
//      Calendar cal = Calendar.getInstance();
//      cal.add(Calendar.HOUR, -20*24);
//      String date = dateFormat.format(cal.getTime());
//
//      Timber.tag(TAG).e("PROCESSED EXIST! %s", date);
//
//      subscription.add(
//        docService.getByFolders(settings.getLogin(), settings.getToken(), null, 500, 0, processed_folder.getUid(), date)
//          .subscribeOn( Schedulers.io() )
//          .observeOn( AndroidSchedulers.mainThread() )
//          .subscribe(
//            data -> {
//              if ( data.getDocuments().size() > 0 ) {
//                Timber.tag("PROCESSED").e("DOCUMENTS COUNT: %s", data.getDocuments().size() );
//                for (Document doc : data.getDocuments()) {
//                  if ( isExist(doc) ) {
//                    if ( !isDocumentMd5Changed( doc.getUid(), doc.getMd5() ) ) {
//                      jobManager.addJobInBackground( new UpdateDocumentJob( doc.getUid() ) );
//                    }
//                  } else {
//                    jobManager.addJobInBackground( new CreateProcessedDocumentsJob( doc.getUid(), processed_folder.getUid() ) );
//                  }
//                }
//              }
//            }, error -> {
//              Timber.tag(TAG).e(error);
//            }
//          )
//      );
//    }
  }

//  @Subscribe(threadMode = ThreadMode.BACKGROUND)
//  public void onMessageEvent(StepperDcCheckEvent event) throws Exception {
//    String token = event.pin;
//  }
}
