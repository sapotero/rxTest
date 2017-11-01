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

  private static final int FIRST_UPDATE_DELAY = 5;
  private static final int DEFAULT_SECOND_UPDATE_DELAY = 15;

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public UpdateDocumentCommand(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "update_document";
  }

  @Override
  public void executeLocal() {
    // After first delay time has passed, update document if MD5 has changed
    Timber.tag(TAG).d("executeLocal - start");

    if ( DateUtil.isSomeTimePassed( getParams().getUpdatedAt(), getFirstUpdateDelay() ) ) {
      Timber.tag(TAG).d("executeLocal - updating document");
      jobManager.addJobInBackground( new UpdateDocumentJob( getParams().getDocument(), getParams().getLogin(), getParams().getCurrentUserId(), true, false ) );
      queueManager.setExecutedLocal(this);

      // From UpdateDocumentCommand we don't call sendSuccessCallback(), because it is not needed in this command
    }
  }

  private int getFirstUpdateDelay() {
    return FIRST_UPDATE_DELAY;
  }

  @Override
  public void executeRemote() {
    // After second delay time has passed, force update document even if MD5 hasn't changed
    Timber.tag(TAG).d("executeRemote - start");

    if ( DateUtil.isSomeTimePassed( getParams().getUpdatedAt(), getSecondUpdateDelay() ) ) {
      Timber.tag(TAG).d("executeRemote - force updating document");
      jobManager.addJobInBackground( new UpdateDocumentJob( getParams().getDocument(), getParams().getLogin(), getParams().getCurrentUserId(), true, true ) );
    }

    // UpdateDocumentCommand is set executed remote only from inside UpdateDocumentJob in case of document update success.
    // Here we do not call setExecutedRemote(), because at this moment we don't know, whether the document has been updated or not, yet.
  }

  private int getSecondUpdateDelay() {
    int secondUpdateDelay = DEFAULT_SECOND_UPDATE_DELAY;

    try {
      secondUpdateDelay = Integer.parseInt( settings.getUpdateTime() );
    } catch (NumberFormatException e) {
      Timber.e(e);
    }

    return secondUpdateDelay;
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    // Not used in this command
  }
}
