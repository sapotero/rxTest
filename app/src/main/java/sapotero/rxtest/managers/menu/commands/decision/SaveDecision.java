package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class SaveDecision extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private RDecisionEntity decision;
  private String decisionId;
  private boolean withSign = false;

  public SaveDecision(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public SaveDecision withDecision(RDecisionEntity decision){
    this.decision = decision;
    return this;
  }
  public SaveDecision withDecisionId(String decisionId){
    this.decisionId = decisionId;
    return this;
  }

  @Override
  public void execute() {


    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
//      .set(RDocumentEntity.MD5, "")
      .where(RDocumentEntity.UID.eq( params.getDocument() ))
      .get()
      .value();

//    EventBus.getDefault().post( new ShowNextDocumentEvent());
    update();

    queueManager.add(this);

    store.process(
      store.startTransactionFor( params.getDocument() )
        .setLabel(LabelType.SYNC)
    );

  }

  @Override
  public String getType() {
    return "save_decision";
  }

  @Override
  public void executeLocal() {
    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
    queueManager.setExecutedLocal(this);
  }

  private void update() {

    Decision dec = params.getDecisionModel();
    Timber.tag(TAG).e("UPDATE %s", new Gson().toJson(dec));
//
//    int put = dataStore
//      .delete(RDecisionEntity.class)
//      .where(RDecisionEntity.UID.eq(dec.getId()))
//      .startTransactionFor().value();
//    Timber.tag(TAG).e("DELETED %s", put);


    // RDecisionEntity decision = new RDecisionEntity();
    RDecisionEntity decision = dataStore
      .select(RDecisionEntity.class)
      .where(RDecisionEntity.UID.eq(dec.getId()))
      .get().firstOrNull();

    decision.setTemporary(true);

    if (dec.getUrgencyText() != null) {
      decision.setUrgencyText(dec.getUrgencyText());
    }

    decision.setComment(dec.getComment());
    decision.setDate( dec.getDate());
    decision.setSigner( dec.getSigner() );
    decision.setSignerBlankText(dec.getSignerBlankText());
    decision.setSignerId(dec.getSignerId());
    decision.setSignerPositionS(dec.getSignerPositionS());
    decision.setTemporary(true);
    decision.setApproved(dec.getApproved());
    decision.setChanged(true);
    decision.setRed(dec.getRed());

    if (dec.getBlocks().size() > 0) {
      decision.getBlocks().clear();
    }

    BlockMapper blockMapper = mappers.getBlockMapper();

    for (Block _block : dec.getBlocks()) {
      RBlockEntity block = blockMapper.toEntity(_block);
      block.setDecision(decision);
      decision.getBlocks().add(block);
    }

//    RDocumentEntity doc = dataStore
//      .select(RDocumentEntity.class)
//      .where(RDocumentEntity.UID.eq(dec.getDocumentUid()))
//      .startTransactionFor()
//      .first();
//
//    decision.setDocument( doc );

    dataStore
      .update(decision)
      .toObservable()
      .observeOn(Schedulers.io())
      .subscribeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.tag(TAG).e("UPDATED %s", data.getSigner() );
          EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( data.getUid() ));
//          queueManager.setExecutedRemote(this);
          queueManager.add(this);
        },
        error -> {
          queueManager.setExecutedWithError(this, Collections.singletonList("db_error"));
        }
      );

    Timber.tag(TAG).e("1 updateFromJob params%s", new Gson().toJson( params ));

//
//    Integer put = dataStore
//      .updateFromJob(RDecisionEntity.class)
//      .set(RDecisionEntity.TEMPORARY, true)
//      .where(RDecisionEntity.UID.eq(dec.getId()))
//      .startTransactionFor().value();

//    Timber.tag(TAG).i( "2 updateFromJob decision: %s", put );

  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( settings.getHost() )
      .client( okHttpClient )
      .build();

//    Decision formated_decision = DecisionConverter.formatDecision( decision );
//
//    DecisionWrapper wrapper = new DecisionWrapper();
//    wrapper.setDecision(formated_decision);

    Decision _decision = params.getDecisionModel();
//    _decision.setDocumentUid( document.getUid() );
    _decision.setDocumentUid( null );


    if (withSign){
      _decision.setApproved(true);
    }

    String json_m = new Gson().toJson( _decision );

    Timber.w("decision_json_m: %s", json_m);

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      json_m
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

            store.process(
              store.startTransactionFor( params.getDocument() )
                .removeLabel(LabelType.SYNC)
            );

          } else {
            store.process(
              store.startTransactionFor( params.getDocument() )
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

  public SaveDecision withSign(boolean withSign) {
    this.withSign = withSign;
    return this;
  }
}
