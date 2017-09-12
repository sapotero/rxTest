package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import rx.Observable;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class SaveAndApproveDecision extends DecisionCommand {

  public SaveAndApproveDecision(CommandParams params) {
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
  }

  @Override
  public String getType() {
    return "save_and_approve_decision";
  }

  @Override
  public void executeLocal() {
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  private void updateLocal() {
    Timber.tag(TAG).e("updateLocal %s", new Gson().toJson( getParams() ));

    setDecisionTemporary();

    setChangedInDb();
    InMemoryDocument doc = store.getDocuments().get(getParams().getDocument());

    if (doc != null) {
      Timber.tag(TAG).d("++++++doc index: %s | status: %s", doc.getIndex(), doc.getFilter());
    }

    
    if ( isActiveOrRed() || (doc != null && Objects.equals(doc.getFilter(), "primary_consideration")) ) {
      startProcessedOperationInMemory();
      startProcessedOperationInDb();
      EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ));

    } else {
      setSyncLabelInMemory();
    }

    EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( getParams().getDecisionModel().getId() ));
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Decision _decision = getParams().getDecisionModel();
    _decision.setDocumentUid( null );
    _decision.setApproved(true);

    String sign = getSign();

    if ( sign != null ) {
//      _decision.setSign( sign );

      // resolved https://tasks.n-core.ru/browse/MVDESD-14141
      // при нажатии кнопки согласовать - не отправляем подпись
      Boolean equals = Objects.equals(store.getDocuments().get(params.getDocument()).getFilter(), "primary_consideration") && !Objects.equals(getParams().getDecisionModel().getSignerId(), settings.getCurrentUserId());
      _decision.setSign( equals? null : sign );

      Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision);
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
