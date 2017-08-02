package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;

public class ChangePerson extends ApprovalSigningCommand {

  private String TAG = this.getClass().getSimpleName();

  public ChangePerson(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ));

    setDocOperationProcessedStartedInMemory();
    setAsProcessed();
  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    String uid = getParams().getDocument();

    dataStore
      .update(RDocumentEntity.class)
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
    printCommandType( this, TAG );
    remoteOperation(TAG);
  }

  @Override
  public void onRemoteError() {
  }
}
