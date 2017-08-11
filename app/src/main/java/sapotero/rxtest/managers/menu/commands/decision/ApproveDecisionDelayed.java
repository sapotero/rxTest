package sapotero.rxtest.managers.menu.commands.decision;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import timber.log.Timber;

public class ApproveDecisionDelayed extends DecisionCommand {

  public ApproveDecisionDelayed(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    setAsProcessed();
  }

  public void update() {
    sendSuccessCallback();

    try {
      dataStore
        .update(RDecisionEntity.class)
        .set( RDecisionEntity.APPROVED, true)
        .where(RDecisionEntity.UID.eq( getParams().getDecisionId() )).get().call();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getType() {
    return "approve_decision_delayed";
  }

  @Override
  public void executeLocal() {
    update();

    sendSuccessCallback();

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    String sign = getSign();

    RDecisionEntity decision= getDecision(getParams().getDecisionId());
    if ( sign != null && decision != null) {

      Decision _decision = mappers.getDecisionMapper().toFormattedModel(decision);
      _decision.setDocumentUid( null );
      _decision.setDocumentUid( null );
      _decision.setApproved(true);
      _decision.setSign( sign );

      if (getParams().isAssignment()){
        _decision.setAssignment(true);
      }

      Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision);
      sendDecisionOperationRequest( info );

    } else {
      Timber.tag(TAG).i("error: no decision yet");
      sendErrorCallback( getType() );
    }
  }

  private RDecisionEntity getDecision(String uid){
    return dataStore.select(RDecisionEntity.class).where(RDecisionEntity.UID.eq(uid)).get().firstOrNull();
  }

  @Override
  public void finishOnDecisionSuccess(DecisionError data) {
    finishOperationWithoutProcessedOnSuccess();
    EventBus.getDefault().post( new UpdateDocumentEvent( data.getDocumentUid() ));
  }

  @Override
  public void finishOnDecisionError(List<String> errors) {
    finishOperationWithoutProcessedOnError( errors );
    EventBus.getDefault().post( new ForceUpdateDocumentEvent( getParams().getDocument() ));
  }
}
