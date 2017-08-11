package sapotero.rxtest.managers.menu.commands;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.requery.query.Tuple;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
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

  protected Observable<DecisionError> getDecisionCreateOperationObservable(Decision decision) {
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

  protected Observable<DecisionError> getDecisionUpdateOperationObservable(Decision decision) {
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

  protected void onDecisionSuccess(DecisionError data, boolean updateDecisionFirstTable) {
    if ( notEmpty( data.getErrors() ) ) {
      sendErrorCallback( "error" );
      finishOnError( data.getErrors() );

    } else {
      removeSyncChanged();
      queueManager.setExecutedRemote(this);
      EventBus.getDefault().post( new UpdateDocumentEvent( data.getDocumentUid() ));

      if ( updateDecisionFirstTable ) {
        checkCreatorAndSignerIsCurrentUser(data);
      }
    }
  }

  protected void onDecisionError(String errorMessage) {
    Timber.tag(TAG).i("error: %s", errorMessage);

    sendErrorCallback( errorMessage );

    if ( settings.isOnline() ) {
      finishOnError( Collections.singletonList( errorMessage ) );
    }
  }

  public abstract void finishOnError(List<String> errors);

  protected boolean signerIsCurrentUser() {
    return Objects.equals( getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId() );
  }

  protected void setDecisionTemporary() {
    Integer count = dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq( getParams().getDecisionModel().getId() ))
      .get().value();

    Timber.tag(TAG).i( "updateLocal: %s", count );
  }

  protected boolean isActiveOrRed() {
    Tuple red = dataStore
      .select(RDecisionEntity.RED)
      .where(RDecisionEntity.UID.eq(getParams().getDecisionModel().getId()))
      .get().firstOrNull();

    return
      // если активная резолюция
      signerIsCurrentUser()

      // или если подписывающий министр
      || red != null && red.get(0).equals(true);
  }
}
