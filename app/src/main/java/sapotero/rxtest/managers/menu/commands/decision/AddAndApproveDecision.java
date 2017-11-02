package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import rx.Observable;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class AddAndApproveDecision extends DecisionCommand {

  public AddAndApproveDecision(CommandParams params) {
    super(params);
  }

  private void updateLocal() {
    Timber.tag(TAG).e("updateLocal %s", new Gson().toJson( getParams() ));

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    setChangedInDb();

    // всегда перемещаем в обработанные при создании и подписании резолюции
    if ( isActiveOrRed() ) {
      startProcessedOperationInMemory();
      startProcessedOperationInDb();

      EventBus.getDefault().post( new ShowNextDocumentEvent( getParams().getDocument() ) );
    }

//    EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( getParams().getDecisionModel().getId() ));
  }

  @Override
  public String getType() {
    return "add_and_approve_decision";
  }

  @Override
  public void executeLocal() {
    setRemoveRedLabel();

    getParams().getDecisionModel().setApproved( true );
    createTemporaryDecision();

    saveOldLabelValues(); // Must be before queueManager.add(this), because old label values are stored in params
    addToQueue();
    updateLocal();
    setAsProcessed();

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

    String sign = getSign(null);

    if ( sign != null ) {
      // resolved https://tasks.n-core.ru/browse/MPSED-1965
      // если при создании нового проекта резолюции в редакторе нажать "сохранить и подписать", то такая резолюция подписывается, а должна согласовываться и штамп подписи не проставляться
      InMemoryDocument document = store.getDocuments().get(getParams().getDocument());
      Boolean equals = document != null && Objects.equals(document.getFilter(), JournalStatus.PRIMARY.getName()) && !Objects.equals(getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId());
      decision.setSign( equals ? null : sign );

      Observable<DecisionError> info = getDecisionCreateOperationObservable(decision);
      sendDecisionOperationRequest( info );

    } else {
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
  }
}
