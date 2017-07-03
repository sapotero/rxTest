package sapotero.rxtest.managers.menu.commands.shared;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class AddToFolder extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String folder_id;
  private String document_id;

  public AddToFolder(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public AddToFolder withFolder(String uid){
    folder_id = uid;
    return this;
  }

  public AddToFolder withDocumentId(String uid) {
    this.document_id = uid;
    return this;
  }

  @Override
  public void execute() {

    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(document_id) )
      .setLabel(LabelType.SYNC)
      .setLabel(LabelType.FAVORITES);
    store.process( transaction );

    Timber.tag(TAG).i("execute for %s - %s",getType(),document_id);
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
      .where(RDocumentEntity.UID.eq(document_id))
      .get().value();
    Timber.tag(TAG).w( "updated: %s", count );

    queueManager.setExecutedLocal(this);

    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  @Override
  public void executeRemote() {
    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add( settings.getUid() );

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

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
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
          }
        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }

          if ( settings.isOnline() ) {
            queueManager.setExecutedRemote(this);
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
      .setLabel(LabelType.FAVORITES);
    store.process( transaction );

    dataStore
      .update( RDocumentEntity.class )
      .set( RDocumentEntity.CHANGED, false )
      .where( RDocumentEntity.UID.eq( document_id ) )
      .get()
      .value();
  }

  private void setError() {
    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(document_id) )
      .removeLabel(LabelType.SYNC)
      .removeLabel(LabelType.FAVORITES);
    store.process( transaction );

    dataStore
      .update( RDocumentEntity.class )
      .set( RDocumentEntity.CHANGED, false )
      .set( RDocumentEntity.FAVORITES, false )
      .where( RDocumentEntity.UID.eq( document_id ) )
      .get()
      .value();
  }
}
