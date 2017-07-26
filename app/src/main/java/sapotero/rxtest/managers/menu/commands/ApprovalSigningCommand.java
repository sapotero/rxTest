package sapotero.rxtest.managers.menu.commands;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;

public abstract class ApprovalSigningCommand extends AbstractCommand {

  public ApprovalSigningCommand(CommandParams params) {
    super(params);
  }

  private Observable<OperationResult> getOperationResultObservable() {
    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add(getParams().getDocument());

    String comment = null;
    if ( getParams().getComment() != null ){
      comment = getParams().getComment();
    }

    String sign = getSign();

    return operationService.approvalSign(
      getType(),
      getParams().getUser(),
      getParams().getToken(),
      uids,
      comment,
      getParams().getStatusCode(),
      getParams().getPerson(),
      sign
    );
  }

  protected void remoteOperation(String TAG) {
    Observable<OperationResult> info = getOperationResultObservable();

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          printLog( data, TAG );

          if (data.getMessage() != null && !data.getMessage().toLowerCase().contains("успешно") ) {
            queueManager.setExecutedWithError(this, Collections.singletonList( data.getMessage() ) );
          } else {
            queueManager.setExecutedRemote(this);
          }

          finishOperationOnSuccess();
        },
        error -> {
          onError( this, error.getLocalizedMessage(), true, TAG );
          onRemoteError();
        }
      );
  }

  public abstract void onRemoteError();
}
