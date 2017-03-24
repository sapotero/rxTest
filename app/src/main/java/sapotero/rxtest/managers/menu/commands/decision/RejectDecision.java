package sapotero.rxtest.managers.menu.commands.decision;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;

import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.DecisionConverter;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.wrapper.DecisionWrapper;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.services.MainService;
import timber.log.Timber;

public class RejectDecision extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private Preference<String> PIN;
  private String folder_id;
  private RDecisionEntity decision;
  private String decisionId;
  private Preference<String> CURRENT_USER_ID;

  public RejectDecision(Context context, DocumentReceiver document){
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
    CURRENT_USER_ID = settings.getString("current_user_id");
    PIN = settings.getString("PIN");
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
    loadSettings();
    queueManager.add(this);
  }

  private Boolean hasActiveDecision(){
    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( document.getUid() ))
      .get().firstOrNull();

    Boolean result = false;

    if (doc != null && doc.getDecisions().size() > 0){
      for (RDecision _decision : doc.getDecisions()){
        RDecisionEntity decision = (RDecisionEntity) _decision;

        if (!decision.isApproved() && Objects.equals(decision.getSignerId(), CURRENT_USER_ID.get())){
          result = true;
        }
      }
    }

    Timber.tag(TAG).e("hasActiveDecision : %s", result);


    return result;
  }

  public void update() {

    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    if (params.getActiveDecision() != null && params.getActiveDecision()){
      try {

        String decision_uid = decision.getUid();

        dataStore
          .update(RDecisionEntity.class)
          .set( RDecisionEntity.APPROVED, false)
          .where(RDecisionEntity.UID.eq( decision_uid ));

        if ( !hasActiveDecision() ){
          dataStore
            .update(RDocumentEntity.class)
            .set( RDocumentEntity.FILTER, Fields.Status.PROCESSED.getValue() )
            .set( RDocumentEntity.MD5, "" )
            .set( RDocumentEntity.CHANGED, true)
            .where(RDocumentEntity.UID.eq( document.getUid() ));
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }



  @Override
  public String getType() {
    return "reject_decision";
  }

  @Override
  public void executeLocal() {

    queueManager.setExecutedLocal(this);

    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    update();
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

    Decision formated_decision;

    if ( params.getDecisionModel() != null ){
      formated_decision = params.getDecisionModel();
    } else {
      formated_decision = DecisionConverter.formatDecision( decision );
    }

    formated_decision.setApproved(false);
    formated_decision.setCanceled(true);
    formated_decision.setDocumentUid(null);

    String sign = null;

    try {
      sign = MainService.getFakeSign( context, PIN.get(), null );
    } catch (Exception e) {
      e.printStackTrace();
    }
    formated_decision.setSign(sign);


    if (params.getComment() != null){
      formated_decision.setComment( String.format( "Причина отклонения: %s", params.getComment() ) );
    }

    DecisionWrapper wrapper = new DecisionWrapper();
    wrapper.setDecision(formated_decision);

    String json_d = new Gson().toJson( wrapper );
    Timber.w("decision_json: %s", json_d);


    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      json_d
    );

    Timber.tag(TAG).e("DECISION");
    Timber.tag(TAG).e("%s", json);

    DocumentService operationService = retrofit.create( DocumentService.class );

    Observable<Object> info = operationService.update(
      decisionId,
      LOGIN.get(),
      TOKEN.get(),
      json
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data);

          if (callback != null){
            callback.onCommandExecuteSuccess( getType() );
          }

          queueManager.setExecutedRemote(this);
        },
        error -> {
          Timber.tag(TAG).i("error: %s", error);
          if (callback != null){
            callback.onCommandExecuteError(getType());
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