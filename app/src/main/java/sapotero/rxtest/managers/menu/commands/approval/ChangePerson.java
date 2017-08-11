package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;

public class ChangePerson extends ApprovalSigningCommand {

  public ChangePerson(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ));

    startRejectedOperationInMemory();
    setAsProcessed();
  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    startRejectedOperationInDb();
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    printCommandType();
    remoteRejectedOperation();
  }

  @Override
  public void onRemoteError() {
  }
}
