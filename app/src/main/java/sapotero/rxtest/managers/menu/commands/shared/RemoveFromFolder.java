package sapotero.rxtest.managers.menu.commands.shared;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Deleter;
import sapotero.rxtest.events.rx.UpdateCountEvent;
import sapotero.rxtest.managers.menu.commands.SharedCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class RemoveFromFolder extends SharedCommand {

  private String TAG = this.getClass().getSimpleName();

  public RemoveFromFolder(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.FAVORITES, false )
      .set( RDocumentEntity.CHANGED, true )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get().value();

    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .setLabel(LabelType.SYNC)
      .removeLabel(LabelType.FAVORITES);
    store.process( transaction );

    queueManager.add(this);

    setAsProcessed();
  }

  @Override
  public String getType() {
    return "remove_from_folder";
  }

  @Override
  public void executeLocal() {
    queueManager.setExecutedLocal(this);

    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  @Override
  public void executeRemote() {
    remoteFolderOperation( this, true, TAG );
  }

  @Override
  protected void setSuccess() {
    RDocumentEntity documentEntity = dataStore
      .select( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq( getParams().getDocument() ) )
      .get().firstOrNull();

    if ( documentEntity != null && documentEntity.isFromFavoritesFolder() != null && documentEntity.isFromFavoritesFolder() ) {
      store.getDocuments().remove( getParams().getDocument() );
      Timber.tag("RecyclerViewRefresh").d("RemoveFromFolder: sending event to update MainActivity");
      EventBus.getDefault().post( new UpdateCountEvent() );
      new Deleter().deleteDocument( documentEntity, TAG );

    } else {
      Transaction transaction = new Transaction();
      transaction
        .from( store.getDocuments().get(getParams().getDocument()) )
        .removeLabel(LabelType.SYNC)
        .removeLabel(LabelType.FAVORITES);
      store.process( transaction );

      setChangedFalse();
    }
  }

  @Override
  protected void setError() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .removeLabel(LabelType.SYNC)
      .setLabel(LabelType.FAVORITES);
    store.process( transaction );

    dataStore
      .update( RDocumentEntity.class )
      .set( RDocumentEntity.CHANGED, false )
      .set( RDocumentEntity.FAVORITES, true )
      .where( RDocumentEntity.UID.eq( getParams().getDocument() ) )
      .get()
      .value();
  }
}
