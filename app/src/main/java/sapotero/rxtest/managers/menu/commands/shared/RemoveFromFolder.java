package sapotero.rxtest.managers.menu.commands.shared;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Deleter;
import sapotero.rxtest.events.rx.UpdateCountEvent;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.managers.menu.commands.SharedCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class RemoveFromFolder extends SharedCommand {

  public RemoveFromFolder(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "remove_from_folder";
  }

  @Override
  public void executeLocal() {
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

    addToQueue();

    setAsProcessed();

    queueManager.setExecutedLocal(this);
    EventBus.getDefault().post( new ShowSnackEvent("Удаление из избранного.") );
  }

  @Override
  public void executeRemote() {
    printCommandType();
    Observable<OperationResult> info = getOperationResultObservable();
    sendOperationRequest( info );
  }

  @Override
  public void finishOnOperationSuccess() {

    String timestamp = DateUtil.getTimestamp();
    String timestampEarly = DateUtil.getTimestampEarly();
    Timber.d("DateUtil: now   %s | %s", timestamp, DateUtil.isSomeTimePassed(timestamp) );
    Timber.d("DateUtil: early %s | %s", timestampEarly, DateUtil.isSomeTimePassed(timestampEarly) );



    RDocumentEntity documentEntity = dataStore
      .select( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq( getParams().getDocument() ) )
      .get().firstOrNull();

    if ( documentEntity != null && documentEntity.isFromFavoritesFolder() != null && documentEntity.isFromFavoritesFolder() ) {
      store.getDocuments().remove( getParams().getDocument() );
      Timber.tag("RecyclerViewRefresh").d("RemoveFromFolder: sending event to update MainActivity");
      EventBus.getDefault().post( new UpdateCountEvent() );
      new Deleter().deleteDocument( documentEntity, true, TAG );

    } else {
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

    queueManager.setExecutedRemote(this);
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
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

    queueManager.setExecutedWithError( this, errors );
  }
}
