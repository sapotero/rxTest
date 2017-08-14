package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.utils.RApprovalNextPersonEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Person;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Step;
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
    EventBus.getDefault().post( new ShowNextDocumentEvent( true,  getParams().getDocument() ));

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

//    RDocumentEntity documentEntity = findDocumentByUID();
//
//    boolean saveDocumentCondition = true;
//
//    if ( documentEntity != null && documentEntity.getRoute() != null ) {
//      Route route = mappers.getRouteMapper().toModel( (RRouteEntity) documentEntity.getRoute() );
//      Step step = getStep( route.getSteps(), "Подписывающие" );
//
//      for ( Person person : nullGuard( step.getPeople() ) ) {
//        if ( Objects.equals( person.getOfficialId(), getParams().getCurrentUserId() ) ) {
//          saveDocumentCondition = false;
//        }
//      }
//    }
//
//    // Если подписывающий равен текущему пользователю, то не рисовать дополнительных плашек,
//    // когда документ после согласования вернется на подписание.
//    if ( saveDocumentCondition ) {
//      finishProcessedOperationOnSuccess();
//    } else {
//      finishOperationOnSuccess();
//    }
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
    setTaskStarted( getParams().getDocument(), false );
  }

//  private Step getStep(List<Step> steps, String title) {
//    Step result = new Step();
//
//    for ( Step step : nullGuard( steps) ) {
//      if ( Objects.equals( step.getTitle(), title) ) {
//        result = step;
//        break;
//      }
//    }
//
//    return result;
//  }

}
