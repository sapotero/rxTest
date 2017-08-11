package sapotero.rxtest.managers.menu.commands.report;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import retrofit2.Retrofit;
import rx.Observable;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import timber.log.Timber;

public class ReturnToPrimaryConsideration extends AbstractCommand {

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
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ));

    startRejectedOperationInMemory();
    setAsProcessed();
  }

  @Override
  public String getType() {
    return "return_to_the_primary_consideration";
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

    String uid = getParams().getDocument();

    uids.add( uid );

    Observable<OperationResult> info = operationService.report(
      getType(),
      getParams().getLogin(),
      settings.getToken(),
      uids,
      uid,
      getParams().getStatusCode()
    );

    sendRejectedOperationRequest( info );
  }
}
