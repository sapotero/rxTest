package sapotero.rxtest.managers;

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
import sapotero.rxtest.db.requery.utils.Journals;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckFailEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckSuccesEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckFailEvent;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckSuccessEvent;
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

  public static final int LIMIT = 500;

  private final String TAG = this.getClass().getSimpleName();

  @Inject OkHttpClient okHttpClient;
  @Inject ISettings settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject MemoryStore store;

  private CompositeSubscription subscription;
  private CompositeSubscription subscriptionFavorites;
  private CompositeSubscription subscriptionProcessed;
  private CompositeSubscription subscriptionInitV2;
  private CompositeSubscription subscriptionUpdateAuth;

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

  public DataLoaderManager() {
    EsdApplication.getManagerComponent().inject(this);
  }

  public void initV2(boolean loadAllDocs) {
    AuthService auth = getAuthService();

    unsubscribeInitV2();

    String login = settings.getLogin();

    subscriptionInitV2.add(
      // получаем данные о пользователе
      auth.getUserInfoV2(login, settings.getToken())
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          v2 -> {
            Timber.tag("LoadSequence").d("Received user info");

            // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
            if ( Objects.equals( login, settings.getLogin() ) ) {
              v2UserOshs user = v2.get(0);
              setCurrentUser(user.getName());
              setCurrentUserId(user.getId());
              setCurrentUserOrganization(user.getOrganization());
              setCurrentUserPosition(user.getPosition());
              setCurrentUserImage(user.getImage());

              String currentUserId = user.getId();

              // получаем папки
              subscriptionInitV2.add(
                auth.getFolders(login, settings.getToken())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    data -> {
                      Timber.tag("LoadSequence").d("Received list of folders");
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreateFoldersJob(data, login));
                        loadAllDocs( loadAllDocs, login, currentUserId );
                      }
                    },
                    error -> {
                      Timber.tag(TAG).e(error);
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        loadAllDocs( loadAllDocs, login, currentUserId );
                      }
                    }
                  )
              );

              subscriptionInitV2.add(
                auth.getPrimaryConsiderationUsers(login, settings.getToken())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    data -> {
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreatePrimaryConsiderationJob(data, login));
                      }
                    },
                    error -> Timber.tag(TAG).e(error)
                  )
              );

              // загрузка срочности
              subscriptionInitV2.add(
                auth.getUrgency(login, settings.getToken(), "urgency")
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    urgencies -> {
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreateUrgencyJob(urgencies, login));
                      }
                    },
                    error -> Timber.tag(TAG).e(error)
                  )
              );

              // загрузка шаблонов резолюции
              subscriptionInitV2.add(
                auth.getTemplates(login, settings.getToken(), null)
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    templates -> {
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreateTemplatesJob(templates, null, login));
                      }
                    },
                    error -> Timber.tag(TAG).e(error)
                  )
              );

              // загрузка шаблонов отклонения
              subscriptionInitV2.add(
                auth.getTemplates(login, settings.getToken(), "rejection")
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    templates -> {
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreateTemplatesJob(templates, "rejection", login));
                      }
                    },
                    error -> Timber.tag(TAG).e(error)
                  )
              );

              // получаем группу Избранное(МП)
              subscriptionInitV2.add(
                auth.getFavoriteUsers(login, settings.getToken())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    data -> {
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreateFavoriteUsersJob(data, login));
                      }
                    },
                    error -> Timber.tag(TAG).e(error)
                  )
              );

              // Доработка api для возврата ВРИО/по поручению
              // resolved https://tasks.n-core.ru/browse/MVDESD-11453
              subscriptionInitV2.add(
                auth.getAssistantByHeadId(login, settings.getToken(), currentUserId)
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    data -> {
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreateAssistantJob(data, login, true));
                      }
                    },
                    error -> Timber.tag(TAG).e(error)
                  )
              );

              // resolved https://tasks.n-core.ru/browse/MVDESD-13711
              subscriptionInitV2.add(
                auth.getAssistantByAssistantId(login, settings.getToken(), currentUserId)
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    data -> {
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreateAssistantJob(data, login, false));
                      }
                    },
                    error -> Timber.tag(TAG).e(error)
                  )
              );

              // resolved https://tasks.n-core.ru/browse/MVDESD-13752
              // Добавить в боковую панель список коллег
              subscriptionInitV2.add(
                auth.getColleagues(login, settings.getToken())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(
                    data -> {
                      // Обрабатываем ответ только если логин не поменялся (при входе/выходе в режим замещения)
                      if ( Objects.equals( login, settings.getLogin() ) ) {
                        jobManager.addJobInBackground(new CreateColleagueJob(data, login));
                      }
                    },
                    error -> {
                      Timber.tag(TAG).e(error);
                      EventBus.getDefault().post( new UpdateDrawerEvent() );
                    }
                  )
              );
            }

          },
          error -> {
            Timber.tag("USER_INFO").e( "ERROR: %s", error);
            if ( Objects.equals( login, settings.getLogin() ) ) {
              loadAllDocs( loadAllDocs, login, settings.getCurrentUserId() );
            }
          })
    );
  }

  private Retrofit getRetrofit() {
    return new RetrofitManager(settings.getHost(), okHttpClient).process();
  }

  private AuthService getAuthService() {
    Retrofit retrofit = getRetrofit();
    return retrofit.create(AuthService.class);
  }

  private void loadAllDocs(boolean load, String login, String currentUserId) {
    if ( load ) {
      updateByCurrentStatus(MainMenuItem.ALL, null, login, currentUserId);
    }
  }

  private void setToken( String token ){
    settings.setToken(token);
  }

  private void setLogin( String login ){
    settings.setLogin(login);
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

  private void unsubscribe(){
    if (subscription != null) {
      subscription.unsubscribe();
    }

    subscription = new CompositeSubscription();
  }

  private void unsubscribeInitV2() {
    if (subscriptionInitV2 != null) {
      subscriptionInitV2.unsubscribe();
    }

    subscriptionInitV2 = new CompositeSubscription();
  }

  private void unsubscribeUpdateAuth() {
    if (subscriptionUpdateAuth != null) {
      subscriptionUpdateAuth.unsubscribe();
    }

    subscriptionUpdateAuth = new CompositeSubscription();
  }

  public void updateAuth( boolean sendEvent ){
    if ( settings.isUpdateAuthStarted() ) {
      return;
    }

    settings.setUpdateAuthStarted( true );

    if ( settings.isSubstituteMode() && !settings.isSignedWithDc() ) {
      // В режиме замещения, если вошли по логину, то меняем логин на логин основного пользователя,
      // чтобы правильно сформировался запрос на получение токена
      swapLogin();
    }

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
    AuthService auth = getAuthService();

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

    AuthService auth = getAuthService();

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

            EventBus.getDefault().post( new StepperDcCheckSuccesEvent() );
          },
          error -> {
            Timber.tag(TAG).i("tryToSignWithDc error: %s" , error );
            EventBus.getDefault().post( new StepperDcCheckFailEvent( error.getMessage() ) );
          }
        )
    );
  }

  public void tryToSignWithLogin(String login, String password) {
    AuthService auth = getAuthService();

    unsubscribe();

    subscription.add(
      auth
        .getAuth(login, password)
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          data -> {
            Timber.tag(TAG).i("tryToSignWithLogin: token %s", data.getAuthToken());

            setLogin(login);
            setPassword(password);
            setToken(data.getAuthToken());

            EventBus.getDefault().post( new StepperLoginCheckSuccessEvent() );
          },
          error -> {
            Timber.tag(TAG).i("tryToSignWithLogin error: %s", error);
            EventBus.getDefault().post( new StepperLoginCheckFailEvent( error.getMessage() ) );
          }
        )
    );
  }

  public void updateByCurrentStatus(MainMenuItem items, MainMenuButton button, String login, String currentUserId) {
    Timber.tag(TAG).e("updateByCurrentStatus: %s %s", items, button );

    favoritesDataLoaded = false;
    processedDataLoaded = false;
    favoritesDataLoading = false;
    processedDataLoading = false;

    if ( items == MainMenuItem.PROCESSED ) {
      updateProcessed( true );

    } else if ( items == MainMenuItem.FAVORITES ) {
      updateFavorites( true );

    } else {
      ArrayList<String> sp = new ArrayList<>();
      ArrayList<String> statuses = new ArrayList<>();
      ArrayList<String> indexes = new ArrayList<>();

      // Обновляем все журналы и статусы только для На контроле или если button null
      if (items == MainMenuItem.ALL || items.getIndex() == Journals.SHARED) {
        addAllIndexes(indexes);

        if (button == null) {
          addAllStatuses(sp, statuses);

        } else {
          switch (button) {
            case PROJECTS:
              addApprovalSigning(sp);
              break;
            case PRIMARY_CONSIDERATION:
              statuses.add( JournalStatus.PRIMARY.getNameForApi() );
              break;
            case PERFORMANCE:
              statuses.add( JournalStatus.FOR_REPORT.getNameForApi() );
              break;
          }
        }

        checkImagesToDelete();

      } else if (items == MainMenuItem.ON_CONTROL) {
        addAllIndexes(indexes);
        addAllStatuses(sp, statuses);
        checkImagesToDelete();

      } else {
        switch (items) {
          case CITIZEN_REQUESTS:
            indexes.add( JournalStatus.CITIZEN_REQUESTS.getNameForApi() );
            break;
          case INCOMING_DOCUMENTS:
            indexes.add( JournalStatus.INCOMING_DOCUMENTS.getNameForApi() );
            break;
          case ORDERS_DDO:
            indexes.add( JournalStatus.ORDERS_DDO.getNameForApi() );
            break;
          case ORDERS:
            indexes.add( JournalStatus.ORDERS.getNameForApi() );
            break;
          case IN_DOCUMENTS:
            indexes.add( JournalStatus.OUTGOING_DOCUMENTS.getNameForApi() );
            break;
          case INCOMING_ORDERS:
            indexes.add( JournalStatus.INCOMING_ORDERS.getNameForApi() );
            break;
        }

        if (button == null) {
          if (items == MainMenuItem.APPROVE_ASSIGN) {
            addApprovalSigning( sp );
          } else {
            addPrimaryReport( statuses );
          }

        } else {
          switch (button) {
            case APPROVAL:
              sp.add( JournalStatus.APPROVAL.getNameForApi() );
              break;
            case ASSIGN:
              sp.add( JournalStatus.SIGNING.getNameForApi() );
              break;
            case PRIMARY_CONSIDERATION:
              statuses.add( JournalStatus.PRIMARY.getNameForApi() );
              break;
            case PERFORMANCE:
              statuses.add( JournalStatus.FOR_REPORT.getNameForApi() );
              break;
          }
        }
      }

      Timber.tag(TAG).e("data: %s %s", indexes, statuses);

      DocumentsService docService = getDocumentService();

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

      unsubscribe();

      for (String index: indexes ) {
        for (String status: statuses ) {
          subscription.add(
            docService
              .getDocumentsByIndexes(login, settings.getToken(), index, status, null , LIMIT, getYears())
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
                    processDocuments( data, status, index, null, DocumentType.DOCUMENT, login, currentUserId );
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
        subscription.add(
          docService
            .getDocuments(login, settings.getToken(), code, null , LIMIT, 0, getYears())
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
                  processDocuments( data, code, null, null, DocumentType.DOCUMENT, login, currentUserId );
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

  private void addAllIndexes(ArrayList<String> indexes) {
    indexes.add( JournalStatus.CITIZEN_REQUESTS.getNameForApi() );
    indexes.add( JournalStatus.INCOMING_DOCUMENTS.getNameForApi() );
    indexes.add( JournalStatus.ORDERS_DDO.getNameForApi() );
    indexes.add( JournalStatus.ORDERS.getNameForApi() );
    indexes.add( JournalStatus.OUTGOING_DOCUMENTS.getNameForApi() );
    indexes.add( JournalStatus.INCOMING_ORDERS.getNameForApi() );
  }

  private void addApprovalSigning(ArrayList<String> sp) {
    sp.add( JournalStatus.APPROVAL.getNameForApi() );
    sp.add( JournalStatus.SIGNING.getNameForApi() );
  }

  private void addPrimaryReport(ArrayList<String> statuses) {
    statuses.add( JournalStatus.PRIMARY.getNameForApi() );
    statuses.add( JournalStatus.FOR_REPORT.getNameForApi() );
  }

  private void addAllStatuses(ArrayList<String> sp, ArrayList<String> statuses) {
    addApprovalSigning( sp );
    addPrimaryReport( statuses );
  }

  private DocumentsService getDocumentService() {
    Retrofit retrofit = getRetrofit();
    return retrofit.create(DocumentsService.class);
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

  private void processDocuments(Documents data, String status, String index, String folder, DocumentType documentType, String login, String currentUserId) {
    if (data.getDocuments().size() >= 0){
      HashMap<String, Document> doc_hash = new HashMap<>();

      for (Document doc: data.getDocuments() ) {
        doc_hash.put( doc.getUid(), doc );
      }

      if (documentType == DocumentType.DOCUMENT) {
        store.process( doc_hash, status, index, login, currentUserId );
      } else {
        store.process( doc_hash, folder, documentType, login, currentUserId );
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

    if ( total > LIMIT ) {
      total = LIMIT;
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

  private Observable<AuthSignToken> getAuthSubscription() {
    AuthService auth = getAuthService();

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

  public void updateFavorites(boolean processLoadedData) {

    processFavoritesData = processLoadedData;

    DocumentsService docService = getDocumentService();

    RFolderEntity favorites_folder = dataStore
      .select(RFolderEntity.class)
      .where(RFolderEntity.TYPE.eq("favorites"))
      .and(RFolderEntity.USER.eq( settings.getLogin() ))
      .get().firstOrNull();

    if ( favorites_folder != null ) {
      Timber.tag(TAG).e("FAVORITES EXIST!");

      String login = settings.getLogin();
      String currentUserId = settings.getCurrentUserId();

      if ( favoritesDataLoaded ) {
        Timber.tag("LoadSequence").d("List of favorites already loaded, quit loading");
        if ( processFavoritesData ) {
          Timber.tag("LoadSequence").d("Processing previously loaded list of favorites");
          processFavorites(favorites_folder, login, currentUserId);
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

      subscriptionFavorites.add(
        docService.getByFolders(login, settings.getToken(), null, LIMIT, 0, favorites_folder.getUid(), null)
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
                  processFavorites(favorites_folder, login, currentUserId);
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

  private void processFavorites(RFolderEntity favorites_folder, String login, String currentUserId) {
    processDocuments( favoritesData, null, null, favorites_folder.getUid(), DocumentType.FAVORITE, login, currentUserId );
  }

  public void updateProcessed(boolean processLoadedData) {

    processProcessedData = processLoadedData;

    DocumentsService docService = getDocumentService();

    RFolderEntity processed_folder = dataStore
      .select(RFolderEntity.class)
      .where(RFolderEntity.TYPE.eq("processed"))
      .and(RFolderEntity.USER.eq( settings.getLogin() ))
      .get().firstOrNull();

    if ( processed_folder != null ) {

      String login = settings.getLogin();
      String currentUserId = settings.getCurrentUserId();

      if ( processedDataLoaded ) {
        Timber.tag("LoadSequence").d("List of processed already loaded, quit loading");
        if ( processProcessedData ) {
          Timber.tag("LoadSequence").d("Processing previously loaded list of processed");
          processProcessed( processed_folder, login, currentUserId );
        }
        return;
      }

      if ( processedDataLoading ) {
        Timber.tag("LoadSequence").d("List of processed loading already started, quit loading");
        return;
      }

      SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("RU"));
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

      subscriptionProcessed.add(
        docService.getByFolders(login, settings.getToken(), null, LIMIT, 0, processed_folder.getUid(), date)
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
                  processProcessed( processed_folder, login, currentUserId );
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

  private void processProcessed(RFolderEntity processed_folder, String login, String currentUserId) {
    processDocuments( processedData, null, null, processed_folder.getUid(), DocumentType.PROCESSED, login, currentUserId );
  }

  public void unsubscribeAll() {
    Timber.tag(TAG).d("Unsubscribe all");

    unsubscribe();
    unsubscribeFavorites();
    unsubscribeProcessed();
    unsubscribeInitV2();
    unsubscribeUpdateAuth();
  }
}
