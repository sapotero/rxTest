package sapotero.rxtest.managers.menu.commands.report;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import timber.log.Timber;

public class ReturnToPrimaryConsideration extends AbstractCommand {

  private String TAG = this.getClass().getSimpleName();

  public ReturnToPrimaryConsideration(CommandParams params) {
    super(params);
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    update();
    setDocOperationProcessedStartedInMemory( getParams().getDocument() );
  }

  private void update() {
    String uid = getParams().getDocument();

    dataStore
      .update(RDocumentEntity.class)
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
    return "return_to_the_primary_consideration";
  }

  @Override
  public void executeLocal() {
    if (callback != null){
      callback.onCommandExecuteSuccess(getType());
    }

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();

    String uid = getParams().getDocument();

    uids.add( uid );

    Observable<OperationResult> info = operationService.report(
      getType(),
      settings.getLogin(),
      settings.getToken(),
      uids,
      uid,
      getParams().getStatusCode()
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          queueManager.setExecutedRemote(this);

          finishOperationOnSuccess( getParams().getDocument() );

        },
        error -> onError( this, getParams().getDocument(), error.getLocalizedMessage(), true, TAG )
      );
  }
}
