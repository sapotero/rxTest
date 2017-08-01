package sapotero.rxtest.managers.menu.commands;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.retrofit.models.wrapper.DecisionWrapper;
import timber.log.Timber;

public abstract class DecisionCommand extends AbstractCommand {

  public DecisionCommand(CommandParams params) {
    super(params);
  }

  protected Observable<DecisionError> getDecisionCreateOperationObservable(Decision decision, String TAG) {
    String json_m = new Gson().toJson( decision );

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      json_m
    );

    Timber.tag(TAG).e("DECISION");
    Timber.tag(TAG).e("%s", json_m);

    Retrofit retrofit = getRetrofit();
    DocumentService operationService = retrofit.create( DocumentService.class );

    return operationService.create(
      getParams().getLogin(),
      settings.getToken(),
      json
    );
  }

  protected Observable<DecisionError> getDecisionUpdateOperationObservable(Decision decision, String TAG) {
    DecisionWrapper wrapper = new DecisionWrapper();
    wrapper.setDecision(decision);

    String json_d = new Gson().toJson( wrapper );
    Timber.w("decision_json: %s", json_d);

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      json_d
    );

    Timber.tag(TAG).e("DECISION");
    Timber.tag(TAG).e("%s", json);

    Retrofit retrofit = getRetrofit();
    DocumentService operationService = retrofit.create( DocumentService.class );

    return operationService.update(
      getParams().getDecisionId(),
      getParams().getLogin(),
      settings.getToken(),
      json
    );
  }

  protected void onSuccess(Command command, DecisionError data, boolean sendEvents, boolean updateDecisionFirstTable, String TAG) {
    if ( notEmpty( data.getErrors() ) ) {
      queueManager.setExecutedWithError(command, data.getErrors());

      if ( sendEvents ) {
        EventBus.getDefault().post( new ForceUpdateDocumentEvent( data.getDocumentUid() ));
      }

    } else {
      queueManager.setExecutedRemote(command);

      if ( sendEvents ) {
        EventBus.getDefault().post( new UpdateDocumentEvent( data.getDocumentUid() ));
      }

      if ( updateDecisionFirstTable ) {
        checkCreatorAndSignerIsCurrentUser(data, TAG);
      }
    }
  }
}
