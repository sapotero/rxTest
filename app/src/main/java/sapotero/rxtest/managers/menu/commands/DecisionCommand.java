package sapotero.rxtest.managers.menu.commands;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.requery.query.Tuple;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RManagerEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RDisplayFirstDecisionEntity;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Block;
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

//    Timber.tag(TAG).e("DECISION");
//    Timber.tag(TAG).e("%s", json_m);

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
//    Timber.w("decision_json: %s", json_d);

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

  protected Observable<DecisionError> getDecisionCreateOrUpdateOperationObservable(Decision decision) {
    Observable<DecisionError> result;

    // If decision is temporary, then this operation canceled previously added AddDecision operation, and we must send CREATE decision request.
    if ( getParams().isTemporaryDecision() ) {
      decision.setDocumentUid( getParams().getDocument() ); // in this case we must send document UID
      result = getDecisionCreateOperationObservable(decision);
    } else {
      // Otherwise decision already exists on the server and we just have to update it.
      decision.setDocumentUid( null ); // in this case we don't send document UID
      result = getDecisionUpdateOperationObservable(decision);
    }

    return result;
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
    InMemoryDocument inMemoryDocument = store.getDocuments().get( getParams().getDocument() );
    boolean red = isRed( inMemoryDocument );

    Tuple manager = dataStore
      .select(RManagerEntity.UID)
      .where(RManagerEntity.USER.eq(getParams().getLogin()))
      .get().firstOrNull();

    return
      // если активная резолюция
      signerIsCurrentUser()

      // или подписант министр
      || manager != null && manager.get(0).equals( getParams().getDecisionModel().getSignerId() )

      // или если подписывающий министр
      || red;
  }

  private boolean isRed( InMemoryDocument inMemoryDocument ) {
    boolean red = false;

    if ( inMemoryDocument != null && inMemoryDocument.getDecisions() != null ) {
      for ( Decision decision : inMemoryDocument.getDecisions() ) {
        if ( decision.getRed() != null && decision.getRed() && Objects.equals( decision.getId(), getParams().getDecisionModel().getId() ) ) {
          red = true;
          break;
        }
      }
    }

    return red;
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13258
  // 1. Созданные мной и подписант я
  protected void checkCreatorAndSignerIsCurrentUser(DecisionError data) {
    String decisionUid = data.getDecisionUid();

    if ( decisionUid != null && !decisionUid.equals("") ) {
      // Если подписант является текущим пользователем
      // и создал резолюцию текущий пользователь (так как операция выполняется в данном мобильном приложении)
      if ( Objects.equals( data.getDecisionSignerId(), getParams().getCurrentUserId() ) ) {
        RDisplayFirstDecisionEntity rDisplayFirstDecisionEntity = dataStore
          .select(RDisplayFirstDecisionEntity.class)
          .where(RDisplayFirstDecisionEntity.DECISION_UID.eq( decisionUid ))
          .and(RDisplayFirstDecisionEntity.USER_ID.eq( getParams().getCurrentUserId() ))
          .get().firstOrNull();

        // И если UID резолюции нет в таблице резолюций, отображаемых первыми,
        // то сохранить UID этой резолюции в таблицу
        if ( rDisplayFirstDecisionEntity == null ) {
          rDisplayFirstDecisionEntity = new RDisplayFirstDecisionEntity();
          rDisplayFirstDecisionEntity.setDecisionUid( decisionUid );
          rDisplayFirstDecisionEntity.setUserId( getParams().getCurrentUserId() );

          dataStore
            .insert( rDisplayFirstDecisionEntity )
            .toObservable()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
              result -> Timber.tag(TAG).v("Added decision %s to display first decision table", decisionUid),
              error -> Timber.tag(TAG).e(error)
            );
        }
      }
    }
  }

  protected void createTemporaryDecision() {
    generateTemporaryDecisionId();

    CommandFactory.Operation operation = CommandFactory.Operation.CREATE_TEMPORARY_DECISION;
    CommandParams _params = new CommandParams();
    _params.setDecisionId( getParams().getDecisionModel().getId() );
    _params.setDecisionModel( getParams().getDecisionModel() );
    _params.setDocument( getParams().getDocument() );
    _params.setAssignment( getParams().isAssignment() );
    Command command = operation.getCommand(null, _params);
    command.execute();
  }

  private void generateTemporaryDecisionId() {
    // Generated temporary decision ID must be stored in CommandParams, so that it would be possible
    // to cancel this operation in case SaveDecision or SaveAndApproveDecision is added (in offline) for this decision
    String tempDecisionId = UUID.randomUUID().toString();
    getParams().setDecisionId( tempDecisionId );
    getParams().getDecisionModel().setId( tempDecisionId );
  }

  protected void updateInMemory() {
    Decision dec = getParams().getDecisionModel();

    if ( dec != null ) {
      dec.setChanged( true );
      InMemoryDocument inMemoryDocument = store.getDocuments().get( getParams().getDocument() );

      if ( inMemoryDocument != null && inMemoryDocument.getDecisions() != null ) {
        boolean decisionAdded = false;
        List<Decision> inMemoryDecisions = inMemoryDocument.getDecisions();

        // If decision already exists, replace it
        for ( int i = 0; i < inMemoryDecisions.size(); i++ ) {
          Decision inMemoryDecision = inMemoryDecisions.get(i);

          if ( Objects.equals( inMemoryDecision.getId(), dec.getId() ) ) {
            inMemoryDecisions.set(i, dec);
            decisionAdded = true;
            break;
          }
        }

        // If decision doesn't exist, add it
        if ( !decisionAdded ) {
          inMemoryDecisions.add( dec );
        }
      }
    }
  }

  protected void updateInDb() {
    Decision dec = getParams().getDecisionModel();
//    Timber.tag(TAG).e("UPDATE %s", new Gson().toJson(dec));

    RDecisionEntity decision = dataStore
      .select(RDecisionEntity.class)
      .where(RDecisionEntity.UID.eq(dec.getId()))
      .get().firstOrNull();

    if (dec.getUrgencyText() != null) {
      decision.setUrgencyText(dec.getUrgencyText());
    }

    decision.setComment(dec.getComment());
    decision.setDate( dec.getDate());
    decision.setSigner( dec.getSigner() );
    decision.setSignerBlankText(dec.getSignerBlankText());
    decision.setSignerId(dec.getSignerId());
    decision.setSignerPositionS(dec.getSignerPositionS());
    decision.setApproved(dec.getApproved());
    decision.setChanged(true);
    decision.setRed(dec.getRed());

    if (dec.getBlocks().size() > 0) {
      decision.getBlocks().clear();
    }

    BlockMapper blockMapper = new BlockMapper();

    for (Block _block : dec.getBlocks()) {
      RBlockEntity block = blockMapper.toEntity(_block);
      block.setDecision(decision);
      decision.getBlocks().add(block);
    }

    dataStore
      .update(decision)
      .toObservable()
      .observeOn(Schedulers.io())
      .subscribeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.tag(TAG).e("UPDATED %s", data.getSigner() );
          EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( data.getUid() ));
        },
        error -> queueManager.setExecutedWithError(this, Collections.singletonList("db_error"))
      );

    Timber.tag(TAG).e("1 updateFromJob params%s", new Gson().toJson( params ));
  }

  // resolved https://tasks.n-core.ru/browse/MPSED-2206
  // Проставлять признак red у документа, при создании/подписании резолюции
  protected void setRemoveRedLabel() {
    int count = dataStore
      .count( RManagerEntity.class )
      .where( RManagerEntity.USER.eq( getParams().getLogin() ) )
      .and( RManagerEntity.UID.eq( getParams().getDecisionModel().getSignerId() ) )
      .get().value();

    InMemoryDocument inMemoryDocument = store.getDocuments().get( getParams().getDocument() );

    if ( count > 0 && !Objects.equals( getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId() ) ) {
      // Если подписант министр и подписант не равен текущему пользователю (т.е. текущий пользователь не министр),
      // то ставим red у документа и резолюции
      setRed( inMemoryDocument, true );

    } else {
      // Иначе просматриваем все резолюции документа, кроме текущей и, если ни одна из них не red,
      // то снимаем red у документа и резолюции
      if ( inMemoryDocument != null && inMemoryDocument.getDecisions() != null ) {
        boolean red = false;

        for ( Decision decision : inMemoryDocument.getDecisions() ) {
          if ( decision.getRed() != null && decision.getRed() && !Objects.equals( decision.getId(), getParams().getDecisionModel().getId() ) ) {
            red = true;
            break;
          }
        }

        if ( !red ) {
          setRed( inMemoryDocument, false );
        }
      }
    }
  }

  private void setRed(InMemoryDocument inMemoryDocument, boolean value) {
    getParams().getDecisionModel().setRed( value );

    if ( inMemoryDocument != null ) {
      inMemoryDocument.getDocument().setRed( value );
    }

    dataStore
      .update( RDecisionEntity.class )
      .set( RDecisionEntity.RED, value )
      .where( RDecisionEntity.UID.eq( getParams().getDecisionModel().getId() ) )
      .get().value();

    dataStore
      .update( RDocumentEntity.class )
      .set( RDocumentEntity.RED, value )
      .where( RDocumentEntity.UID.eq( getParams().getDocument() ) )
      .get().value();
  }
}
