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
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class SaveAndApproveDecision extends DecisionCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private RDecisionEntity decision;
  private String decisionId;
  private boolean withSign = false;

  public SaveAndApproveDecision(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public SaveAndApproveDecision withDecision(RDecisionEntity decision){
    this.decision = decision;
    return this;
  }
  public SaveAndApproveDecision withDecisionId(String decisionId){
    this.decisionId = decisionId;
    return this;
  }

  @Override
  public void execute() {

    queueManager.add(this);
    updateLocal();

    setDocOperationStartedInMemory( params.getDocument() );
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

    Timber.tag(TAG).e("updateLocal %s", new Gson().toJson( params ));


    Integer count = dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq( params.getDecisionModel().getId() ))
      .get().value();
    Timber.tag(TAG).i( "updateLocal: %s", count );

    Tuple red = dataStore
      .select(RDecisionEntity.RED)
      .where(RDecisionEntity.UID.eq(params.getDecisionModel().getId()))
      .get().firstOrNull();

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( params.getDocument() ))
      .get()
      .value();


    Timber.tag(TAG).e("-------- %s %s", params.getDecisionModel().getSignerId(), settings.getCurrentUserId());
    if (
      Objects.equals(params.getDecisionModel().getSignerId(), settings.getCurrentUserId())
      // или если подписывающий министр
//      || ( red != null && red.startTransactionFor(0).equals(true) )
      ){
      Integer dec = dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .set(RDocumentEntity.MD5, "")
        .where(RDocumentEntity.UID.eq(  params.getDocument() ))
        .get()
        .value();



      store.process(
        store.startTransactionFor(  params.getDocument() )
          .setLabel(LabelType.SYNC)
          .setField(FieldType.PROCESSED, true)
      );

      EventBus.getDefault().post( new ShowNextDocumentEvent());
    }

    EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( params.getDecisionModel().getId() ));
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

//    Decision formated_decision = DecisionConverter.formatDecision( decision );
//
//    DecisionWrapper wrapper = new DecisionWrapper();
//    wrapper.setDecision(formated_decision);

    Decision _decision = params.getDecisionModel();
//    _decision.setDocumentUid( document.getUid() );
    _decision.setDocumentUid( null );
    _decision.setApproved(true);

    String sign = getSign();

    _decision.setSign( sign );

    Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision, decisionId, TAG);

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          onSuccess( this, data, false, true, TAG );
          finishOperationOnSuccess( params.getDocument() );
        },
        error -> onError( this, params.getDocument(), error.getLocalizedMessage(), true, TAG )
      );
  }

  public SaveAndApproveDecision withSign(boolean withSign) {
    this.withSign = withSign;
    return this;
  }
}
