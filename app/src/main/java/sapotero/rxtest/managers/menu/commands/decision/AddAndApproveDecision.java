package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Objects;

import io.requery.query.Tuple;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class AddAndApproveDecision extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String decisionId;

  public AddAndApproveDecision(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {

    updateLocal();
    EventBus.getDefault().post( new ShowNextDocumentEvent() );

    queueManager.add(this);
    store.process(
      store.startTransactionFor( params.getDocument() )
        .setLabel(LabelType.SYNC)
    );
  }

  private void updateLocal() {

    Timber.tag(TAG).e("updateLocal %s", new Gson().toJson( params ));


    Integer count = dataStore
      .update(RDecisionEntity.class)
      .set(RDecisionEntity.TEMPORARY, true)
      .where(RDecisionEntity.UID.eq(getDecisionUid()))
      .get().value();
    Timber.tag(TAG).i( "updateLocal: %s", count );

    Tuple red = dataStore
      .select(RDecisionEntity.RED)
      .where(RDecisionEntity.UID.eq(getDecisionUid()))
      .get().firstOrNull();

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( params.getDocument() ))
      .get()
      .value();

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .set(RDocumentEntity.MD5, "")
      .where(RDocumentEntity.UID.eq( params.getDocument() ))
      .get()
      .value();




    if (
      Objects.equals(params.getDecisionModel().getSignerId(), settings.getCurrentUserId())
      // или если подписывающий министр
      || ( red != null && red.get(0).equals(true) )
      ){
      Integer dec = dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .set(RDocumentEntity.MD5, "")
        .where(RDocumentEntity.UID.eq( params.getDocument() ))
        .get()
        .value();


      store.process(
        store.startTransactionFor( params.getDocument() )
          .setField(FieldType.PROCESSED, true)
      );


    }

    EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent(getDecisionUid()));
  }

  private String getDecisionUid() {
    return params.getDecisionModel().getId();
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

    Timber.tag(TAG).i( "type: %s", new Gson().toJson(params) );

    Retrofit retrofit = getRetrofit();

    Decision decision = params.getDecisionModel();
//    decision.setLetterheadFontSize("12");
//    decision.setPerformersFontSize("12");
    decision.setLetterhead(null);

    if (params.isAssignment()){
      decision.setAssignment(true);
    }

    decision.setApproved(true);

    String sign = null;

    try {
      sign = MainService.getFakeSign( settings.getPin(), null );
    } catch (Exception e) {
      e.printStackTrace();
    }
    decision.setSign(sign);

    String json_m = new Gson().toJson( decision );


    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      json_m
    );

    Timber.tag(TAG).e("DECISION");
    Timber.tag(TAG).e("%s", json);

    DocumentService operationService = retrofit.create( DocumentService.class );

    Observable<DecisionError> info = operationService.createAndSign(
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

            store.process(
              store.startTransactionFor( params.getDocument() )
                .removeLabel(LabelType.SYNC)
            );

          } else {
            store.process(
              store.startTransactionFor(params.getDocument())
                .removeLabel(LabelType.SYNC)
            );
            queueManager.setExecutedRemote(this);

            checkCreatorAndSignerIsCurrentUser(data, TAG);
          }

        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }

          if ( settings.isOnline() ){
            store.process(
              store.startTransactionFor( params.getDocument() )
                .removeLabel(LabelType.SYNC)
                .setField(FieldType.PROCESSED, false)
            );
            queueManager.setExecutedWithError(this, Collections.singletonList(error.getLocalizedMessage()));

          }
        }
      );
  }

  @Override
  public void withParams(CommandParams params) {
    this.params = params;
  }

  @Override
  public CommandParams getParams() {
    return params;
  }
}
