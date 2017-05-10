package sapotero.rxtest.managers.menu.commands.decision;

import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import io.requery.query.Tuple;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.services.MainService;
import timber.log.Timber;

public class SaveAndApproveDecision extends AbstractCommand {

  private final DocumentReceiver document;

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

  private void loadSettings(){
    LOGIN = settings.getString("login");
    TOKEN = settings.getString("token");
    UID   = settings.getString("activity_main_menu.uid");
    HOST  = settings.getString("settings_username_host");
    STATUS_CODE = settings.getString("activity_main_menu.star");
    PIN = settings.getString("PIN");
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
      .where(RDecisionEntity.UID.eq(params.getDecisionModel().getId()))
      .get().value();
    Timber.tag(TAG).i( "updateLocal: %s", count );

    Tuple red = dataStore
      .select(RDecisionEntity.RED)
      .where(RDecisionEntity.UID.eq(params.getDecisionModel().getId()))
      .get().firstOrNull();

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( params.getDecisionModel().getDocumentUid() ))
      .get()
      .value();


    Timber.tag(TAG).e("-------- %s %s", params.getDecisionModel().getSignerId(), settings.getString("current_user_id").get());
    if (
      Objects.equals(params.getDecisionModel().getSignerId(), settings.getString("current_user_id").get())
      // или если подписывающий министр
//      || ( red != null && red.get(0).equals(true) )
      ){
      Integer dec = dataStore
        .update(RDocumentEntity.class)
        .set(RDocumentEntity.PROCESSED, true)
        .set(RDocumentEntity.MD5, "")
        .where(RDocumentEntity.UID.eq( params.getDecisionModel().getDocumentUid() ))
        .get()
        .value();

      Timber.tag(TAG).e("count %s", dec);
      EventBus.getDefault().post( new ShowNextDocumentEvent());
    }

    EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( params.getDecisionModel().getId() ));
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
    _decision.setApproved(true);

    try {
      if ( settings.getBoolean("SIGN_WITH_DC").get() ){
        String fake_sign = null;

        fake_sign = MainService.getFakeSign( PIN.get(), null );

        if (fake_sign != null) {
          _decision.setSign(fake_sign);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    String json_d = new Gson().toJson( decision );
    String json_m = new Gson().toJson( _decision );

//    Timber.w("decision_json: %s", json_d);
    Timber.w("decision_json_m: %s", json_m);
    Timber.w("decision_json_old: %s", json_d);

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
            }
            EventBus.getDefault().post( new UpdateDocumentEvent( document.getUid() ));

            queueManager.setExecutedRemote(this);
          }

//          EventBus.getDefault().post( new UpdateCurrentDocumentEvent( data.getDocumentUid() ));

        },
        error -> {
          Timber.tag(TAG).i("error: %s", error);
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }
//          queueManager.setExecutedWithError(this, Collections.singletonList("http_error"));
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

  public SaveAndApproveDecision withSign(boolean withSign) {
    this.withSign = withSign;
    return this;
  }
}
