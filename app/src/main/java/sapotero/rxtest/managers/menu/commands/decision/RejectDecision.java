package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import rx.Observable;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class RejectDecision extends DecisionCommand {

  public RejectDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  private void updateLocal() {
    Timber.tag(TAG).e("1 updateLocal params%s", new Gson().toJson(getParams()));

    getParams().getDecisionModel().setApproved( false );
    getParams().getDecisionModel().setCanceled( true );
    updateInMemory();

    setDecisionChanged();

    InMemoryDocument doc = store.getDocuments().get(getParams().getDocument());

    if (doc != null) {
      Timber.tag(TAG).d("++++++doc index: %s | status: %s", doc.getIndex(), doc.getFilter());
    }

    if ( signerIsCurrentUser() || (doc != null && Objects.equals(doc.getFilter(), JournalStatus.PRIMARY.getName()))) {
      startRejectedOperationInMemory();
      startRejectedOperationInDb();
    } else {
      setSyncLabelInMemory();
      setChangedInDb();
    }
  }

  @Override
  public String getType() {
    return "reject_decision";
  }

  @Override
  public void executeLocal() {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    queueManager.add(this);
    updateLocal();
    setAsProcessed();

    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Decision formated_decision;

    formated_decision = getParams().getDecisionModel();

    formated_decision.setApproved(false);
    formated_decision.setCanceled(true);
    formated_decision.setDocumentUid(null);

    if (getParams().getComment() != null){
      formated_decision.setComment( String.format( "Причина отклонения: %s", getParams().getComment() ) );
    }

    Observable<DecisionError> info = getDecisionUpdateOperationObservable(formated_decision);
    sendDecisionOperationRequest( info );
  }

  @Override
  public void finishOnDecisionSuccess(DecisionError data) {
    if ( signerIsCurrentUser() ) {
      finishRejectedOperationOnSuccess();
    } else {
      finishOperationOnSuccess();
    }

    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .setField(FieldType.UPDATED_AT, DateUtil.getTimestamp())
      .removeLabel(LabelType.SYNC);
    store.process( transaction );

    EventBus.getDefault().post( new UpdateDocumentEvent( getParams().getDocument() ));
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
  }
}
