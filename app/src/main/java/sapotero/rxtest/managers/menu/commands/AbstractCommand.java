package sapotero.rxtest.managers.menu.commands;

import com.birbit.android.jobqueue.JobManager;
import com.google.gson.Gson;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
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
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RSignImageEntity;
import sapotero.rxtest.db.requery.models.utils.RReturnedRejectedAgainEntity;
import sapotero.rxtest.db.requery.models.utils.enums.DocumentCondition;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.interfaces.Operation;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.OperationResult;
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
  @Inject public SingleEntityStore<Persistable> dataStore;
  @Inject public QueueManager queueManager;
  @Inject public MemoryStore store;
  @Inject public JobManager jobManager;

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

  public String getSign(File file) {
    String sign = null;

    // resolved https://tasks.n-core.ru/browse/MVDESD-14117
    // При входе по логин/паролю давать возможность подписывать документы
    if ( settings.isSignedWithDc() ) {
      try {
        sign = MainService.getFakeSign( getParams().getPin(), file );
      } catch (Exception e) {
        e.printStackTrace();
        ACRA.getErrorReporter().handleSilentException(e);
      }

      if (Objects.equals(sign, "")) {
        sign = null;
      }
    } else {
      sign = "";
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

  protected void setSyncLabelInMemory() {
    Timber.tag("RecyclerViewRefresh").d("Command: Set sync label");

    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .setLabel(LabelType.SYNC)
        .setField(FieldType.UPDATED_AT, DateUtil.getTimestamp())
        .setState(InMemoryState.LOADING)
    );
  }

  protected void setChangedInDb() {
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .set( RDocumentEntity.UPDATED_AT, DateUtil.getTimestamp() )
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();
  }

  private void removeSyncChanged(boolean setUpdatedAt) {
    Timber.tag("RecyclerViewRefresh").d("Command: Remove sync label");

    if ( setUpdatedAt ) {
      store.process(
        store.startTransactionFor( getParams().getDocument() )
          .setField(FieldType.UPDATED_AT, DateUtil.getTimestamp())
      );
    }

    removeChangedInDb( setUpdatedAt );

    addUpdateDocumentTask();
  }

  private void removeChangedInDb(boolean setUpdatedAt) {
    if ( setUpdatedAt ) {
      dataStore
        .update(RDocumentEntity.class)
        .set( RDocumentEntity.UPDATED_AT, DateUtil.getTimestamp() )
        .where(RDocumentEntity.UID.eq(getParams().getDocument()))
        .get()
        .value();
    }
  }

  protected void finishOperationOnSuccess() {
    removeSyncChanged(true);
    queueManager.setExecutedRemote(this);
  }

  protected void finishOperationOnError(List<String> errors) {
    removeSyncChanged(false);
    queueManager.setExecutedWithError( this, errors );
  }

  public <T> boolean notEmpty(Collection<T> collection) {
    return collection != null && collection.size() > 0;
  }

  protected RSignImageEntity getSignImage(String imageId) {
    return dataStore
      .select(RSignImageEntity.class)
      .where(RSignImageEntity.IMAGE_ID.eq(imageId))
      .get().firstOrNull();
  }

  void printOperationResult(OperationResult data) {
    Timber.tag(TAG).i("ok: %s", data.getOk());
    Timber.tag(TAG).i("error: %s", data.getMessage());
    Timber.tag(TAG).i("type: %s", data.getType());
  }

  protected void printCommandType() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );
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

  private void startRejectedOperation() {
    startRejectedOperationInMemory();
    startRejectedOperationInDb();
    setAsProcessed();
  }

  protected void finishRejectedOperationOnSuccess() {
    removeSyncChanged(true);
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
      returnedRejectedAgainEntity.setStatus( getParams().getStatusCode() );
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
      returnedRejectedAgainEntity.setStatus( getParams().getStatusCode() );
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
      .removeLabel(LabelType.REJECTED)
      .setField(FieldType.PROCESSED, false);

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
      .set( RDocumentEntity.REJECTED, false)
      .set( RDocumentEntity.RETURNED, returnedOldValue)
      .set( RDocumentEntity.AGAIN, againOldValue)
      .set( RDocumentEntity.PROCESSED, false)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ) )
      .get()
      .value();

    queueManager.setExecutedWithError( this, errors );

    addUpdateDocumentTask();
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

  private void startProcessedOperation() {
    startProcessedOperationInMemory();
    startProcessedOperationInDb();
    setAsProcessed();
  }

  protected void finishProcessedOperationOnSuccess() {
    removeSyncChanged(true);
    setDocumentCondition( DocumentCondition.PROCESSED );
    queueManager.setExecutedRemote(this);
  }

  protected void onOperationError(Throwable error) {
    String errorMessage = error.getLocalizedMessage();

    Timber.tag(TAG).i("error: %s", errorMessage);

    if ( isOnline( error ) ) {
      finishOnOperationError( Collections.singletonList( errorMessage ) );
    }
  }

  // resolved https://tasks.n-core.ru/browse/MPSED-2273
  // 1) Подписание с потерей сети.
  // If error is instance of IOException (network error occurred) or server returned "Unauthorized",
  // do not finish operation on error even if settings.isOnline() is true.
  protected boolean isOnline(Throwable error) {
    Timber.tag(TAG).d("settings.isOnline() ? %s", settings.isOnline());
    Timber.tag(TAG).d("error instanceof IOException ? %s", error instanceof IOException);

    boolean isUnauthorized = ( error instanceof HttpException ) && ((HttpException) error).code() == HttpURLConnection.HTTP_UNAUTHORIZED;

    Timber.tag(TAG).d("isUnauthorized ? %s", isUnauthorized);

    return settings.isOnline() && !(error instanceof IOException) && !isUnauthorized;
  }

  public abstract void finishOnOperationError(List<String> errors);

  // resolved https://tasks.n-core.ru/browse/MPSED-2286
  // В конце каждой операции ставить задачу на обновление документа.
  // Метод вызывается во всех операциях, кроме:
  // AddTemporaryDecision (так как не меняет документ на сервере),
  // SignFile (так как после нее всегда отрабатывает Signing NextPerson),
  // AddToFolder, RemoveFromFolder (так как не меняют документа),
  // DoNothing (так как пустая операция),
  // CreateTemplate, RemoveTemplate, UpdateTemplate (так как не меняют документов)
  protected void addUpdateDocumentTask() {
    Timber.tag(TAG).e("addUpdateDocumentTask");

    CommandFactory.Operation operation = CommandFactory.Operation.UPDATE_DOCUMENT;
    CommandParams params = new CommandParams();
    params.setDocument( getParams().getDocument() );
    params.setUpdatedAt( DateUtil.getTimestamp() );
    Command command = operation.getCommand(null, params);
    queueManager.add(command);
  }

  public void addToQueue() {
    queueManager.add(this);
    queueManager.setAsRunning(this);
  }

  public void local(boolean rejected) {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    addToQueue();
    EventBus.getDefault().post( new ShowNextDocumentEvent( getParams().getDocument() ));

    if ( rejected ) {
      startRejectedOperation();
    } else {
      startProcessedOperation();
    }

    queueManager.setExecutedLocal(this);
  }
}
