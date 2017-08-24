package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import timber.log.Timber;

public class AddAndApproveDecision extends DecisionCommand {

  public AddAndApproveDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    queueManager.add(this);
    updateLocal();
    setAsProcessed();

    EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ) );
  }

  private void updateLocal() {
    Timber.tag(TAG).e("updateLocal %s", new Gson().toJson( getParams() ));

    setDecisionTemporary();

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    setChangedInDb();

    // всегда перемещаем в обработанные при создании и подписании резолюции
//    if ( isActiveOrRed() ) {
    startProcessedOperationInMemory();
    startProcessedOperationInDb();
//    }

//    EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( getParams().getDecisionModel().getId() ));
  }

  @Override
  public String getType() {
    return "add_and_approve_decision";
  }

  @Override
  public void executeLocal() {
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    queueManager.setAsRunning(this);

    Timber.tag(TAG).i( "type: %s", new Gson().toJson(getParams()) );

    Decision decision = getParams().getDecisionModel();
    decision.setLetterhead(null);

    if (getParams().isAssignment()){
      decision.setAssignment(true);
    }

    decision.setApproved(true);

    String sign = getSign();

    if ( sign != null ) {
      decision.setSign(sign);
      Observable<DecisionError> info = getDecisionCreateOperationObservable(decision);
      sendDecisionOperationRequest( info );

    } else {
      sendErrorCallback( SIGN_ERROR_MESSAGE );
      finishOnOperationError( Collections.singletonList( SIGN_ERROR_MESSAGE ) );
    }
  }

  @Override
  public void finishOnDecisionSuccess(DecisionError data) {
    if ( isActiveOrRed() ) {
      finishProcessedOperationOnSuccess();
    } else {
      finishOperationOnSuccess();
    }

    checkCreatorAndSignerIsCurrentUser(data);
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
    EventBus.getDefault().post( new ForceUpdateDocumentEvent( getParams().getDocument() ));
  }
}
