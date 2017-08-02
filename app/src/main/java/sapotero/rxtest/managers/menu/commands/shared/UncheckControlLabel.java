package sapotero.rxtest.managers.menu.commands.shared;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.managers.menu.commands.SharedCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class UncheckControlLabel extends SharedCommand {

  private String TAG = this.getClass().getSimpleName();

  public UncheckControlLabel(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    Timber.tag(TAG).i("execute for %s - %s", getType(), getParams().getDocument());
    queueManager.add(this);

    Timber.tag("RecyclerViewRefresh").d("UncheckControlLabel: execute - update in MemoryStore");

    store.process(
      store.startTransactionFor(getParams().getDocument())
        .setLabel(LabelType.SYNC)
        .removeLabel(LabelType.CONTROL)
    );

    setAsProcessed();
  }

  @Override
  public String getType() {
    return "uncheck_control_label";
  }

  @Override
  public void executeLocal() {
    Timber.tag("RecyclerViewRefresh").d("UncheckControlLabel: executeLocal - update in DB");

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, false )
      .set( RDocumentEntity.CHANGED, true )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();

    queueManager.setExecutedLocal(this);

    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  @Override
  public void executeRemote() {
    remoteControlLabelOperation( this, TAG );
  }

  @Override
  protected void setSuccess() {
    Timber.tag("RecyclerViewRefresh").d("UncheckControlLabel: executeRemote success - update in DB and MemoryStore");

    InMemoryDocument docInMemory = store.getDocuments().get(getParams().getDocument());

    if ( docInMemory != null ) {
      Timber.tag("RecyclerViewRefresh").d("UncheckControlLabel: setAllowUpdate( false )");
      docInMemory.getDocument().setControl( false );
      docInMemory.getDocument().setChanged( false );
      docInMemory.setAllowUpdate( false );
      store.getDocuments().put(getParams().getDocument(), docInMemory);
      Timber.tag("RecyclerViewRefresh").d("MemoryStore: pub.onNext()");
      store.getPublishSubject().onNext( docInMemory );
    }

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, false )
      .set( RDocumentEntity.CHANGED, false )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();
  }

  @Override
  protected void setError() {
    store.process(
      store.startTransactionFor(getParams().getDocument())
        .removeLabel(LabelType.SYNC)
        .setLabel(LabelType.CONTROL)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, true )
      .set( RDocumentEntity.CHANGED, false )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();
  }
}
