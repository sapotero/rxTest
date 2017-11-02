package sapotero.rxtest.managers.menu.commands.primary_consideration;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import rx.Observable;
import sapotero.rxtest.managers.menu.commands.OperationResultCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public class PrimaryConsideration extends OperationResultCommand {

  public PrimaryConsideration(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "to_the_primary_consideration";
  }

  @Override
  public void executeLocal() {
    local( false );
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
    finishProcessedOperationOnSuccess();
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
  }
}
