package sapotero.rxtest.managers.menu.commands.shared;

import java.util.List;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.managers.menu.commands.SharedCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class CheckControlLabel extends SharedCommand {

  public CheckControlLabel(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    Timber.tag(TAG).i("execute for %s - %s", getType(), getParams().getDocument());
    queueManager.add(this);

    Timber.tag("RecyclerViewRefresh").d("CheckControlLabel: execute - update in MemoryStore");

    store.process(
      store.startTransactionFor(getParams().getDocument())
      .setLabel(LabelType.SYNC)
      .setLabel(LabelType.CONTROL)
    );

    setAsProcessed();
  }

  @Override
  public String getType() {
    return "check_for_control";
  }

  @Override
  public void executeLocal() {
    Timber.tag("RecyclerViewRefresh").d("CheckControlLabel: executeLocal - update in DB");

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, true)
      .set( RDocumentEntity.CHANGED, true )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();

    queueManager.setExecutedLocal(this);

    sendSuccessCallback();
  }

  @Override
  public void executeRemote() {
    remoteControlLabelOperation();
  }

  @Override
  public void finishOnOperationSuccess() {
    Timber.tag("RecyclerViewRefresh").d("CheckControlLabel: executeRemote success - update in DB and MemoryStore");

    store.process(
      store.startTransactionFor(getParams().getDocument())
        .setLabel(LabelType.CONTROL)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, true )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();

    queueManager.setExecutedRemote(this);

    addUpdateDocumentTask();
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    store.process(
      store.startTransactionFor(getParams().getDocument())
        .removeLabel(LabelType.CONTROL)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, false )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();

    queueManager.setExecutedWithError( this, errors );

    addUpdateDocumentTask();
  }
}
