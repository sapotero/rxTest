package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
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

public class ApproveDecision extends DecisionCommand {

  public ApproveDecision(CommandParams params) {
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
    Timber.tag(TAG).e("1 updateLocal params%s", new Gson().toJson( getParams() ));

    setDecisionTemporary();

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    setChangedInDb();

    InMemoryDocument doc = store.getDocuments().get(getParams().getDocument());

    if (doc != null) {
      Timber.tag(TAG).d("++++++doc index: %s | status: %s", doc.getIndex(), doc.getFilter());
    }

    if (
      // resolved https://tasks.n-core.ru/browse/MPSED-2207
      // Новое подписание резолюций и переход документов в обработанные
      signerIsCurrentUser() && (doc != null && Objects.equals(doc.getFilter(), JournalStatus.FOR_REPORT.getName()))
      || isActiveOrRed() && (doc != null && Objects.equals(doc.getFilter(), JournalStatus.PRIMARY.getName()))

      ){
      startProcessedOperationInMemory();
      startProcessedOperationInDb();

    } else {
      setSyncLabelInMemory();
    }

    sendInvalidateDecisionEvent();
  }

  @Override
  public String getType() {
    return "approve_decision";
  }

  @Override
  public void executeLocal() {
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    String sign = getSign();

    if ( sign != null ) {
      Decision _decision = getParams().getDecisionModel();
      _decision.setDocumentUid( null );
      _decision.setApproved(true);

      // resolved https://tasks.n-core.ru/browse/MVDESD-14141
      // при нажатии кнопки согласовать - не отправляем подпись
      Boolean equals = Objects.equals(store.getDocuments().get(params.getDocument()).getFilter(), JournalStatus.PRIMARY.getName()) && !Objects.equals(getParams().getDecisionModel().getSignerId(), settings.getCurrentUserId());
      _decision.setSign( equals? null : sign );

      if ( getParams().isAssignment() ) {
        _decision.setAssignment(true);
      }

      Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision);
      sendDecisionOperationRequest( info );

    } else {
      sendErrorCallback( SIGN_ERROR_MESSAGE );
      finishOnOperationError( Collections.singletonList( SIGN_ERROR_MESSAGE ) );
    }
  }

  private void sendInvalidateDecisionEvent() {
    Observable.just("").timeout(100, TimeUnit.MILLISECONDS).subscribe(
      data -> {
        Timber.tag("slow").e("exec");
        EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( getParams().getDecisionModel().getId() ));
      },
      error -> Timber.tag(TAG).e(error)
    );
  }

  @Override
  public void finishOnDecisionSuccess(DecisionError data) {
    if ( isActiveOrRed() ) {
      finishProcessedOperationOnSuccess();
    } else {
      finishOperationOnSuccess();
    }

    Transaction transaction = new Transaction();
    transaction
      .from( store.getDocuments().get(getParams().getDocument()) )
      .setField(FieldType.UPDATED_AT, DateUtil.getTimestamp())
      .removeLabel(LabelType.SYNC);
    store.process( transaction );

    EventBus.getDefault().post( new UpdateDocumentEvent( data.getDocumentUid() ));
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishRejectedProcessedOperationOnError( errors );
  }
}
