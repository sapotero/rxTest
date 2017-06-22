package sapotero.rxtest.managers.menu.commands.decision;

import android.support.annotation.Nullable;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.requery.query.Tuple;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.retrofit.models.wrapper.DecisionWrapper;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class ApproveDecision extends AbstractCommand {

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
    setDocOperationStartedInMemory( params.getDocument() );
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

    Retrofit retrofit = getRetrofit();

    String sign = null;

    try {
      sign = MainService.getFakeSign( settings.getPin(), null );
    } catch (Exception e) {
      e.printStackTrace();
    }

    Decision _decision = params.getDecisionModel();
    _decision.setDocumentUid( null );
    _decision.setApproved(true);
    _decision.setSign( sign );

    if (params.isAssignment()){
      _decision.setAssignment(true);
    }

    DecisionWrapper wrapper = new DecisionWrapper();
    wrapper.setDecision(_decision);

    String json_d = new Gson().toJson( wrapper );
    Timber.w("decision_json: %s", json_d);


    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      json_d
    );

    Timber.tag(TAG).e("DECISION");
    Timber.tag(TAG).e("%s", json);

    DocumentService operationService = retrofit.create( DocumentService.class );

    Observable<DecisionError> info = operationService.update(
      decisionId,
      settings.getLogin(),
      settings.getToken(),
      json
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {

          if (data.getErrors() !=null && data.getErrors().size() > 0){
            queueManager.setExecutedWithError(this, data.getErrors());
            EventBus.getDefault().post( new ForceUpdateDocumentEvent( params.getDocument() ));

          } else {

            if (callback != null ){
              callback.onCommandExecuteSuccess( getType() );
            }
            EventBus.getDefault().post( new UpdateDocumentEvent( params.getDocument() ));

            queueManager.setExecutedRemote(this);
          }

          finishOperationOnSuccess( params.getDocument() );

        },
        error -> {
          Timber.tag(TAG).i("error: %s", error);
          if (callback != null){
            callback.onCommandExecuteError(error.getLocalizedMessage());
          }

          if ( settings.isOnline() ){
            finishOperationProcessedOnError( this, params.getDocument(), Collections.singletonList(error.getLocalizedMessage() ) );
          }
        }
      );
  }
}
