package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import java.util.Objects;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class RejectDecision extends DecisionCommand {

  private String TAG = this.getClass().getSimpleName();

  public RejectDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    updateLocal();
    setDocOperationStartedInMemory();
    setAsProcessed();
  }

  private void updateLocal() {
    Timber.tag(TAG).e("1 updateLocal params%s", new Gson().toJson( getParams() ));

    dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.CHANGED, true)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq( getParams().getDecisionId() ))
      .get().value();

    if (Objects.equals(getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId())){

      String uid = getParams().getDocument();

      Timber.tag(TAG).i( "3 updateLocal document uid:\n%s\n%s\n", getParams().getDecisionModel().getDocumentUid(), getParams().getDocument() );

      Integer dec = dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
        .get().value();

      Timber.tag(TAG).e("3 updateLocal document %s | %s", uid, dec > 0);

      store.process(
        store.startTransactionFor( uid )
          .setLabel(LabelType.SYNC)
          .setField(FieldType.PROCESSED, true)
      );
    }
  }

  @Override
  public String getType() {
    return "reject_decision";
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

    Decision formated_decision;

    formated_decision = getParams().getDecisionModel();

    formated_decision.setApproved(false);
    formated_decision.setCanceled(true);
    formated_decision.setDocumentUid(null);

    if (getParams().getComment() != null){
      formated_decision.setComment( String.format( "Причина отклонения: %s", getParams().getComment() ) );
    }

    Observable<DecisionError> info = getDecisionUpdateOperationObservable(formated_decision, TAG);

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          onSuccess( this, data, true, false, TAG );
          finishOperationOnSuccess();
        },
        error -> onError( this, error.getLocalizedMessage(), false, TAG )
      );
  }
}
