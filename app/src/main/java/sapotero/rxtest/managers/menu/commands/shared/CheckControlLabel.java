package sapotero.rxtest.managers.menu.commands.shared;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.document.DropControlEvent;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class CheckControlLabel extends AbstractCommand {

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
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

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
      null,
      null
    );


    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(document_id))
      .get().firstOrNull();

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          Timber.tag(TAG).i("ok: %s", result.getOk());
          Timber.tag(TAG).i("error: %s", result.getMessage());
          Timber.tag(TAG).i("type: %s", result.getType());

          if ( Objects.equals(result.getType(), "danger") && result.getMessage() != null){
            EventBus.getDefault().post( new ShowSnackEvent( result.getMessage() ));

            if (doc != null) {
              EventBus.getDefault().post( new DropControlEvent( doc.isControl() ));
            }

            queueManager.setExecutedWithError( this, Collections.singletonList( result.getMessage() ) );
            setError();

          } else {
            if (callback != null){
              callback.onCommandExecuteSuccess(getType());
            }

            if (result.getMessage() != null && !result.getMessage().toLowerCase().contains("успешно") ) {
              queueManager.setExecutedWithError(this, Collections.singletonList( result.getMessage() ) );
              setError();
            } else {
              queueManager.setExecutedRemote(this);
              setSuccess();
            }
          }
        },
        error -> {
          if (callback != null){
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
    store.process(
      store.startTransactionFor(document_id)
        .removeLabel(LabelType.SYNC)
    );

    setChangedFalse(document_id);
  }

  private void setError() {
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
