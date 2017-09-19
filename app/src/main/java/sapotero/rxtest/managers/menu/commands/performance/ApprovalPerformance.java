package sapotero.rxtest.managers.menu.commands.performance;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class ApprovalPerformance extends AbstractCommand {

  public ApprovalPerformance(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent( getParams().getDocument() ));
    setAsProcessed();
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add( getParams().getDocument() );

    Observable<OperationResult> info = operationService.performance(
      getType(),
      getParams().getLogin(),
      settings.getToken(),
      uids,
      getParams().getDocument(),
      getParams().getStatusCode(),
      getParams().getPerson()
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          Transaction transaction = new Transaction();
          transaction
            .from( store.getDocuments().get(getParams().getDocument()) )
            .setField(FieldType.UPDATED_AT, DateUtil.getTimestamp())
            .removeLabel(LabelType.SYNC);
          store.process( transaction );

          queueManager.setExecutedRemote(this);
        },
        error -> {
          sendErrorCallback( getType() );
        }
      );
  }

  @Override
  public void executeLocal() {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get()
      .value();

    sendErrorCallback( getType() );

    queueManager.setExecutedLocal(this);
  }

  @Override
  public String getType() {
    return "to_the_approval_performance";
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
  }
}
