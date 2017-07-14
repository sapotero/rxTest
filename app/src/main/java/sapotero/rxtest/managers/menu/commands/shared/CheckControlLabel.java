package sapotero.rxtest.managers.menu.commands.shared;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.managers.menu.commands.SharedCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class CheckControlLabel extends SharedCommand {

  private final DocumentReceiver document;
  private String TAG = this.getClass().getSimpleName();
  private String document_id;

  public CheckControlLabel(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public CheckControlLabel withDocumentId(String uid) {
    this.document_id = uid;
    return this;
  }

  @Override
  public void execute() {
    Timber.tag(TAG).i("execute for %s - %s",getType(),document_id);
    queueManager.add(this);

    Timber.tag("RecyclerViewRefresh").d("CheckControlLabel: execute - update in MemoryStore");

    store.process(
      store.startTransactionFor(document_id)
      .setLabel(LabelType.SYNC)
      .setLabel(LabelType.CONTROL)
    );
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
    Timber.tag("RecyclerViewRefresh").d("CheckControlLabel: executeRemote success - update in DB and MemoryStore");

    InMemoryDocument docInMemory = store.getDocuments().get(document_id);

    if ( docInMemory != null ) {
      Timber.tag("RecyclerViewRefresh").d("CheckControlLabel: setAllowUpdate( false )");
      docInMemory.getDocument().setControl( true );
      docInMemory.getDocument().setChanged( false );
      docInMemory.setAllowUpdate( false );
      store.getDocuments().put(document_id, docInMemory);
      Timber.tag("RecyclerViewRefresh").d("MemoryStore: pub.onNext()");
      store.getPublishSubject().onNext( docInMemory );
    }

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, true )
      .set( RDocumentEntity.CHANGED, false )
      .where(RDocumentEntity.UID.eq(document_id))
      .get()
      .value();
  }

  @Override
  protected void setError() {
    store.process(
      store.startTransactionFor(document_id)
        .removeLabel(LabelType.SYNC)
        .removeLabel(LabelType.CONTROL)
    );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, false )
      .set( RDocumentEntity.CHANGED, false )
      .where(RDocumentEntity.UID.eq(document_id))
      .get()
      .value();
  }
}
