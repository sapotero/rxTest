package sapotero.rxtest.managers.menu.commands.decision;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import timber.log.Timber;

public class ApproveDecisionDelayed extends DecisionCommand {

  private String TAG = this.getClass().getSimpleName();

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
    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

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

    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

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

      Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision, TAG);

      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            onSuccess( this, data, true, false, TAG );
          },
          error -> {
            Timber.tag(TAG).i("error: %s", error);
            if (callback != null){
              callback.onCommandExecuteError(getType());
            }
//            queueManager.setExecutedWithError(this, Collections.singletonList("http_error"));
            EventBus.getDefault().post( new ForceUpdateDocumentEvent( getParams().getDocument() ));
          }
        );
    } else {
      Timber.tag(TAG).i("error: no decision yet");
      if (callback != null){
        callback.onCommandExecuteError(getType());
      }
    }
  }

  private RDecisionEntity getDecision(String uid){
    return dataStore.select(RDecisionEntity.class).where(RDecisionEntity.UID.eq(uid)).get().firstOrNull();
  }
}
