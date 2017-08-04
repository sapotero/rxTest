package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import io.requery.query.Tuple;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class AddAndApproveDecision extends DecisionCommand {

  private String TAG = this.getClass().getSimpleName();

  public AddAndApproveDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    updateLocal();
    EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ) );

    queueManager.add(this);
    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .setLabel(LabelType.SYNC)
        .setState(InMemoryState.LOADING)
    );
    setAsProcessed();
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
      .where(RDecisionEntity.UID.eq( getParams().getDecisionModel().getId() ))
      .get().firstOrNull();

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();

    // всегда перемещаем в обработанные при создании и подписании резолюции
//    if (
//
//      Objects.equals(getParams().getDecisionModel().getSignerId(), getParams().getCurrentUserId())
//      // или если подписывающий министр
//      || ( red != null && red.get(0).equals(true) )
//      ){
      Integer dec = dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
        .get()
        .value();

      store.process(
        store.startTransactionFor( getParams().getDocument() )
          .setField(FieldType.PROCESSED, true)
      );
//    }

    EventBus.getDefault().post( new ShowNextDocumentEvent( true, getParams().getDocument() ));
//    EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( getParams().getDecisionModel().getId() ));
  }

  @Override
  public String getType() {
    return "add_and_approve_decision";
  }

  @Override
  public void executeLocal() {
    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
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

    String sign = getSign();

    if ( sign != null ) {
      decision.setSign(sign);

      Observable<DecisionError> info = getDecisionCreateOperationObservable(decision, TAG);

      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            onSuccess( this, data, false, true, TAG );
            finishOperationOnSuccess();
          },
          error -> onError( this, error.getLocalizedMessage(), true, TAG )
        );

    } else {
      onError( this, SIGN_ERROR_MESSAGE, true, TAG );
    }
  }
}
