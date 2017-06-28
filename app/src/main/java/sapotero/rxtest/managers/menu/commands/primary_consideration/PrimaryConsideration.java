package sapotero.rxtest.managers.menu.commands.primary_consideration;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public class PrimaryConsideration extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String official_id;

  public PrimaryConsideration(DocumentReceiver document){
    super();
    this.document = document;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public PrimaryConsideration withPerson(String uid){
    official_id = uid;
    return this;
  }

  @Override
  public void execute() {

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( params.getDocument() ))
      .get()
      .value();

    queueManager.add(this);

    setDocOperationProcessedStartedInMemory( params.getDocument() );
  }

  private void update(){
    String uid = null;

    if (params.getDocument() != null && !Objects.equals(params.getDocument(), "")){
      uid = params.getDocument();
    }

    if (document.getUid() != null && !Objects.equals(document.getUid(), "")){
      uid = document.getUid();
    }


    Timber.tag(TAG).i( "3 updateLocal document uid:\n%s\n%s\n", params.getDocument(), document.getUid() );


    int count = dataStore
      .update(RDocumentEntity.class)
//      .set( RDocumentEntity.FILTER, Fields.Status.PROCESSED.getValue() )
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(uid))
      .get()
      .value();
    EventBus.getDefault().post( new ShowNextDocumentEvent());
  }


  @Override
  public String getType() {
    return "to_the_primary_consideration";
  }

  @Override
  public void executeLocal() {
    int count = dataStore
      .update(RDocumentEntity.class)
//      .set( RDocumentEntity.FILTER, Fields.Status.PROCESSED.getValue() )
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(params.getDocument() != null ? params.getDocument(): settings.getUid()))
      .get()
      .value();
    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    update();

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add( params.getDocument() != null ? params.getDocument(): settings.getUid() );

    Observable<OperationResult> info = operationService.consideration(
      getType(),
      settings.getLogin(),
      settings.getToken(),
      uids,
      settings.getUid(),
      settings.getStatusCode(),
      official_id
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          queueManager.setExecutedRemote(this);

          finishOperationOnSuccess( params.getDocument() );

        },
        error -> onError( this, params.getDocument(), error.getLocalizedMessage(), true, TAG )
      );
  }
}
