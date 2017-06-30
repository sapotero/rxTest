package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RSignImageEntity;
import sapotero.rxtest.db.requery.models.utils.RApprovalNextPerson;
import sapotero.rxtest.db.requery.models.utils.RApprovalNextPersonEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import timber.log.Timber;

public class NextPerson extends ApprovalSigningCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String official_id;
  private String sign;

  public NextPerson(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public NextPerson withPerson(String uid){
    this.official_id = uid;
    return this;
  }
  public NextPerson withSign(String sign){
    this.sign = sign;
    return this;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent());

    setDocOperationProcessedStartedInMemory( getUid() );

    setTaskStarted( getUid(), false );
  }

  private String getUid() {
    return params.getDocument() != null ? params.getDocument() : document.getUid();
  }

  @Override
  public String getType() {
    return "next_person";
  }

  @Override
  public void executeLocal() {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(getUid()))
      .get()
      .value();

    if (callback != null){
      callback.onCommandExecuteSuccess(getType());
    }

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    RApprovalNextPersonEntity rApprovalNextPersonEntity = getApprovalNextPersonEntity( getUid() );

    if ( rApprovalNextPersonEntity != null && rApprovalNextPersonEntity.isTaskStarted() ) {
      Timber.tag(TAG).i( "Task already started, quit" );
      return;
    }

    if ( rApprovalNextPersonEntity == null ) {
      createNewRApprovalNextPersonEntity( getUid() );
    }

    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );
    remoteOperation(getUid(), official_id, TAG);
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
  public void onRemoteError() {
    setTaskStarted( getUid(), false );
  }
}
