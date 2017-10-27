package sapotero.rxtest.managers.menu.commands.update;

// resolved https://tasks.n-core.ru/browse/MPSED-2286
// В конце каждой операции ставить задачу на обновление документа.
// Плашку ожидает синхронизации снимать после того, как отработает операция и документ будет обновлён.

import java.util.List;

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

  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    // Not used in this command
  }
}
