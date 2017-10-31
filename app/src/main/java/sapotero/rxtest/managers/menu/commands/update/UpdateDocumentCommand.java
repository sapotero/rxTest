package sapotero.rxtest.managers.menu.commands.update;

import java.util.List;

import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import timber.log.Timber;


// resolved https://tasks.n-core.ru/browse/MPSED-2286
// В конце каждой операции ставить задачу на обновление документа.
// Плашку ожидает синхронизации снимать после того, как отработает операция и документ будет обновлён.
public class UpdateDocumentCommand extends AbstractCommand {

  private static final int DEFAULT_UPDATE_DELAY = 30;

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public UpdateDocumentCommand(CommandParams params) {
    super(params);
  }

  @Override
  public void execute() {
    queueManager.add(this);
    jobManager.addJobInBackground( new UpdateDocumentJob( getParams().getDocument(), getParams().getLogin(), getParams().getCurrentUserId(), true, false ) );
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
    int time = DEFAULT_UPDATE_DELAY;
    try {
      time = Integer.parseInt(settings.getUpdateTime());
    } catch (NumberFormatException e) {
      Timber.e(e);
    }

    if ( DateUtil.isSomeTimePassed( getParams().getUpdatedAt(), time ) ) {
      jobManager.addJobInBackground( new UpdateDocumentJob( getParams().getDocument(), getParams().getLogin(), getParams().getCurrentUserId(), true, true ) );
      queueManager.setExecutedRemote(this);
    }
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    // Not used in this command
  }
}
