package sapotero.rxtest.managers.menu.commands.decision;

import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Objects;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class RejectDecision extends DecisionCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String folder_id;
  private RDecisionEntity decision;
  private String decisionId;

  public RejectDecision(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public RejectDecision withDecision(RDecisionEntity decision){
    this.decision = decision;
    return this;
  }
  public RejectDecision withDecisionId(String decisionId){
    this.decisionId = decisionId;
    return this;
  }

  @Override
  public void execute() {

    queueManager.add(this);

    String uid = params.getDecisionModel().getId();
//    store.setLabel(LabelType.SYNC, uid);
//    store.setField(FieldType.PROCESSED, true, uid);

    updateLocal();

    setDocOperationStartedInMemory( params.getDocument() );
  }


  private void updateLocal() {

    Timber.tag(TAG).e("1 updateLocal params%s", new Gson().toJson( params ));


    Integer count = dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.CHANGED, true)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq( params.getDocument() ))
      .get().value();



    if (Objects.equals(params.getDecisionModel().getSignerId(), settings.getCurrentUserId())){

      String uid = getUid();


      Timber.tag(TAG).i( "3 updateLocal document uid:\n%s\n%s\n%s\n", params.getDecisionModel().getDocumentUid(), params.getDocument(), document.getUid() );


      Integer dec = dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .set(RDocumentEntity.MD5, "")
        .where(RDocumentEntity.UID.eq( params.getDocument() ))
        .get().value();

      Timber.tag(TAG).e("3 updateLocal document %s | %s", uid, dec > 0);

      store.process(
        store.startTransactionFor( uid )
          .setLabel(LabelType.SYNC)
          .setField(FieldType.PROCESSED, true)
      );

    }

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

    if ( params.getDecisionModel() != null ){
      formated_decision = params.getDecisionModel();
    } else {
      formated_decision = mappers.getDecisionMapper().toFormattedModel( decision );
    }

    formated_decision.setApproved(false);
    formated_decision.setCanceled(true);
    formated_decision.setDocumentUid(null);

    if (params.getComment() != null){
      formated_decision.setComment( String.format( "Причина отклонения: %s", params.getComment() ) );
    }

    Observable<DecisionError> info = getDecisionUpdateOperationObservable(formated_decision, decisionId, TAG);

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          onSuccess( this, data, true, false, TAG );
          finishOperationOnSuccess( params.getDocument() );
        },
        error -> onError( this, params.getDocument(), error.getLocalizedMessage(), false, TAG )
      );
  }
}
