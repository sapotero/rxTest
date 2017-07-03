package sapotero.rxtest.managers.menu.commands.shared;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.utils.RecalculateMenuEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class RemoveFromFolder extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String folder_id;
  private String document_id;

  public RemoveFromFolder(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public RemoveFromFolder withFolder(String uid){
    folder_id = uid;
    return this;
  }

  public RemoveFromFolder withDocumentId(String uid) {
    this.document_id = uid;
    return this;
  }

  @Override
  public void execute() {
    Integer count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.FAVORITES, false )
      .set( RDocumentEntity.FROM_FAVORITES_FOLDER, false )
      .set( RDocumentEntity.CHANGED, true )
      .where(RDocumentEntity.UID.eq(document_id))
      .get().value();


    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(document_id) )
      .setLabel(LabelType.SYNC)
      .removeLabel(LabelType.FAVORITES);
    store.process( transaction );

    queueManager.add(this);
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
    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create(OperationService.class);

    ArrayList<String> uids = new ArrayList<>();
    uids.add(settings.getUid());

    Observable<OperationResult> info = operationService.shared(
      getType(),
      settings.getLogin(),
      settings.getToken(),
      uids,
      document_id == null ? settings.getUid() : document_id,
      settings.getStatusCode(),
      folder_id,
      null
    );

    info.subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          if (data.getMessage() != null && !data.getMessage().toLowerCase().contains("успешно") ) {
            queueManager.setExecutedWithError(this, Collections.singletonList( data.getMessage() ) );
            setError();
          } else {
            queueManager.setExecutedRemote(this);
            setSuccess();
            EventBus.getDefault().post( new RecalculateMenuEvent() );
          }
        },
        error -> {
          if (callback != null) {
            callback.onCommandExecuteError(getType());
          }

          if ( settings.isOnline() ) {
            queueManager.setExecutedWithError( this, Collections.singletonList( error.getLocalizedMessage() ) );
            setError();
          }
        }
      );
  }

  private void setSuccess() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(document_id) )
      .removeLabel(LabelType.SYNC)
      .removeLabel(LabelType.FAVORITES);
    store.process( transaction );

    setChangedFalse(document_id);
  }

  private void setError() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(document_id) )
      .removeLabel(LabelType.SYNC)
      .setLabel(LabelType.FAVORITES);
    store.process( transaction );

    dataStore
      .update( RDocumentEntity.class )
      .set( RDocumentEntity.CHANGED, false )
      .set( RDocumentEntity.FAVORITES, true )
      .where( RDocumentEntity.UID.eq( document_id ) )
      .get()
      .value();
  }
}
