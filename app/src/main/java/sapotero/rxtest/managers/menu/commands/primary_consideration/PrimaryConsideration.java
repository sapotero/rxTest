package sapotero.rxtest.managers.menu.commands.primary_consideration;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import rx.Observable;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.OperationResultCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public class PrimaryConsideration extends OperationResultCommand {

  public PrimaryConsideration(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ));

    startRejectedOperationInMemory();
    setAsProcessed();
  }

  @Override
  public String getType() {
    return "to_the_primary_consideration";
  }

  @Override
  public void executeLocal() {
    startRejectedOperationInDb();
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add( getParams().getDocument() );

    Observable<OperationResult> info = operationService.consideration(
      getType(),
      getParams().getLogin(),
      settings.getToken(),
      uids,
      getParams().getDocument(),
      getParams().getStatusCode(),
      getParams().getPerson()
    );

    sendOperationRequest( info );
  }

  @Override
  public void finishOnOperationSuccess() {
    finishRejectedOperationOnSuccess();
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
  }
}
