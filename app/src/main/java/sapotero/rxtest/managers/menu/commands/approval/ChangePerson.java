package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import timber.log.Timber;

public class ChangePerson extends ApprovalSigningCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String official_id;

  public ChangePerson(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public ChangePerson withPerson(String uid){
    official_id = uid;
    return this;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent());

    setDocOperationProcessedStartedInMemory( getUid() );
  }

  private String getUid() {
    return params.getDocument() != null ? params.getDocument() : settings.getUid();
  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    String uid = getUid();

    int count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(uid))
      .get()
      .value();

    queueManager.setExecutedLocal(this);

    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );
    remoteOperation(getUid(), official_id, TAG);
  }

  @Override
  protected void onRemoteSuccess() {
  }
}
