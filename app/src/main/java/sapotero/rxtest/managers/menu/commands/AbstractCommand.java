package sapotero.rxtest.managers.menu.commands;

import com.google.gson.Gson;

import org.acra.ACRA;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDisplayFirstDecisionEntity;
import sapotero.rxtest.db.requery.models.images.RSignImageEntity;
import sapotero.rxtest.db.requery.models.utils.RReturnedRejectedAgainEntity;
import sapotero.rxtest.db.requery.models.utils.enums.DocumentCondition;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.interfaces.Operation;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.retrofit.models.wrapper.SignWrapper;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Transaction;
import sapotero.rxtest.utils.queue.QueueManager;
import timber.log.Timber;


public abstract class AbstractCommand implements Serializable, Command, Operation {

  @Inject public OkHttpClient okHttpClient;
  @Inject public ISettings settings;
  @Inject public Mappers mappers;
  @Inject public SingleEntityStore<Persistable> dataStore;
  @Inject public QueueManager queueManager;
  @Inject public MemoryStore store;

  protected static final String SIGN_ERROR_MESSAGE = "Произошла ошибка электронной подписи";

  public String TAG = this.getClass().getSimpleName();

  public CommandParams params;

  public AbstractCommand(CommandParams params) {
    EsdApplication.getManagerComponent().inject(this);
    this.params = params;
  }

  public CommandParams getParams() {
    return params;
  }

  public String getTAG() {
    return TAG;
  }

  public Callback callback;
  public interface Callback {
    void onCommandExecuteSuccess(String command);
    void onCommandExecuteError(String type);
  }

  protected Retrofit getOperationsRetrofit() {
    return new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( settings.getHost() + "v3/operations/" )
      .client( okHttpClient )
      .build();
  }

  public Retrofit getRetrofit() {
    return new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( settings.getHost() )
      .client( okHttpClient )
      .build();
  }

  public String getSign() {
    String sign = null;

    try {
      sign = MainService.getFakeSign( getParams().getPin(), null );
    } catch (Exception e) {
      e.printStackTrace();
      ACRA.getErrorReporter().handleSilentException(e);
    }

    if (Objects.equals(sign, "")) {
      sign = null;
    }

    return sign;
  }

  protected RequestBody getSignBody(String sign) {
    SignWrapper signWrapper = new SignWrapper();
    signWrapper.setSign(sign);

    String signJson = new Gson().toJson( signWrapper );

    return RequestBody.create(
      MediaType.parse("application/json"),
      signJson
    );
  }

  private RDocumentEntity findDocumentByUID(){
    return dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get().firstOrNull();
  }

  public void setAsProcessed(){
    RDocumentEntity doc = findDocumentByUID();
    if ( doc != null && !doc.isViewed() ){
      Retrofit retrofit =  new Retrofit.Builder()
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .baseUrl( settings.getHost() + "v3/operations/" )
        .client( okHttpClient )
        .build();

      DocumentsService operationService = retrofit.create( DocumentsService.class );

//     добавить проверку на то, что документ уже был обработан/просмотрен
      Observable<ResponseBody> view = operationService.processDocument(
        getParams().getDocument(),
        getParams().getLogin(),
        settings.getToken()
      );

      view
        .subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe( body -> {
          try {
            Timber.d( "setAsProcessed : %s | %s", getParams().getDocument(), body.string() );
          } catch (IOException e) {
            e.printStackTrace();
          }
        }, Timber::e);
    }
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13258
  // 1. Созданные мной и подписант я
  protected void checkCreatorAndSignerIsCurrentUser(DecisionError data) {
    String decisionUid = data.getDecisionUid();

    // Если создал резолюцию я и подписант я, то сохранить UID этой резолюции в отдельную таблицу
    if ( decisionUid != null && !decisionUid.equals("") ) {
      if ( Objects.equals( data.getDecisionSignerId(), getParams().getCurrentUserId() ) ) {
        RDisplayFirstDecisionEntity rDisplayFirstDecisionEntity = new RDisplayFirstDecisionEntity();
        rDisplayFirstDecisionEntity.setDecisionUid( decisionUid );
        rDisplayFirstDecisionEntity.setUserId( getParams().getCurrentUserId() );

        dataStore
          .insert( rDisplayFirstDecisionEntity )
          .toObservable()
          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            result -> Timber.tag(TAG).v("Added decision to display first decision table"),
            error -> Timber.tag(TAG).e(error)
          );
      }
    }
  }

  protected void setSyncLabelInMemory() {
    Timber.tag("RecyclerViewRefresh").d("Command: Set sync label");

    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .setLabel(LabelType.SYNC)
        .setState(InMemoryState.LOADING)
    );
  }

  protected void setChangedInDb() {
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();
  }

  protected void setSyncAndProcessedInMemory() {
    Timber.tag("RecyclerViewRefresh").d("Command: Set sync label");

    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .setLabel(LabelType.SYNC)
        .setField(FieldType.PROCESSED, true)
        .setState(InMemoryState.LOADING)
    );
  }

  protected void removeSyncChanged() {
    Timber.tag("RecyclerViewRefresh").d("Command: Remove sync label");

    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .removeLabel(LabelType.SYNC)
        .setState(InMemoryState.READY)
    );

    removeChangedInDb();
  }

  protected void removeChangedInDb() {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CHANGED, false)
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();
  }

  protected void finishOperationWithoutProcessedOnSuccess() {
    removeSyncChanged();
    queueManager.setExecutedRemote(this);
  }

  protected void finishOperationWithoutProcessedOnError(List<String> errors) {
    removeSyncChanged();
    queueManager.setExecutedWithError( this, errors );
  }

  protected void finishOperationWithProcessedOnError(List<String> errors) {
    Timber.tag("RecyclerViewRefresh").d("Command: Remove sync label");

    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .removeLabel(LabelType.SYNC)
        .setField(FieldType.PROCESSED, false)
        .setState(InMemoryState.READY)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.PROCESSED, false)
      .set( RDocumentEntity.CHANGED, false)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ) )
      .get()
      .value();

    queueManager.setExecutedWithError( this, errors );
  }

  public <T> boolean notEmpty(Collection<T> collection) {
    return collection != null && collection.size() > 0;
  }

  public void onError(String errorMessage, boolean setProcessedFalse) {
    Timber.tag(TAG).i("error: %s", errorMessage);

    sendErrorCallback( errorMessage );

    if ( settings.isOnline() ) {
      if ( setProcessedFalse ) {
        finishOperationWithProcessedOnError( Collections.singletonList( errorMessage ) );
      } else {
        finishOperationWithoutProcessedOnError( Collections.singletonList( errorMessage ) );
      }
    }
  }

  protected RSignImageEntity getSignImage(String imageId) {
    return dataStore
      .select(RSignImageEntity.class)
      .where(RSignImageEntity.IMAGE_ID.eq(imageId))
      .get().firstOrNull();
  }

  protected void printOperationResult(OperationResult data) {
    Timber.tag(TAG).i("ok: %s", data.getOk());
    Timber.tag(TAG).i("error: %s", data.getMessage());
    Timber.tag(TAG).i("type: %s", data.getType());
  }

  protected void printCommandType() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );
  }

  protected void sendSuccessCallback() {
    if ( callback != null ) {
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  protected void sendErrorCallback(String errorMessage) {
    if ( callback != null ) {
      callback.onCommandExecuteError( errorMessage );
    }
  }

  protected void saveOldLabelValues() {
    InMemoryDocument inMemoryDocument = store.getDocuments().get( getParams().getDocument() );
    getParams().setReturnedOldValue( inMemoryDocument.getDocument().isReturned() );
    getParams().setAgainOldValue( inMemoryDocument.getDocument().isAgain() );
  }

  protected void startRejectedOperationInMemory() {
    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .setLabel(LabelType.SYNC)
        .setLabel(LabelType.REJECTED)
        .removeLabel(LabelType.RETURNED)
        .removeLabel(LabelType.AGAIN)
        .setField(FieldType.PROCESSED, true)
        .setState(InMemoryState.LOADING)
    );
  }

  protected void startRejectedOperationInDb() {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CHANGED, true)
      .set( RDocumentEntity.REJECTED, true )
      .set( RDocumentEntity.RETURNED, false )
      .set( RDocumentEntity.AGAIN, false )
      .set( RDocumentEntity.PROCESSED, true)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();
  }

  protected void finishRejectedOperationOnSuccess() {
    removeSyncChanged();
    setDocumentCondition( DocumentCondition.REJECTED );
    queueManager.setExecutedRemote(this);
  }

  private void setDocumentCondition(DocumentCondition documentCondition) {
    RReturnedRejectedAgainEntity returnedRejectedAgainEntity = dataStore
      .select( RReturnedRejectedAgainEntity.class )
      .where( RReturnedRejectedAgainEntity.DOCUMENT_UID.eq( getParams().getDocument() ) )
      .and( RReturnedRejectedAgainEntity.USER.eq( getParams().getLogin() ) )
      .get().firstOrNull();

    if ( returnedRejectedAgainEntity != null ) {
      returnedRejectedAgainEntity.setDocumentCondition( documentCondition );

      dataStore
        .update( returnedRejectedAgainEntity )
        .toObservable()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          result -> Timber.tag(TAG).d("Updated document condition in ReturnedRejectedAgain table"),
          error -> Timber.tag(TAG).e(error)
        );

    } else {
      returnedRejectedAgainEntity = new RReturnedRejectedAgainEntity();
      returnedRejectedAgainEntity.setDocumentUid( getParams().getDocument() );
      returnedRejectedAgainEntity.setUser( getParams().getLogin() );
      returnedRejectedAgainEntity.setDocumentCondition( documentCondition );

      dataStore
        .insert( returnedRejectedAgainEntity )
        .toObservable()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          result -> Timber.tag(TAG).d("Added document condition to ReturnedRejectedAgain table"),
          error -> Timber.tag(TAG).e(error)
        );
    }
  }

  protected void finishRejectedProcessedOperationOnError(List<String> errors) {
    Transaction transaction = store.startTransactionFor( getParams().getDocument() )
      .removeLabel(LabelType.SYNC)
      .removeLabel(LabelType.REJECTED)
      .setField(FieldType.PROCESSED, false)
      .setState(InMemoryState.READY);

    boolean returnedOldValue = getParams().getReturnedOldValue();
    boolean againOldValue = getParams().getAgainOldValue();

    if ( returnedOldValue ) {
      transaction.setLabel(LabelType.RETURNED);
    }

    if ( againOldValue ) {
      transaction.setLabel(LabelType.AGAIN);
    }

    store.process( transaction );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CHANGED, false)
      .set( RDocumentEntity.REJECTED, false)
      .set( RDocumentEntity.RETURNED, returnedOldValue)
      .set( RDocumentEntity.AGAIN, againOldValue)
      .set( RDocumentEntity.PROCESSED, false)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ) )
      .get()
      .value();

    queueManager.setExecutedWithError( this, errors );
  }

  void handleRejectedOperationError(String errorMessage) {
    Timber.tag(TAG).i("error: %s", errorMessage);

    sendErrorCallback( errorMessage );

    if ( settings.isOnline() ) {
      finishRejectedProcessedOperationOnError( Collections.singletonList( errorMessage ) );
    }
  }

  private void checkRejectedOperationResult(OperationResult data) {
    printOperationResult( data );

    if (data.getMessage() != null && !data.getMessage().toLowerCase().contains("успешно") ) {
      sendErrorCallback( data.getMessage() );
      finishRejectedProcessedOperationOnError( Collections.singletonList( data.getMessage() ) );
    } else {
      finishRejectedOperationOnSuccess();
    }
  }

  protected void sendRejectedOperationRequest(Observable<OperationResult> info) {
    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        this::checkRejectedOperationResult,
        error -> handleRejectedOperationError( error.getLocalizedMessage() )
      );
  }

  protected void startProcessedOperationInMemory() {
    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .setLabel(LabelType.SYNC)
        .removeLabel(LabelType.RETURNED)
        .removeLabel(LabelType.AGAIN)
        .setField(FieldType.PROCESSED, true)
        .setState(InMemoryState.LOADING)
    );
  }

  protected void startProcessedOperationInDb() {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CHANGED, true)
      .set( RDocumentEntity.RETURNED, false )
      .set( RDocumentEntity.AGAIN, false )
      .set( RDocumentEntity.PROCESSED, true)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();
  }

  protected void finishProcessedOperationOnSuccess() {
    removeSyncChanged();
    setDocumentCondition( DocumentCondition.PROCESSED );
    queueManager.setExecutedRemote(this);
  }
}
