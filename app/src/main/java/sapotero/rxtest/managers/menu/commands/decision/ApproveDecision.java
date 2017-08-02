package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.requery.query.Tuple;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class ApproveDecision extends DecisionCommand {

  private String TAG = this.getClass().getSimpleName();

  public ApproveDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    updateLocal();
    queueManager.add(this);
    setDocOperationStartedInMemory();
  }

  private void updateLocal() {
    Timber.tag(TAG).e("1 updateLocal params%s", new Gson().toJson( getParams() ));

    dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq(getParams().getDecisionModel().getId()))
      .get().value();

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();

    Tuple red = dataStore
      .select(RDecisionEntity.RED)
      .where(RDecisionEntity.UID.eq(getParams().getDecisionModel().getId()))
      .get().firstOrNull();

    if (
        // если активная резолюция
        Objects.equals(getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId())

        // или если подписывающий министр
        || ( red != null && red.get(0).equals(true) )
      ) {

      String uid = getParams().getDocument();

      store.process(
        store.startTransactionFor( getParams().getDocument() )
          .setLabel(LabelType.SYNC)
          .setField(FieldType.PROCESSED, true)
      );

      Timber.tag(TAG).i( "3 updateLocal document uid:\n%s\n%s\n", getParams().getDecisionModel().getDocumentUid(), getParams().getDocument() );

      Integer dec = dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
        .get().value();

      Timber.tag(TAG).e("3 updateLocal document %s | %s", uid, dec > 0);
    }

    Observable.just("").timeout(100, TimeUnit.MILLISECONDS).subscribe(
      data -> {
        Timber.tag("slow").e("exec");
        EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( getParams().getDecisionModel().getId() ));
      }, error -> {
        Timber.tag(TAG).e(error);
      }
    );
  }

  @Override
  public String getType() {
    return "approve_decision";
  }

  @Override
  public void executeLocal() {
    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
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

      Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision, TAG);

      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            onSuccess( this, data, true, false, TAG );
            finishOperationOnSuccess();
          },
          error -> onError( this, error.getLocalizedMessage(), true, TAG )
        );

    } else {
      onError( this, SIGN_ERROR_MESSAGE, true, TAG );
    }
  }
}
