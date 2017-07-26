package sapotero.rxtest.managers.menu.commands.decision;

import android.support.annotation.Nullable;

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
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class ApproveDecision extends DecisionCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private RDecisionEntity decision;
  private String decisionId;

  public ApproveDecision(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public ApproveDecision withDecision(RDecisionEntity decision){
    this.decision = decision;
    return this;
  }
  public ApproveDecision withDecisionId(String decisionId){
    this.decisionId = decisionId;
    return this;
  }

  @Override
  public void execute() {
    updateLocal();
    queueManager.add(this);
    setDocOperationStartedInMemory();
  }


  private void updateLocal() {

    Timber.tag(TAG).e("1 updateLocal params%s", new Gson().toJson( params ));


    Integer count = dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq(params.getDecisionModel().getId()))
      .get().value();

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .set(RDocumentEntity.MD5, "")
      .where(RDocumentEntity.UID.eq( params.getDocument() ))
      .get()
      .value();
    Tuple red = dataStore
      .select(RDecisionEntity.RED)
      .where(RDecisionEntity.UID.eq(params.getDecisionModel().getId()))
      .get().firstOrNull();

    if (
        // если активная резолюция
        Objects.equals(params.getDecisionModel().getSignerId(), settings.getCurrentUserId())

        // или если подписывающий министр
        || ( red != null && red.get(0).equals(true) )

      ){


      String uid = getUid();

      store.process(
        store.startTransactionFor( params.getDocument() )
          .setLabel(LabelType.SYNC)
          .setField(FieldType.PROCESSED, true)
      );


      Timber.tag(TAG).i( "3 updateLocal document uid:\n%s\n%s\n%s\n", params.getDecisionModel().getDocumentUid(), params.getDocument(), document.getUid() );


      Integer dec = dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .set(RDocumentEntity.MD5, "")
        .where(RDocumentEntity.UID.eq( params.getDocument() ))
        .get().value();

      Timber.tag(TAG).e("3 updateLocal document %s | %s", uid, dec > 0);

//      EventBus.getDefault().post( new ShowNextDocumentEvent());
    }


    Observable.just("").timeout(100, TimeUnit.MILLISECONDS).subscribe(
      data -> {
        Timber.tag("slow").e("exec");
        EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( params.getDecisionModel().getId() ));
      }, error -> {
        Timber.tag(TAG).e(error);
      }
    );

  }

  @Nullable
  private String getUid() {
    String uid = null;
    if (params.getDecisionModel().getDocumentUid() != null && !Objects.equals(params.getDecisionModel().getDocumentUid(), "")){
      uid = params.getDecisionModel().getDocumentUid();
    }

    if (params.getDocument() != null && !Objects.equals(params.getDocument(), "")){
      uid = params.getDocument();
    }

    if (document.getUid() != null && !Objects.equals(document.getUid(), "")){
      uid = document.getUid();
    }

    Timber.tag(TAG).e( "%s | %s | %s", params.getDecisionModel().getDocumentUid(), params.getDocument(), document.getUid() );


    return uid;
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

    Decision _decision = params.getDecisionModel();
    _decision.setDocumentUid( null );
    _decision.setApproved(true);
    _decision.setSign( sign );

    if (params.isAssignment()){
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
  }
}
