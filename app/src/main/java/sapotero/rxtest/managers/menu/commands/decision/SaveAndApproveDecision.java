package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import io.requery.query.Tuple;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
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
    queueManager.add(this);
    updateLocal();

    setSyncLabelInMemory();
    setAsProcessed();
  }

  @Override
  public String getType() {
    return "save_and_approve_decision";
  }

  @Override
  public void executeLocal() {
    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    queueManager.setExecutedLocal(this);
  }

  private void updateLocal() {
    Timber.tag(TAG).e("updateLocal %s", new Gson().toJson( getParams() ));

    Integer count = dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq( getParams().getDecisionModel().getId() ))
      .get().value();

    Timber.tag(TAG).i( "updateLocal: %s", count );

    Tuple red = dataStore
      .select(RDecisionEntity.RED)
      .where(RDecisionEntity.UID.eq(getParams().getDecisionModel().getId()))
      .get().firstOrNull();

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();

    Timber.tag(TAG).e("-------- %s %s", getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId());

    if (
      Objects.equals(getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId())
        // или если подписывающий министр
        || ( red != null && red.get(0).equals(true) )
      ) {
      dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .where(RDocumentEntity.UID.eq(  getParams().getDocument() ))
        .get()
        .value();

      store.process(
        store.startTransactionFor(  getParams().getDocument() )
          .setLabel(LabelType.SYNC)
          .setField(FieldType.PROCESSED, true)
      );

      EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ));
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
      _decision.setSign( sign );

      Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision);

      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            onSuccess( data, false, true );
            finishOperationOnSuccess();
          },
          error -> onError( error.getLocalizedMessage(), true )
        );

    } else {
      onError( SIGN_ERROR_MESSAGE, true );
    }
  }
}
