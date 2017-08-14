package sapotero.rxtest.managers.menu.commands;

import java.util.ArrayList;
import java.util.Collections;

import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;

public abstract class ApprovalSigningCommand extends OperationResultCommand {

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

  protected void approvalSigningRemote() {
    printCommandType();

    Observable<OperationResult> info = getOperationResultObservable();

    if (info != null) {
      sendOperationRequest(info);

    } else {
      sendErrorCallback( SIGN_ERROR_MESSAGE );
      finishOnOperationError( Collections.singletonList( SIGN_ERROR_MESSAGE ) );
    }
  }
}
