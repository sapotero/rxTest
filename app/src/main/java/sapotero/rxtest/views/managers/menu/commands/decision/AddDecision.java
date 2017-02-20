package sapotero.rxtest.views.managers.menu.commands.decision;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class AddDecision extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private String decisionId;

  public AddDecision(Context context, DocumentReceiver document){
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
    STATUS_CODE = settings.getString("activity_main_menu.start");
  }

  public AddDecision withDecisionId(String decisionId){
    this.decisionId = decisionId;
    return this;
  }

  @Override
  public void execute() {
    loadSettings();

    if ( queueManager.getConnected() ){
      executeRemote();
    } else {
      executeLocal();
    }
    update();

  }


  public void update() {
    try {
//      RDocumentEntity document = (RDocumentEntity) decision.getDocument();
//      String decision_uid = decision.getUid();
//      String document_uid = document.getUid();
//
//      dataStore
//        .update(RDocumentEntity.class)
//        .set( RDocumentEntity.FILTER, Fields.Status.PROCESSED.getValue())
//        .where(RDocumentEntity.UID.eq( document_uid ))
//        .get()
//        .call();
//
//      dataStore
//        .update(decision).toObservable().subscribe();
      if (callback != null ){
        callback.onCommandExecuteSuccess( getType() );
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getType() {
    return "add_decision";
  }

  @Override
  public void executeLocal() {
    queueManager.add(this);
    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  @Override
  public void executeRemote() {

    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( HOST.get() )
      .client( okHttpClient )
      .build();


    Decision decision = params.getDecisionModel();
    decision.setLetterheadFontSize("15");
    decision.setPerformersFontSize("12");

    String json_m = new Gson().toJson( decision );

    Timber.w("decision_json_m: %s", json_m);

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      json_m
    );

    Timber.tag(TAG).e("DECISION");
    Timber.tag(TAG).e("%s", json);

    DocumentService operationService = retrofit.create( DocumentService.class );

    Observable<Object> info = operationService.create(
      LOGIN.get(),
      TOKEN.get(),
      json
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          String data_JSON = new Gson().toJson(data);

          Timber.tag(TAG).i("new id: %s", data_JSON );

          try {
            Decision new_decision = new Gson().fromJson( data_JSON, Decision.class );
            Timber.tag(TAG).i("new decision: %s", new_decision );
          } catch (JsonSyntaxException e) {
            e.printStackTrace();
          }

          if (callback != null ){
            callback.onCommandExecuteSuccess( getType() );
          }

          EventBus.getDefault().post( new UpdateDocumentEvent( document.getUid() ));

          update();
        },
        error -> {
          Timber.tag(TAG).i("error: %s", error);
          if (callback != null){
            callback.onCommandExecuteError();
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
