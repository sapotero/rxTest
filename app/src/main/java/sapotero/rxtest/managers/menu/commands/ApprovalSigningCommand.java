package sapotero.rxtest.managers.menu.commands;

import java.util.ArrayList;
import java.util.Collections;

import okhttp3.RequestBody;
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

  protected Observable<OperationResult> getOperationResultObservable() {
    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add(getParams().getDocument());

    String comment = null;
    if ( getParams().getComment() != null ){
      comment = getParams().getComment();
    }

    String sign = getSign();

    Observable<OperationResult> info;

    if (sign != null) {
      RequestBody signBody = getSignBody(sign);

      info = operationService.approvalSign(
        getType(),
        getParams().getLogin(),
        settings.getToken(),
        uids,
        comment,
        getParams().getStatusCode(),
        getParams().getPerson(),
        signBody
      );

    } else {
      info = null;
    }

    return info;
  }

  protected void remoteOperation() {
    Observable<OperationResult> info = getOperationResultObservable();

    if (info != null) {
      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            printOperationResult( data );

            if (data.getMessage() != null && !data.getMessage().toLowerCase().contains("успешно") ) {
              queueManager.setExecutedWithError(this, Collections.singletonList( data.getMessage() ) );
            } else {
              queueManager.setExecutedRemote(this);
            }

            finishOperationOnSuccess();
          },
          error -> {
            onError(error.getLocalizedMessage());
          }
        );

    } else {
      onError(SIGN_ERROR_MESSAGE);
    }
  }

  private void onError(String errorMessage) {
    onError( errorMessage, true );
    onRemoteError();
  }

  public abstract void onRemoteError();

  protected void remoteRejectedOperation() {
    Observable<OperationResult> info = getOperationResultObservable();

    if (info != null) {
      sendRejectedOperationRequest( info );
    } else {
      handleRejectedOperationError( SIGN_ERROR_MESSAGE );
    }
  }
}
