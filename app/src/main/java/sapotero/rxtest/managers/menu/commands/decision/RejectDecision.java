package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import rx.Observable;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.V2DocumentType;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
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

  @Override
  public void execute() {
    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    queueManager.add(this);
    updateLocal();
    setAsProcessed();
  }

  private void updateLocal() {
    Timber.tag(TAG).e("1 updateLocal params%s", new Gson().toJson(getParams()));

    setDecisionChangedTemporary();

    dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.CHANGED, true)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq(getParams().getDecisionId()))
      .get().value();

    InMemoryDocument doc = store.getDocuments().get(getParams().getDocument());

    if (doc != null) {
      Timber.tag(TAG).d("++++++doc index: %s | status: %s", doc.getIndex(), doc.getFilter());
    }

    if ( signerIsCurrentUser() || (doc != null && Objects.equals(doc.getFilter(), V2DocumentType.PRIMARY.getName()))) {
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

  private void setDecisionChangedTemporary() {
    dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.CHANGED, true)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq( getParams().getDecisionId() ))
      .get().value();
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
    EventBus.getDefault().post( new ForceUpdateDocumentEvent( getParams().getDocument() ));
  }
}
