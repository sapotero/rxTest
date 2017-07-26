package sapotero.rxtest.managers.menu.commands.shared;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.managers.menu.commands.SharedCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class AddToFolder extends SharedCommand {

  private String TAG = this.getClass().getSimpleName();

  public AddToFolder(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .setLabel(LabelType.SYNC)
      .setLabel(LabelType.FAVORITES);
    store.process( transaction );

    Timber.tag(TAG).i("execute for %s - %s", getType(), getParams().getDocument());
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "add_to_folder";
  }

  @Override
  public void executeLocal() {
    Integer count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.FAVORITES, true )
      .set( RDocumentEntity.CHANGED, true )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get().value();
    Timber.tag(TAG).w( "updated: %s", count );

    queueManager.setExecutedLocal(this);

    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  @Override
  public void executeRemote() {
    remoteFolderOperation( this, false, TAG );
  }

  @Override
  protected void setSuccess() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .removeLabel(LabelType.SYNC)
      .setLabel(LabelType.FAVORITES);
    store.process( transaction );

    setChangedFalse();
  }

  @Override
  protected void setError() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .removeLabel(LabelType.SYNC)
      .removeLabel(LabelType.FAVORITES);
    store.process( transaction );

    dataStore
      .update( RDocumentEntity.class )
      .set( RDocumentEntity.CHANGED, false )
      .set( RDocumentEntity.FAVORITES, false )
      .where( RDocumentEntity.UID.eq( getParams().getDocument() ) )
      .get()
      .value();
  }
}
