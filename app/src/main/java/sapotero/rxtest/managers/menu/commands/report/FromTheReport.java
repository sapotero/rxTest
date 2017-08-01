package sapotero.rxtest.managers.menu.commands.report;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public class FromTheReport extends AbstractCommand {

  private String TAG = this.getClass().getSimpleName();

  public FromTheReport(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    update();
    setDocOperationProcessedStartedInMemory();
  }

  @Override
  public String getType() {
    return "from_the_report";
  }

  private void update(){
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
    uids.add( getParams().getDocument() );

    String comment = null;

    if (getParams().getComment() != null){
      comment = getParams().getComment();
    }

    Observable<OperationResult> info = operationService.report(
      getType(),
      getParams().getLogin(),
      settings.getToken(),
      uids,
      comment,
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

          finishOperationOnSuccess();

        },
        error -> onError( this, error.getLocalizedMessage(), true, TAG )
      );
  }
}
