package sapotero.rxtest.managers.menu.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
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
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.interfaces.Operation;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import timber.log.Timber;


public abstract class AbstractCommand implements Serializable, Command, Operation {

  @Inject public OkHttpClient okHttpClient;
  @Inject public Settings settings;
  @Inject public Mappers mappers;
  @Inject public SingleEntityStore<Persistable> dataStore;
  @Inject public QueueManager queueManager;
  @Inject public MemoryStore store;

  public CommandParams params;

  public AbstractCommand() {
    EsdApplication.getManagerComponent().inject(this);
  }

  public void withParams(CommandParams params) {
    this.params = params;
  }

  public CommandParams getParams() {
    return params;
  }

  public Callback callback;
  public interface Callback {
    void onCommandExecuteSuccess(String command);
    void onCommandExecuteError(String type);
  }

  public Retrofit getOperationsRetrofit() {
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

  // resolved https://tasks.n-core.ru/browse/MVDESD-13258
  // 1. Созданные мной и подписант я
  protected void checkCreatorAndSignerIsCurrentUser(DecisionError data, String TAG) {
    String decisionUid = data.getDecisionUid();

    // Если создал резолюцию я и подписант я, то сохранить UID этой резолюции в отдельную таблицу
    if ( decisionUid != null && !decisionUid.equals("") ) {
      if ( Objects.equals( data.getDecisionSignerId(), settings.getCurrentUserId() ) ) {
        RDisplayFirstDecisionEntity rDisplayFirstDecisionEntity = new RDisplayFirstDecisionEntity();
        rDisplayFirstDecisionEntity.setDecisionUid( decisionUid );
        rDisplayFirstDecisionEntity.setUserId( settings.getCurrentUserId() );

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

  protected void setDocOperationStartedInMemory(String uid) {
    store.process(
      store.startTransactionFor( uid )
        .setLabel(LabelType.SYNC)
        .setState(InMemoryState.LOADING)
    );
  }

  protected void setDocOperationProcessedStartedInMemory(String uid) {
    store.process(
      store.startTransactionFor( uid )
        .setLabel(LabelType.SYNC)
        .setField(FieldType.PROCESSED, true)
        .setState(InMemoryState.LOADING)
    );
  }

  protected void finishOperationOnSuccess(String uid) {
    store.process(
      store.startTransactionFor( uid )
        .removeLabel(LabelType.SYNC)
        .setField(FieldType.MD5, "")
        .setState(InMemoryState.READY)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CHANGED, false)
      .where(RDocumentEntity.UID.eq(uid))
      .get()
      .value();
  }

  protected void finishOperationOnError(Command command, String uid, List<String> errors) {
    finishOperationOnSuccess( uid );
    queueManager.setExecutedWithError( command, errors );
  }

  protected void finishOperationProcessedOnError(Command command, String uid, List<String> errors) {
    store.process(
      store.startTransactionFor( uid )
        .removeLabel(LabelType.SYNC)
        .setField(FieldType.MD5, "")
        .setField(FieldType.PROCESSED, false)
        .setState(InMemoryState.READY)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.PROCESSED, false)
      .set( RDocumentEntity.CHANGED, false)
      .where(RDocumentEntity.UID.eq( uid ) )
      .get()
      .value();

    queueManager.setExecutedWithError( command, errors );
  }
}
