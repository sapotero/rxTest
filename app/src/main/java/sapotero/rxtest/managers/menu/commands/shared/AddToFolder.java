package sapotero.rxtest.managers.menu.commands.shared;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.managers.menu.commands.SharedCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class AddToFolder extends SharedCommand {

  public AddToFolder(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "add_to_folder";
  }

  @Override
  public void executeLocal() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .setLabel(LabelType.SYNC)
      .setLabel(LabelType.FAVORITES);
    store.process( transaction );

    Timber.tag(TAG).i("execute for %s - %s", getType(), getParams().getDocument());

    addToQueue();

    setAsProcessed();


    int count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.FAVORITES, true )
      .set( RDocumentEntity.CHANGED, true )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get().value();
    Timber.tag(TAG).w( "updated: %s", count );

    queueManager.setExecutedLocal(this);
    EventBus.getDefault().post( new ShowSnackEvent("Добавление в избранное.") );
  }

  @Override
  public void executeRemote() {
    printCommandType();
    Observable<OperationResult> info = getOperationResultObservable();
    sendOperationRequest( info );
  }

  @Override
  public void finishOnOperationSuccess() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .removeLabel(LabelType.SYNC)
      .setLabel(LabelType.FAVORITES);
    store.process( transaction );

    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CHANGED, false)
      .set( RDocumentEntity.FAVORITES, true )
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();

    queueManager.setExecutedRemote(this);
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
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

    queueManager.setExecutedWithError( this, errors );
  }
}
