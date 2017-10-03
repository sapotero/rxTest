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

public class SaveDecision extends DecisionCommand {

  public SaveDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    setRedLabel();

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    setChangedInDb();

    updateInMemory();
    updateInDb();

    setSyncLabelInMemory();

    queueManager.add(this);
    setAsProcessed();
  }

  @Override
  public String getType() {
    return "save_decision";
  }

  @Override
  public void executeLocal() {
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Decision _decision = getParams().getDecisionModel();
    _decision.setDocumentUid( null );

    Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision);
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
