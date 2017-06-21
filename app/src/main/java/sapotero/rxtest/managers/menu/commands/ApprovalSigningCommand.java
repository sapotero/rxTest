package sapotero.rxtest.managers.menu.commands;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public abstract class ApprovalSigningCommand extends AbstractCommand {

  protected Observable<OperationResult> getOperationResultObservable(String uid, String official_id) {
    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add(uid);

    String comment = null;
    if ( params.getComment() != null ){
      comment = params.getComment();
    }

    String sign = null;

    try {
      sign = MainService.getFakeSign( settings.getPin(), null );
    } catch (Exception e) {
      e.printStackTrace();
    }

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

  public void remoteOperation(String uid, String official_id, String TAG) {
    Observable<OperationResult> info = getOperationResultObservable(uid, official_id);

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          onRemoteSuccess();

          queueManager.setExecutedRemote(this);

          store.process(
            store.startTransactionFor( uid )
              .removeLabel(LabelType.SYNC)
              .setField(FieldType.MD5, "")
              .setState(InMemoryState.READY)
          );
        },
        error -> {
          if (callback != null) {
            callback.onCommandExecuteError(getType());
          }

          if ( settings.isOnline() ){
            store.process(
              store.startTransactionFor( uid )
                .removeLabel(LabelType.SYNC)
                .setField(FieldType.PROCESSED, false)
                .setState(InMemoryState.READY)
            );

            queueManager.setExecutedWithError(this, Collections.singletonList(error.getLocalizedMessage()));
          }
        }
      );
  }

  protected abstract void onRemoteSuccess();
}
