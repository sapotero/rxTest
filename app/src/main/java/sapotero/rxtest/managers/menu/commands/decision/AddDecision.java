package sapotero.rxtest.managers.menu.commands.decision;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
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
    CommandFactory.Operation operation = CommandFactory.Operation.CREATE_TEMPORARY_DECISION;
    CommandParams _params = new CommandParams();
    _params.setDecisionId( getParams().getDecisionModel().getId() );
    _params.setDecisionModel( getParams().getDecisionModel() );
    _params.setDocument( getParams().getDocument() );
    _params.setAssignment( getParams().isAssignment() );
    Command command = operation.getCommand(null, _params);
    command.execute();

    setSyncLabelInMemory();

    Timber.tag(TAG).w("ASSIGNMENT: %s", getParams().isAssignment() );

    queueManager.add(this);
    setAsProcessed();
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
    finishOperationWithoutProcessedOnError( errors );
    EventBus.getDefault().post( new ForceUpdateDocumentEvent( getParams().getDocument() ));
  }
}
