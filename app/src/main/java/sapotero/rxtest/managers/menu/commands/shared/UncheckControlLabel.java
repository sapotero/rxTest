package sapotero.rxtest.managers.menu.commands.shared;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.managers.menu.commands.SharedCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class UncheckControlLabel extends SharedCommand {

  private final DocumentReceiver document;
  private String TAG = this.getClass().getSimpleName();
  private String document_id;

  public UncheckControlLabel(DocumentReceiver document){
    super();
    this.document = document;
  }

  public UncheckControlLabel withDocumentId(String uid) {
    this.document_id = uid;
    return this;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    Timber.tag(TAG).i("execute for %s - %s",getType(),document_id);
    queueManager.add(this);

    store.process(
      store.startTransactionFor(document_id)
        .setLabel(LabelType.SYNC)
        .removeLabel(LabelType.CONTROL)
    );
  }

  @Override
  public String getType() {
    return "uncheck_control_label";
  }

  @Override
  public void executeLocal() {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, false )
      .set( RDocumentEntity.CHANGED, true )
      .where(RDocumentEntity.UID.eq(document_id))
      .get()
      .value();

    queueManager.setExecutedLocal(this);

    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  @Override
  public void executeRemote() {
    remoteControlLabelOperation( this, document_id, TAG );
  }

  @Override
  protected void setSuccess() {
    setControlLabelSuccess(document_id);
  }

  @Override
  protected void setError() {
    store.process(
      store.startTransactionFor(document_id)
        .removeLabel(LabelType.SYNC)
        .setLabel(LabelType.CONTROL)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, true)
      .set( RDocumentEntity.CHANGED, false )
      .where(RDocumentEntity.UID.eq(document_id))
      .get()
      .value();
  }
}
