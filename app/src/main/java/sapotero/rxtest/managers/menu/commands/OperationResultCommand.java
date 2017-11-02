package sapotero.rxtest.managers.menu.commands;

import java.util.Collections;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.OperationResult;

public abstract class OperationResultCommand extends AbstractCommand {

  public OperationResultCommand(CommandParams params) {
    super(params);
  }

  protected void sendOperationRequest(Observable<OperationResult> info) {
    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        this::onOperationSuccess,
        this::onOperationError
      );
  }

  protected void onOperationSuccess(OperationResult data) {
    printOperationResult( data );

    if (data.getMessage() != null && !data.getMessage().toLowerCase().contains("успешно") ) {
      finishOnOperationError( Collections.singletonList( data.getMessage() ) );

    } else {
      finishOnOperationSuccess();
    }
  }

  public abstract void finishOnOperationSuccess();
}
