package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.requery.query.Tuple;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
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

    if ( isActiveOrRed() ) {
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
      _decision.setSign( sign );

      if ( getParams().isAssignment() ) {
        _decision.setAssignment(true);
      }

      Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision);

      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            if ( notEmpty( data.getErrors() ) ) {
              sendErrorCallback( "error" );
              finishOnError( data.getErrors() );

            } else {
              if ( isActiveOrRed() ) {
                finishProcessedOperationOnSuccess();
              } else {
                removeSyncChanged();
                queueManager.setExecutedRemote(this);
              }

              EventBus.getDefault().post( new UpdateDocumentEvent( data.getDocumentUid() ));
            }
          },

          error -> onDecisionError( error.getLocalizedMessage() )
        );

    } else {
      sendErrorCallback( SIGN_ERROR_MESSAGE );
      finishOnError( Collections.singletonList( SIGN_ERROR_MESSAGE ) );
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
  public void finishOnError(List<String> errors) {
    if ( isActiveOrRed() ) {
      finishRejectedProcessedOperationOnError( errors );
    } else {
      finishOperationWithoutProcessedOnError( errors );
    }

    EventBus.getDefault().post( new ForceUpdateDocumentEvent( getParams().getDocument() ));
  }
}
