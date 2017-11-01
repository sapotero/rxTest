package sapotero.rxtest.managers.menu.commands.report;

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

public class FromTheReport extends OperationResultCommand {

  public FromTheReport(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "from_the_report";
  }

  @Override
  public void executeLocal() {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent( getParams().getDocument() ));

    startProcessedOperationInMemory();
    startProcessedOperationInDb();
    setAsProcessed();

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

    sendOperationRequest( info );
  }

  @Override
  public void finishOnOperationSuccess() {
    // Do not save document condition, just finish operation
    finishOperationOnSuccess();
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
  }
}
