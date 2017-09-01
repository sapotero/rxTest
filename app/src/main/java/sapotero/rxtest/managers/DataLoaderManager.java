package sapotero.rxtest.managers;

import android.content.Context;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
import sapotero.rxtest.db.requery.models.RAssistantEntity;
import sapotero.rxtest.db.requery.models.RColleagueEntity;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFavoriteUserEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.models.RPrimaryConsiderationEntity;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.events.auth.AuthDcCheckFailEvent;
import sapotero.rxtest.events.auth.AuthDcCheckSuccessEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckFailEvent;
import sapotero.rxtest.events.auth.AuthLoginCheckSuccessEvent;
import sapotero.rxtest.events.stepper.load.StepperDocumentCountReadyEvent;
import sapotero.rxtest.events.utils.ErrorReceiveTokenEvent;
import sapotero.rxtest.events.utils.ReceivedTokenEvent;
import sapotero.rxtest.events.view.UpdateDrawerEvent;
import sapotero.rxtest.jobs.bus.CreateAssistantJob;
import sapotero.rxtest.jobs.bus.CreateColleagueJob;
import sapotero.rxtest.jobs.bus.CreateFavoriteUsersJob;
import sapotero.rxtest.jobs.bus.CreateFoldersJob;
import sapotero.rxtest.jobs.bus.CreatePrimaryConsiderationJob;
import sapotero.rxtest.jobs.bus.CreateTemplatesJob;
import sapotero.rxtest.jobs.bus.CreateUrgencyJob;
import sapotero.rxtest.jobs.bus.DeleteProcessedImageJob;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.AuthSignToken;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;
import sapotero.rxtest.retrofit.models.v2.v2UserOshs;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import sapotero.rxtest.views.menu.fields.MainMenuButton;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

public class DataLoaderManager {

  private final String TAG = this.getClass().getSimpleName();

  @Inject OkHttpClient okHttpClient;
  @Inject ISettings settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject MemoryStore store;

  private SimpleDateFormat dateFormat;
  private CompositeSubscription subscription;
  private CompositeSubscription subscriptionFavorites;
  private CompositeSubscription subscriptionProcessed;
  private CompositeSubscription subscriptionInitV2;
  private CompositeSubscription subscriptionUpdateAuth;
  private final Context context;

  // Network request counter. Incremented when request created, decremented when response received.
  private int requestCount;

  private boolean isDocumentCountSent;

  private Documents favoritesData;
  private Documents processedData;
  private boolean favoritesDataLoaded = false;
  private boolean processedDataLoaded = false;
  private boolean favoritesDataLoading = false;
  private boolean processedDataLoading = false;
  private boolean processFavoritesData = false;
  private boolean processProcessedData = false;

  public DataLoaderManager(Context context) {

    this.context = context;
    EsdApplication.getManagerComponent().inject(this);

  }

  public void initV2(boolean loadAllDocs) {
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
              Timber.tag("LoadSequence").d("Received user info");
//            try {
              v2UserOshs user = v2.get(0);
              setCurrentUser(user.getName());
              setCurrentUserId(user.getId());
              setCurrentUserOrganization(user.getOrganization());
              setCurrentUserPosition(user.getPosition());
              setCurrentUserImage(user.getImage());

              deleteUsers();
              deleteTemplates();

            // получаем папки
              subscriptionInitV2.add(
                auth.getFolders(settings.getLogin(), settings.getToken())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe( data -> {
                    Timber.tag("LoadSequence").d("Received list of folders");
                    jobManager.addJobInBackground(new CreateFoldersJob(data));
                    loadAllDocs( loadAllDocs );
                  }, error -> {
                    Timber.tag(TAG).e(error);
                    loadAllDocs( loadAllDocs );
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
            // resolved https://tasks.n-core.ru/browse/MVDESD-11453
            subscriptionInitV2.add(
              auth.getAssistantByHeadId(settings.getLogin(), settings.getToken(), settings.getCurrentUserId())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( data -> {
                  jobManager.addJobInBackground(new CreateAssistantJob(data));
                }, error -> {
                  Timber.tag(TAG).e(error);
                })
            );

            // resolved https://tasks.n-core.ru/browse/MVDESD-13711
            subscriptionInitV2.add(
              auth.getAssistantByAssistantId(settings.getLogin(), settings.getToken(), settings.getCurrentUserId())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( data -> {
                  jobManager.addJobInBackground(new CreateAssistantJob(data));
                }, error -> {
                  Timber.tag(TAG).e(error);
                })
            );

            // resolved https://tasks.n-core.ru/browse/MVDESD-13752
            // Добавить в боковую панель список коллег
            subscriptionInitV2.add(
              auth.getColleagues(settings.getLogin(), settings.getToken())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( data -> {
                  jobManager.addJobInBackground(new CreateColleagueJob(data));
                }, error -> {
                  Timber.tag(TAG).e(error);
                  EventBus.getDefault().post( new UpdateDrawerEvent() );
                })
            );

          },
          error -> {
            Timber.tag("USER_INFO").e( "ERROR: %s", error);
            loadAllDocs( loadAllDocs );
          })
    );

  }

  private void deleteUsers() {
    dataStore
      .delete(RAssistantEntity.class)
      .where(RAssistantEntity.USER.eq(settings.getLogin()))
      .get().value();

    dataStore
      .delete(RFavoriteUserEntity.class)
      .where(RFavoriteUserEntity.USER.eq(settings.getLogin()))
      .get().value();

    dataStore
      .delete(RPrimaryConsiderationEntity.class)
      .where(RPrimaryConsiderationEntity.USER.eq(settings.getLogin()))
      .get().value();

    dataStore
      .delete(RColleagueEntity.class)
      .where(RColleagueEntity.USER.eq(settings.getLogin()))
      .get().value();
  }

  private void deleteTemplates() {
    dataStore
      .delete(RTemplateEntity.class)
      .where(RTemplateEntity.USER.eq(settings.getLogin()))
      .get().value();
  }

  private void loadAllDocs(boolean load) {
    if ( load ) {
      updateByCurrentStatus(MainMenuItem.ALL, null);
    }
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

  private void setCurrentUserPosition(String position) {
    settings.setCurrentUserPosition(position);
  }

  private void setCurrentUserImage(String image) {
    settings.setCurrentUserImage(image);
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


  public void updateAuth( String sign, boolean sendEvent ){
    if ( settings.isUpdateAuthStarted() ) {
      return;
    }

    settings.setUpdateAuthStarted( true );

    if ( settings.isSubstituteMode() && !settings.isSignedWithDc() ) {
      // В режиме замещения, если вошли по логину, то меняем логин на логин основного пользователя,
      // чтобы правильно сформировался запрос на получение токена
      swapLogin();
    }

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

    if ( settings.isSubstituteMode() && !settings.isSignedWithDc() ) {
      // Запрос на получение токена уже сформирован, меняем логин обратно на логин коллеги
      swapLogin();
    }

    unsubscribeUpdateAuth();

    subscriptionUpdateAuth.add(
      authSubscription
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            Timber.tag(TAG).i("updateAuth: token" + data.getAuthToken());
            setToken( data.getAuthToken() );

            if ( !settings.isSubstituteMode() ) {
              if ( sendEvent ) {
                EventBus.getDefault().post( new ReceivedTokenEvent() );
              }

              initV2(false);
              settings.setUpdateAuthStarted( false );

            } else {
              getColleagueToken();
            }
          },
          error -> {
            Timber.tag(TAG).i("updateAuth error: %s" , error );

            if ( sendEvent ) {
              EventBus.getDefault().post( new ErrorReceiveTokenEvent() );
            }

            settings.setUpdateAuthStarted( false );
          }
        )
    );
  }

  private void swapLogin() {
    String tempLogin = settings.getLogin();
    settings.setLogin( settings.getOldLogin() );
    settings.setOldLogin( tempLogin );
  }

  private void getColleagueToken() {
    Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
    AuthService auth = retrofit.create(AuthService.class);

    // Запрос на получение токена коллеги выполняется от имени основного пользователя
    auth.switchToColleague(settings.getColleagueId(), settings.getOldLogin(), settings.getToken())
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        colleagueResponse -> {
          // Логин основного пользователя уже сохранен в oldLogin, поэтому только обновляем логин и токен коллеги
          settings.setLogin( colleagueResponse.getLogin() );
          settings.setToken( colleagueResponse.getAuthToken() );
          initV2(false);
          EventBus.getDefault().post( new ReceivedTokenEvent() );
          settings.setUpdateAuthStarted( false );
        },
        error -> {
          Timber.tag(TAG).e(error);
          EventBus.getDefault().post( new ErrorReceiveTokenEvent() );
          settings.setUpdateAuthStarted( false );
        }
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

            initV2(true);

//            updateByCurrentStatus(MainMenuItem.ALL, null, false);
//            updateByCurrentStatus(MainMenuItem.ALL, null, true);

//            updateFavoritesAndProcessed();
          },
          error -> {
            Timber.tag(TAG).i("tryToSignWithDc error: %s" , error );
            EventBus.getDefault().post( new AuthDcCheckFailEvent( error.getMessage() ) );
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
        .subscribe(
          data -> {
            Timber.tag(TAG).i("tryToSignWithLogin: token %s", data.getAuthToken());

            setHost(host);
            setLogin(login);
            setPassword(password);
            setToken(data.getAuthToken());

            EventBus.getDefault().post(new AuthLoginCheckSuccessEvent());

            initV2(true);
//            updateByCurrentStatus(MainMenuItem.ALL, null, false);
//            updateFavoritesAndProcessed();
          },
          error -> {
            Timber.tag(TAG).i("tryToSignWithLogin error: %s", error);
            EventBus.getDefault().post(new AuthLoginCheckFailEvent(error.getMessage()));
          }
        )
    );
  }

  public void updateByCurrentStatus(MainMenuItem items, MainMenuButton button) {
    Timber.tag(TAG).e("updateByCurrentStatus: %s %s", items, button );

    if ( !isSubscriptionExist() ) {
      unsubscribe();
    }

    favoritesDataLoaded = false;
    processedDataLoaded = false;
    favoritesDataLoading = false;
    processedDataLoading = false;

    if (items == MainMenuItem.PROCESSED){
      updateProcessed( true );
    } else if (items == MainMenuItem.FAVORITES) {
      updateFavorites( true );
    } else {

      ArrayList<String> indexes = new ArrayList<>();

      switch (items) {
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
        switch (button) {
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
      if (items == MainMenuItem.ALL || items.getIndex() == 11 || items == MainMenuItem.ON_CONTROL) {
        if ( !statuses.contains("primary_consideration") ) {
          statuses.add("primary_consideration");
        }
        if ( !statuses.contains( "sent_to_the_report" ) ) {
          statuses.add("sent_to_the_report");
        }
        if ( !sp.contains( "approval" ) ) {
          sp.add("approval");
        }
        if ( !sp.contains( "signing" ) ) {
          sp.add("signing");
        }

        indexes.add("citizen_requests_production_db_core_cards_citizen_requests_cards");
        indexes.add("incoming_documents_production_db_core_cards_incoming_documents_cards");
        indexes.add("orders_ddo_production_db_core_cards_orders_ddo_cards");
        indexes.add("orders_production_db_core_cards_orders_cards");
        indexes.add("outgoing_documents_production_db_core_cards_outgoing_documents_cards");
        indexes.add("incoming_orders_production_db_core_cards_incoming_orders_cards");

        checkImagesToDelete();
      }

      if (button == null) {
        if ( !statuses.contains("primary_consideration") ) {
          statuses.add("primary_consideration");
        }
        if ( !statuses.contains( "sent_to_the_report" ) ) {
          statuses.add("sent_to_the_report");
        }
        if ( !sp.contains( "approval" ) && items == MainMenuItem.APPROVE_ASSIGN ) {
          sp.add("approval");
        }
        if ( !sp.contains( "signing" ) && items == MainMenuItem.APPROVE_ASSIGN ) {
          sp.add("signing");
        }
      }


      Timber.tag(TAG).e("data: %s %s", indexes, statuses);

      Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
      DocumentsService docService = retrofit.create(DocumentsService.class);

      // resolved https://tasks.n-core.ru/browse/MVDESD-13343
      // если общие документы
      //
      // boolean shared = false;
      // if (items.getIndex() == 11){
      //   shared = true;
      // }


      jobManager.cancelJobsInBackground(null, TagConstraint.ANY, "SyncDocument");

      requestCount = indexes.size() * statuses.size() + sp.size();
      isDocumentCountSent = false;
      settings.setTotalDocCount(0);
      settings.setDocProjCount(0);

      if (subscription != null) {
        subscription.clear();
      }


      for (String index: indexes ) {
        for (String status: statuses ) {

          String login = settings.getLogin();

          subscription.add(
            docService
              .getDocumentsByIndexes(settings.getLogin(), settings.getToken(), index, status, null , 500, getYears())
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(
                data -> {
                  Timber.tag("LoadSequence").d("Received list of documents");
                  requestCount--;
                  updateDocCount( data, true );
                  checkAndSendCountReady();
                  if ( Objects.equals( login, settings.getLogin() ) ) {
                    // Обрабатываем полученный список только если логин не поменялся (при входе/выходе в режим замещения)
                    Timber.tag("LoadSequence").d("Processing list of documents");
                    processDocuments( data, status, index, null, DocumentType.DOCUMENT );
                  }
                },
                error -> {
                  requestCount--;
                  checkAndSendCountReady();
                  Timber.tag(TAG).e(error);
                })
          );
        }
      }


      for (String code : sp) {

        String login = settings.getLogin();

        subscription.add(
          docService
            .getDocuments(settings.getLogin(), settings.getToken(), code, null , 500, 0, getYears())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
              data -> {
                Timber.tag("LoadSequence").d("Received list of projects");
                requestCount--;
                updateDocCount( data, true );
                checkAndSendCountReady();
                if ( Objects.equals( login, settings.getLogin() ) ) {
                  // Обрабатываем полученный список только если логин не поменялся (при входе/выходе в режим замещения)
                  Timber.tag("LoadSequence").d("Processing list of projects");
                  processDocuments( data, code, null, null, DocumentType.DOCUMENT );
                }
              },
              error -> {
                requestCount--;
                checkAndSendCountReady();
                Timber.tag(TAG).e(error);
              })
        );
      }
    }
  }

  private void checkImagesToDelete() {
    Timber.tag(TAG).e( "checkImagesToDelete" );

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.PROCESSED_DATE.ne( 0 ) )
      .and(RDocumentEntity.CONTROL.eq(false))
      .get().toObservable()
      .map(RDocumentEntity::getUid)
      .toList()
      .observeOn(Schedulers.computation())
      .subscribeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.tag(TAG).e("DELETE UID: %s", data.size() );
          for (String uid : data) {
            jobManager.addJobInBackground( new DeleteProcessedImageJob(uid) );
          }
        },
        Timber::e
      );
  }

  @Nullable
  private List<String> getYears() {
    return settings.getYears().size() == 4 ? null : new ArrayList<>(settings.getYears());
  }

  private void processDocuments(Documents data, String status, String index, String folder, DocumentType documentType) {
    if (data.getDocuments().size() > 0){
      HashMap<String, Document> doc_hash = new HashMap<>();

      for (Document doc: data.getDocuments() ) {
        doc_hash.put( doc.getUid(), doc );
      }

      if (documentType == DocumentType.DOCUMENT) {
        store.process( doc_hash, status, index );
      } else {
        store.process( doc_hash, folder, documentType );
      }
    }
  }

  private void updateDocCount(Documents data, boolean isDocProj) {
    int total;

    try {
      total = Integer.valueOf( data.getMeta() != null ? data.getMeta().getTotal() : "0" );
    } catch (NumberFormatException e) {
      total = 0;
    }

    settings.addTotalDocCount( total );

    if ( isDocProj ) {
      settings.addDocProjCount( total );
    }
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13145
  // Передача количества документов в экран загрузки
  private void checkAndSendCountReady() {
    if (0 == requestCount && !isDocumentCountSent) {
      // Received responses on all requests, now jobCount contains total initial job count value.
      // Send event to update progress bar in login activity.
      isDocumentCountSent = true;
      Timber.tag("LoadSequence").d("Sending StepperDocumentCountReadyEvent");
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

      authSubscription= Observable.just("")
        .map( data -> {

          String sign = "";
          try {
            sign = MainService.getFakeSign( settings.getPin(), null );
          } catch (Exception e) {
            e.printStackTrace();
          }

          Map<String, Object> map = new HashMap<>();
          map.put( "sign", sign );

          return RequestBody.create(
            MediaType.parse("application/json"),
            new JSONObject( map ).toString()
          );
        })
        .flatMap(auth::getAuthBySign);

    } else {
      authSubscription = auth.getAuth( settings.getLogin(), settings.getPassword() );
    }

    return authSubscription;
  }

  public void updateDocument(String uid) {
//    jobManager.addJobInBackground(new UpdateDocumentJob( uid, "" ));
  }

  public void updateFavorites(boolean processLoadedData) {

    processFavoritesData = processLoadedData;

    Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
    DocumentsService docService = retrofit.create(DocumentsService.class);

    RFolderEntity favorites_folder = dataStore
            .select(RFolderEntity.class)
            .where(RFolderEntity.TYPE.eq("favorites"))
            .and(RFolderEntity.USER.eq( settings.getLogin() ))
            .get().firstOrNull();

    if ( favorites_folder != null ) {
      Timber.tag(TAG).e("FAVORITES EXIST!");

      if ( favoritesDataLoaded ) {
        Timber.tag("LoadSequence").d("List of favorites already loaded, quit loading");
        if ( processFavoritesData ) {
          Timber.tag("LoadSequence").d("Processing previously loaded list of favorites");
          processFavorites(favorites_folder);
        }
        return;
      }

      if ( favoritesDataLoading ) {
        Timber.tag("LoadSequence").d("List of favorites loading already started, quit loading");
        return;
      }

      Timber.tag("LoadSequence").d("Loading list of favorites");
      favoritesDataLoading = true;

      unsubscribeFavorites();

      String login = settings.getLogin();

      subscriptionFavorites.add(
        docService.getByFolders(settings.getLogin(), settings.getToken(), null, 500, 0, favorites_folder.getUid(), null)
          .subscribeOn( Schedulers.io() )
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(
            data -> {
              Timber.tag("LoadSequence").d("Received list of favorites");
              Timber.tag("FAVORITES").e("DOCUMENTS COUNT: %s", data.getDocuments().size() );
              favoritesData = data;
              favoritesDataLoaded = true;
              updateDocCount( favoritesData, false );
              if ( processFavoritesData ) {
                if ( Objects.equals( login, settings.getLogin() ) ) {
                  // Обрабатываем полученный список только если логин не поменялся (при входе/выходе в режим замещения)
                  Timber.tag("LoadSequence").d("Processing list of favorites");
                  processFavorites(favorites_folder);
                }
              } else {
                Timber.tag("LoadSequence").d("processLoadedData = false, quit processing list of favorites");
              }
            }, error -> {
              Timber.tag(TAG).e(error);
              favoritesDataLoading = false;
            }
          )
      );
    }
  }

  private void unsubscribeFavorites() {
    if (subscriptionFavorites != null) {
      subscriptionFavorites.unsubscribe();
    }
    subscriptionFavorites = new CompositeSubscription();
  }

  private void processFavorites(RFolderEntity favorites_folder) {
    processDocuments( favoritesData, null, null, favorites_folder.getUid(), DocumentType.FAVORITE );
  }

  public void updateProcessed(boolean processLoadedData) {

    processProcessedData = processLoadedData;

    Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
    DocumentsService docService = retrofit.create(DocumentsService.class);

    RFolderEntity processed_folder = dataStore
      .select(RFolderEntity.class)
      .where(RFolderEntity.TYPE.eq("processed"))
      .and(RFolderEntity.USER.eq( settings.getLogin() ))
      .get().firstOrNull();

    if ( processed_folder != null ) {

      if ( processedDataLoaded ) {
        Timber.tag("LoadSequence").d("List of processed already loaded, quit loading");
        if ( processProcessedData ) {
          Timber.tag("LoadSequence").d("Processing previously loaded list of processed");
          processProcessed( processed_folder );
        }
        return;
      }

      if ( processedDataLoading ) {
        Timber.tag("LoadSequence").d("List of processed loading already started, quit loading");
        return;
      }

      dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("RU"));
      Calendar cal = Calendar.getInstance();

      int period = 1;

      try {
        period = Integer.parseInt( settings.getImageLoadPeriod() );
      } catch (NumberFormatException e) {
        Timber.e(e);
      }

      cal.add(Calendar.HOUR, -24*7*period);
      String date = dateFormat.format(cal.getTime());
      Timber.tag(TAG).e("PROCESSED EXIST! %s", date);

      Timber.tag("LoadSequence").d("Loading list of processed");
      processedDataLoading = true;

      unsubscribeProcessed();

      String login = settings.getLogin();

      subscriptionProcessed.add(
        docService.getByFolders(settings.getLogin(), settings.getToken(), null, 500, 0, processed_folder.getUid(), date)
          .subscribeOn( Schedulers.io() )
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(
            data -> {
              Timber.tag("LoadSequence").d("Received list of processed");
              Timber.tag("PROCESSED").e("DOCUMENTS COUNT: %s", data.getDocuments().size() );
              processedData = data;
              processedDataLoaded = true;
              updateDocCount( processedData, false );
              if ( processProcessedData ) {
                if ( Objects.equals( login, settings.getLogin() ) ) {
                  // Обрабатываем полученный список только если логин не поменялся (при входе/выходе в режим замещения)
                  Timber.tag("LoadSequence").d("Processing list of processed");
                  processProcessed( processed_folder );
                }
              } else {
                Timber.tag("LoadSequence").d("processLoadedData = false, quit processing list of processed");
              }
            }, error -> {
              Timber.tag(TAG).e(error);
              processedDataLoading = false;
            }
          )
      );
    }
  }

  private void unsubscribeProcessed() {
    if (subscriptionProcessed != null) {
      subscriptionProcessed.unsubscribe();
    }
    subscriptionProcessed = new CompositeSubscription();
  }

  private void processProcessed(RFolderEntity processed_folder) {
    processDocuments( processedData, null, null, processed_folder.getUid(), DocumentType.PROCESSED );
  }

  public void unsubcribeAll() {
    Timber.tag(TAG).d("Unsubscribe all");

    unsubscribe();
    unsubscribeFavorites();
    unsubscribeProcessed();
    unsubscribeInitV2();
    unsubscribeUpdateAuth();
  }

//  @Subscribe(threadMode = ThreadMode.BACKGROUND)
//  public void onMessageEvent(StepperDcCheckEvent event) throws Exception {
//    String token = event.pin;
//  }
}
