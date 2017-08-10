package sapotero.rxtest.managers.menu.commands;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.retrofit.models.wrapper.SignWrapper;
import timber.log.Timber;

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

  protected void remoteOperation(String TAG) {
    Observable<OperationResult> info = getOperationResultObservable();

    if (info != null) {
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
            onError(error.getLocalizedMessage(), TAG);
          }
        );

    } else {
      onError(SIGN_ERROR_MESSAGE, TAG);
    }
  }

  private void onError(String errorMessage, String TAG) {
    onError( this, errorMessage, true, TAG );
    onRemoteError();
  }

  public abstract void onRemoteError();

  protected void remoteRejectedOperation(String TAG) {
    Observable<OperationResult> info = getOperationResultObservable();

    if (info != null) {
      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            printLog( data, TAG );

            if (data.getMessage() != null && !data.getMessage().toLowerCase().contains("успешно") ) {
              sendErrorCallback( data.getMessage() );
              finishRejectedOperationOnError( data.getMessage() );
            } else {
              finishRejectedOperationOnSuccess( TAG );
            }
          },

          error -> handleRejectedOperationError( error.getLocalizedMessage(), TAG )
        );

    } else {
      handleRejectedOperationError( SIGN_ERROR_MESSAGE, TAG );
    }
  }

  private void handleRejectedOperationError(String errorMessage, String TAG) {
    Timber.tag(TAG).i("error: %s", errorMessage);

    sendErrorCallback( errorMessage );

    if ( settings.isOnline() ) {
      finishRejectedOperationOnError( errorMessage );
    }

    onRemoteError();
  }
}
