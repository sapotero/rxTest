package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.utils.RApprovalNextPersonEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public class NextPerson extends ApprovalSigningCommand {

  public NextPerson(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    queueManager.add(this);
    queueManager.setAsRunning(this, true);

    EventBus.getDefault().post( new ShowNextDocumentEvent( getParams().getDocument() ));

    startProcessedOperationInMemory();

    setTaskStarted( getParams().getDocument(), false );
    setAsProcessed();
  }

  @Override
  public String getType() {
    return "next_person";
  }

  @Override
  public void executeLocal() {
    startProcessedOperationInDb();
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    RApprovalNextPersonEntity rApprovalNextPersonEntity = getApprovalNextPersonEntity( getParams().getDocument() );

    if ( rApprovalNextPersonEntity != null && rApprovalNextPersonEntity.isTaskStarted() ) {
      Timber.tag(TAG).i( "Task already started, quit" );
      return;
    }

    if ( rApprovalNextPersonEntity == null ) {
      createNewRApprovalNextPersonEntity( getParams().getDocument() );
    } else {
      setTaskStarted( getParams().getDocument(), true );
    }

    printCommandType();

    Observable<OperationResult> info = getOperationResultObservable();

    if (info != null) {
      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          this::onOperationSuccess,
          error -> {
            onOperationError( error );
            setTaskStarted( getParams().getDocument(), false );
          }
        );

    } else {
      sendErrorCallback( SIGN_ERROR_MESSAGE );
      finishOnOperationError( Collections.singletonList( SIGN_ERROR_MESSAGE ) );
    }
  }

  private RApprovalNextPersonEntity createNewRApprovalNextPersonEntity(String uid) {
    RApprovalNextPersonEntity rApprovalNextPersonEntity = new RApprovalNextPersonEntity();
    rApprovalNextPersonEntity.setDocumentUid( uid );
    rApprovalNextPersonEntity.setTaskStarted( true );

    dataStore
      .insert(rApprovalNextPersonEntity)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .subscribeOn(Schedulers.computation())
      .subscribe(
        data -> Timber.tag(TAG).v( "inserted RApprovalNextPersonEntity %s", data.getDocumentUid() ),
        Timber::e
      );

    return rApprovalNextPersonEntity;
  }

  private RApprovalNextPersonEntity getApprovalNextPersonEntity(String documentUid) {
    return dataStore
      .select( RApprovalNextPersonEntity.class )
      .where( RApprovalNextPersonEntity.DOCUMENT_UID.eq( documentUid ) )
      .get().firstOrNull();
  }

  private void setTaskStarted(String documentUid, boolean value) {
    int count = dataStore
      .update( RApprovalNextPersonEntity.class )
      .set( RApprovalNextPersonEntity.TASK_STARTED, value )
      .where( RApprovalNextPersonEntity.DOCUMENT_UID.eq( documentUid ) )
      .get()
      .value();

    Timber.tag(TAG).i("Set task started = %s, count = %s", value, count);
  }

  @Override
  public void finishOnOperationSuccess() {
    finishProcessedOperationOnSuccess();
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
    setTaskStarted( getParams().getDocument(), false );
  }
}
