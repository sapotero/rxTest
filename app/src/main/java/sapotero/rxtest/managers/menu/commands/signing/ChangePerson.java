package sapotero.rxtest.managers.menu.commands.signing;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;

public class ChangePerson extends ApprovalSigningCommand {

  public ChangePerson(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    addToQueue();
    EventBus.getDefault().post( new ShowNextDocumentEvent( getParams().getDocument() ));

    startProcessedOperationInMemory();
    startProcessedOperationInDb();
    setAsProcessed();

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    approvalSigningRemote();
  }

  @Override
  public void finishOnOperationSuccess() {
    finishProcessedOperationOnSuccess();
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
  }
}
