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
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;

public abstract class SharedCommand extends OperationResultCommand {

  public SharedCommand(CommandParams params) {
    super(params);
  }

  private void onControlLabelSuccess(OperationResult result, RDocumentEntity doc) {
    if ( Objects.equals(result.getType(), "danger") && result.getMessage() != null){
      printOperationResult( result );

      EventBus.getDefault().post( new ShowSnackEvent( result.getMessage() ));

      if (doc != null) {
        EventBus.getDefault().post( new DropControlEvent( doc.isControl() ));
      }

      finishOnOperationError( Collections.singletonList( result.getMessage() ) );

    } else {
      onOperationSuccess( result );
    }
  }

  protected Observable<OperationResult> getOperationResultObservable() {
    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    String uid = getParams().getDocument();

    ArrayList<String> uids = new ArrayList<>();
    uids.add( uid );

    return operationService.shared(
      getType(),
      getParams().getLogin(),
      settings.getToken(),
      uids,
      uid,
      getParams().getStatusCode(),
      getParams().getFolder(),
      null
    );
  }

  protected void remoteControlLabelOperation() {
    printCommandType();

    Observable<OperationResult> info = getOperationResultObservable();

    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(getParams().getDocument()))
      .get().firstOrNull();

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> onControlLabelSuccess( result, doc ),
        this::onOperationError
      );
  }
}