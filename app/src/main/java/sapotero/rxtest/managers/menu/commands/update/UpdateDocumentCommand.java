package sapotero.rxtest.managers.menu.commands.update;

// resolved https://tasks.n-core.ru/browse/MPSED-2286
// В конце каждой операции ставить задачу на обновление документа.
// Плашку ожидает синхронизации снимать после того, как отработает операция и документ будет обновлён.

import java.util.List;

import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;

public class UpdateDocumentCommand extends AbstractCommand {

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public UpdateDocumentCommand(CommandParams params) {
    super(params);
  }

  @Override
  public void execute() {
    queueManager.add(this);
    jobManager.addJobInBackground( new UpdateDocumentJob( getParams().getDocument(), getParams().getLogin(), getParams().getCurrentUserId(), true ) );
  }

  @Override
  public String getType() {
    return "update_document";
  }

  @Override
  public void executeLocal() {
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    queueManager.setExecutedRemote(this);
    // TODO: if (enough time passed) {call UpdateDocJob with force update}
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    // Not used in this command
  }
}
