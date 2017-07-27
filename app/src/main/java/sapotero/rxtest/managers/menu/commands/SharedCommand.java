package sapotero.rxtest.managers.menu.commands;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.document.DropControlEvent;
import sapotero.rxtest.events.utils.RecalculateMenuEvent;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public abstract class SharedCommand extends AbstractCommand {

  public SharedCommand(CommandParams params) {
    super(params);
  }

  public void onError(Command command, String message) {
    if (callback != null){
      callback.onCommandExecuteError(getType());
    }

    if ( settings.isOnline() ) {
      queueManager.setExecutedWithError( command, Collections.singletonList( message ) );
      setError();
    }
  }

  protected abstract void setError();

  private void checkMessage(Command command, String message, boolean recalculateMenu) {
    Timber.tag("RecyclerViewRefresh").d("SharedCommand: response message: %s", message);

    if (message != null && !message.toLowerCase().contains("успешно") ) {
      queueManager.setExecutedWithError( command, Collections.singletonList( message ) );
      setError();
    } else {
      queueManager.setExecutedRemote( command );
      setSuccess();
      if ( recalculateMenu ) {
        EventBus.getDefault().post( new RecalculateMenuEvent() );
      }
    }
  }

  protected abstract void setSuccess();

  private void onControlLabelSuccess(Command command, OperationResult result, RDocumentEntity doc, String TAG) {
    printLog( result, TAG );

    if ( Objects.equals(result.getType(), "danger") && result.getMessage() != null){
      EventBus.getDefault().post( new ShowSnackEvent( result.getMessage() ));

      if (doc != null) {
        EventBus.getDefault().post( new DropControlEvent( doc.isControl() ));
      }

      queueManager.setExecutedWithError( command, Collections.singletonList( result.getMessage() ) );
      setError();

    } else {
      checkMessage( command, result.getMessage(), false );
    }
  }

  private void onFolderSuccess(Command command, OperationResult data, boolean recalculateMenu, String TAG) {
    printLog( data, TAG );
    checkMessage( command, data.getMessage(), recalculateMenu );
  }

  private Observable<OperationResult> getOperationResultObservable() {
    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    String uid = getParams().getDocument();

    ArrayList<String> uids = new ArrayList<>();
    uids.add( uid );

    return operationService.shared(
      getType(),
      getParams().getLogin(),
      getParams().getToken(),
      uids,
      uid,
      getParams().getStatusCode(),
      getParams().getFolder(),
      null
    );
  }

  protected void remoteFolderOperation(Command command, boolean recalculateMenu, String TAG) {
    printCommandType( command, TAG );

    Observable<OperationResult> info = getOperationResultObservable();

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> onFolderSuccess( command, data, recalculateMenu, TAG ),
        error -> onError( command, error.getLocalizedMessage() )
      );
  }

  protected void remoteControlLabelOperation(Command command, String TAG) {
    printCommandType( command, TAG );

    Observable<OperationResult> info = getOperationResultObservable();

    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get().firstOrNull();

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> onControlLabelSuccess( command, result, doc, TAG ),
        error -> onError( command, error.getLocalizedMessage() )
      );
  }
}
