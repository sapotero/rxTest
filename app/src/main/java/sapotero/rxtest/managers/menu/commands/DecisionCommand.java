package sapotero.rxtest.managers.menu.commands;

import com.google.gson.Gson;

import java.util.List;
import java.util.Objects;

import io.requery.query.Tuple;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RManagerEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RDisplayFirstDecisionEntity;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.retrofit.models.wrapper.DecisionWrapper;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Transaction;
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

  protected void sendDecisionOperationRequest(Observable<DecisionError> info) {
    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        this::onDecisionSuccess,
        this::onOperationError
      );
  }

  private void onDecisionSuccess(DecisionError data) {
    if ( notEmpty( data.getErrors() ) ) {
      sendErrorCallback( "error" );
      finishOnOperationError( data.getErrors() );

      Transaction transaction = new Transaction();
      transaction
        .from( store.getDocuments().get(getParams().getDocument()) )
        .setField(FieldType.UPDATED_AT, DateUtil.getTimestamp())
        .removeLabel(LabelType.SYNC);
      store.process( transaction );

    } else {
      finishOnDecisionSuccess( data );
    }
  }

  public abstract void finishOnDecisionSuccess(DecisionError data);

  protected boolean signerIsCurrentUser() {
    return Objects.equals( getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId() );
  }

  protected void setDecisionChanged() {
    Integer count = dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.CHANGED, true)
      .where(RDecisionEntity.UID.eq( getParams().getDecisionModel().getId() ))
      .get().value();

    Timber.tag(TAG).i( "updateLocal: %s", count );
  }

  protected boolean isActiveOrRed() {
    Tuple red = dataStore
      .select(RDecisionEntity.RED)
      .where(RDecisionEntity.UID.eq(getParams().getDecisionModel().getId()))
      .get().firstOrNull();

    Tuple manager = dataStore
      .select(RManagerEntity.UID)
      .where(RManagerEntity.USER.eq(settings.getLogin()))
      .get().firstOrNull();



    return
      // если активная резолюция
      signerIsCurrentUser()

      // или подписант министр
      || manager != null && manager.get(0).equals( getParams().getDecisionModel().getSignerId() )

      // или если подписывающий министр
      || red != null && red.get(0).equals(true);
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13258
  // 1. Созданные мной и подписант я
  protected void checkCreatorAndSignerIsCurrentUser(DecisionError data) {
    String decisionUid = data.getDecisionUid();

    // Если создал резолюцию я и подписант я, то сохранить UID этой резолюции в отдельную таблицу
    if ( decisionUid != null && !decisionUid.equals("") ) {
      if ( Objects.equals( data.getDecisionSignerId(), getParams().getCurrentUserId() ) ) {
        RDisplayFirstDecisionEntity rDisplayFirstDecisionEntity = new RDisplayFirstDecisionEntity();
        rDisplayFirstDecisionEntity.setDecisionUid( decisionUid );
        rDisplayFirstDecisionEntity.setUserId( getParams().getCurrentUserId() );

        dataStore
          .insert( rDisplayFirstDecisionEntity )
          .toObservable()
          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            result -> Timber.tag(TAG).v("Added decision to display first decision table"),
            error -> Timber.tag(TAG).e(error)
          );
      }
    }
  }

  protected void createTemporaryDecision() {
    CommandFactory.Operation operation = CommandFactory.Operation.CREATE_TEMPORARY_DECISION;
    CommandParams _params = new CommandParams();
    _params.setDecisionId( getParams().getDecisionModel().getId() );
    _params.setDecisionModel( getParams().getDecisionModel() );
    _params.setDocument( getParams().getDocument() );
    _params.setAssignment( getParams().isAssignment() );
    Command command = operation.getCommand(null, _params);
    command.execute();
  }

  protected void updateInMemory() {
    Decision dec = getParams().getDecisionModel();

    if ( dec != null ) {
      dec.setChanged( true );
      InMemoryDocument inMemoryDocument = store.getDocuments().get( getParams().getDocument() );

      if ( inMemoryDocument != null && inMemoryDocument.getDecisions() != null ) {
        List<Decision> inMemoryDecisions = inMemoryDocument.getDecisions();

        if ( inMemoryDecisions.size() > 0 ) {
          for ( int i = 0; i < inMemoryDecisions.size(); i++ ) {
            Decision inMemoryDecision = inMemoryDecisions.get(i);

            if ( Objects.equals( inMemoryDecision.getId(), dec.getId() ) ) {
              inMemoryDecisions.set(i, dec);
              break;
            }
          }
        } else {
          inMemoryDecisions.add( dec );
        }
      }
    }
  }
}
