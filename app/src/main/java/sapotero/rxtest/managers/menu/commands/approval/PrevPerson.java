package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.utils.RReturnedRejectedAgain;
import sapotero.rxtest.db.requery.models.utils.RReturnedRejectedAgainEntity;
import sapotero.rxtest.db.requery.models.utils.enums.DocumentCondition;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class PrevPerson extends ApprovalSigningCommand {

  private String TAG = this.getClass().getSimpleName();

  private boolean returnedOldValue;
  private boolean againOldValue;

  public PrevPerson(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ));

    InMemoryDocument inMemoryDocument = store.getDocuments().get( getParams().getDocument() );
    returnedOldValue = inMemoryDocument.getDocument().isReturned();
    againOldValue = inMemoryDocument.getDocument().isAgain();

    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .setLabel(LabelType.SYNC)
        .setLabel(LabelType.REJECTED)
        .removeLabel(LabelType.RETURNED)
        .removeLabel(LabelType.AGAIN)
        .setField(FieldType.PROCESSED, true)
        .setState(InMemoryState.LOADING)
    );

    setAsProcessed();
  }

  @Override
  public String getType() {
    return "prev_person";
  }

  @Override
  public void executeLocal() {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CHANGED, true)
      .set( RDocumentEntity.REJECTED, true )
      .set( RDocumentEntity.RETURNED, false )
      .set( RDocumentEntity.AGAIN, false )
      .set( RDocumentEntity.PROCESSED, true)
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();

    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    printCommandType( this, TAG );

    Observable<OperationResult> info = getOperationResultObservable();

    if (info != null) {
      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            printLog( data, TAG );

            if (data.getMessage() != null && !data.getMessage().toLowerCase().contains("успешно") ) {
              sendErrorCallback( data.getMessage() );
              setError( data.getMessage() );
            } else {
              setSuccess();
            }
          },

          error -> {
            handleError( error.getLocalizedMessage() );
          }
        );

    } else {
      handleError(SIGN_ERROR_MESSAGE);
    }

  }

  private void handleError(String errorMessage) {
    Timber.tag(TAG).i("error: %s", errorMessage);

    sendErrorCallback( errorMessage );

    if ( settings.isOnline() ) {
      setError( errorMessage );
    }
  }

  private void sendErrorCallback(String errorMessage) {
    if ( callback != null ) {
      callback.onCommandExecuteError( errorMessage );
    }
  }

  private void setSuccess() {
    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .removeLabel(LabelType.SYNC)
        .setState(InMemoryState.READY)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CHANGED, false)
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();

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

  private void setError(String errorMessage) {
    Transaction transaction = store.startTransactionFor( getParams().getDocument() )
      .removeLabel(LabelType.SYNC)
      .removeLabel(LabelType.REJECTED)
      .setField(FieldType.PROCESSED, false)
      .setState(InMemoryState.READY);

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

    queueManager.setExecutedWithError( this, Collections.singletonList( errorMessage ) );
  }

  @Override
  public void onRemoteError() {
  }
}
