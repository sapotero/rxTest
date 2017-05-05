package sapotero.rxtest.managers.menu.commands.decision;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
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
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import timber.log.Timber;

public class SaveDecision extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private Preference<String> PIN;
  private RDecisionEntity decision;
  private String decisionId;
  private boolean withSign = false;

  public SaveDecision(Context context, DocumentReceiver document){
    super(context);
    this.context = context;
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  private void loadSettings(){
    LOGIN = settings.getString("login");
    TOKEN = settings.getString("token");
    UID   = settings.getString("activity_main_menu.uid");
    HOST  = settings.getString("settings_username_host");
    STATUS_CODE = settings.getString("activity_main_menu.star");
    PIN = settings.getString("PIN");
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
      .set(RDocumentEntity.MD5, "")
      .where(RDocumentEntity.UID.eq( params.getDecisionModel().getDocumentUid() ))
      .get()
      .value();

    EventBus.getDefault().post( new ShowNextDocumentEvent());
    update();

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
//    int count = dataStore
//      .delete(RDecisionEntity.class)
//      .where(RDecisionEntity.UID.eq(dec.getId()))
//      .get().value();
//    Timber.tag(TAG).e("DELETED %s", count);


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

    for (Block _block : dec.getBlocks()) {
      RBlockEntity block = new RBlockEntity();

      block.setTextBefore(_block.getTextBefore());
      block.setText(_block.getText());
      block.setAppealText(_block.getAppealText());
      block.setNumber(_block.getNumber());
      block.setToFamiliarization(_block.getAskToReport());
      block.setToCopy(_block.getToCopy());
      block.setHidePerformers(_block.getHidePerformers());
      block.setToFamiliarization(_block.getToFamiliarization());


      for (Performer _perf : _block.getPerformers()) {
        RPerformerEntity perf = new RPerformerEntity();

        perf.setPerformerId(_perf.getPerformerId());
        perf.setNumber(_perf.getNumber());
        perf.setPerformerText(_perf.getPerformerText());
        perf.setPerformerGender(_perf.getPerformerGender());
        perf.setOrganizationText(_perf.getOrganizationText());
        perf.setIsOriginal(_perf.getIsOriginal());
        perf.setIsResponsible(_perf.getIsResponsible());
        perf.setBlock(block);
        block.getPerformers().add(perf);
      }

      block.setDecision(decision);
      decision.getBlocks().add(block);
    }

//    RDocumentEntity doc = dataStore
//      .select(RDocumentEntity.class)
//      .where(RDocumentEntity.UID.eq(dec.getDocumentUid()))
//      .get()
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

    Timber.tag(TAG).e("1 update params%s", new Gson().toJson( params ));

//
//    Integer count = dataStore
//      .update(RDecisionEntity.class)
//      .set(RDecisionEntity.TEMPORARY, true)
//      .where(RDecisionEntity.UID.eq(dec.getId()))
//      .get().value();

//    Timber.tag(TAG).i( "2 update decision: %s", count );

  }

  @Override
  public void executeRemote() {
    loadSettings();

    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( HOST.get() )
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
      LOGIN.get(),
      TOKEN.get(),
      json
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {

          if (data.getErrors() !=null && data.getErrors().size() > 0){
            queueManager.setExecutedWithError(this, data.getErrors());
            EventBus.getDefault().post( new ForceUpdateDocumentEvent( data.getDocumentUid() ));
          } else {

            if (callback != null ){
              callback.onCommandExecuteSuccess( getType() );
              EventBus.getDefault().post( new UpdateDocumentEvent( document.getUid() ));
            }

            queueManager.setExecutedRemote(this);
          }

        },
        error -> {
          Timber.tag(TAG).i("error: %s", error);
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }

          // Раскоментировать перед релизом
          // if ( queueManager.getConnected() ){
          //   queueManager.setExecutedWithError(this, Collections.singletonList("http_error"));
          // }

          EventBus.getDefault().post( new ForceUpdateDocumentEvent( params.getDecisionModel().getDocumentUid() ));
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
