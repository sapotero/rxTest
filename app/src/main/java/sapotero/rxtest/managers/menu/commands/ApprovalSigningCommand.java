package sapotero.rxtest.managers.menu.commands;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public abstract class ApprovalSigningCommand extends AbstractCommand {

  private Observable<OperationResult> getOperationResultObservable(String uid, String official_id) {
    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add(uid);

    String comment = null;
    if ( params.getComment() != null ){
      comment = params.getComment();
    }

    String sign = getSign();

    return operationService.approvalSign(
      getType(),
      settings.getLogin(),
      settings.getToken(),
      uids,
      comment,
      settings.getStatusCode(),
      official_id,
      sign
    );
  }

  protected void remoteOperation(String uid, String official_id, String TAG) {
    Observable<OperationResult> info = getOperationResultObservable(uid, official_id);

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          if (data.getMessage() != null && !data.getMessage().toLowerCase().contains("успешно") ) {
            queueManager.setExecutedWithError(this, Collections.singletonList( data.getMessage() ) );
          } else {
            queueManager.setExecutedRemote(this);
          }

          finishOperationOnSuccess( uid );
        },
        error -> {
          onError( this, uid, error.getLocalizedMessage(), true, TAG );
          onRemoteError();
        }
      );
  }

  public abstract void onRemoteError();
}
