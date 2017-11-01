package sapotero.rxtest.managers.menu.commands.decision;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import timber.log.Timber;

public class AddDecision extends DecisionCommand {

  public AddDecision(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "add_decision";
  }

  @Override
  public void executeLocal() {
    setRemoveRedLabel();

    getParams().getDecisionModel().setApproved( false );
    createTemporaryDecision();

    setSyncLabelInMemory();

    Timber.tag(TAG).w("ASSIGNMENT: %s", getParams().isAssignment() );

    queueManager.add(this);
    setAsProcessed();

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    setChangedInDb();

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
