package sapotero.rxtest.managers.menu.commands.decision;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import rx.Observable;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RManagerEntity;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class AddDecision extends DecisionCommand {

  public AddDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    setRedLabel();

    getParams().getDecisionModel().setApproved( false );
    createTemporaryDecision();

    setSyncLabelInMemory();

    Timber.tag(TAG).w("ASSIGNMENT: %s", getParams().isAssignment() );

    queueManager.add(this);
    setAsProcessed();
  }

  // resolved https://tasks.n-core.ru/browse/MPSED-2206
  // Проставлять признак red у документа, при создании/подписании резолюции
  private boolean setRedLabel() {
    boolean result = false;

    int count = dataStore
      .count( RManagerEntity.class )
      .where( RManagerEntity.USER.eq( getParams().getLogin() ) )
      .and( RManagerEntity.UID.eq( getParams().getDecisionModel().getSignerId() ) )
      .get().value();

    // Если подписант министр и подписант не равен текущему пользователю (т.е. текущий пользователь не министр)
    if ( count > 0 && !Objects.equals( getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId() ) ) {
      getParams().getDecisionModel().setRed( true );

      InMemoryDocument inMemoryDocument = store.getDocuments().get( getParams().getDocument() );
      if ( inMemoryDocument != null ) {
        inMemoryDocument.getDocument().setRed( true );
      }

      dataStore
        .update( RDocumentEntity.class )
        .set( RDocumentEntity.RED, true )
        .where( RDocumentEntity.UID.eq( getParams().getDocument() ) )
        .get().value();

      result = true;
    }

    return result;
  }

  @Override
  public String getType() {
    return "add_decision";
  }

  @Override
  public void executeLocal() {
    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    setChangedInDb();

    sendSuccessCallback();

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Decision decision = getParams().getDecisionModel();
    decision.setLetterhead(null);
    decision.setShowPosition( false );

    if ( getParams().isAssignment() ) {
      decision.setAssignment(true);
    }

    Observable<DecisionError> info = getDecisionCreateOperationObservable(decision);
    sendDecisionOperationRequest( info );
  }

  @Override
  public void finishOnDecisionSuccess(DecisionError data) {
    finishOperationOnSuccess();
    checkCreatorAndSignerIsCurrentUser(data);
    EventBus.getDefault().post( new UpdateDocumentEvent( data.getDocumentUid() ));
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishOperationOnError( errors );
  }
}
