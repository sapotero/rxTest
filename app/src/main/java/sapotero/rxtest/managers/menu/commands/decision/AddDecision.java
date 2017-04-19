package sapotero.rxtest.managers.menu.commands.decision;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
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
  private Preference<String> PIN;
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
    STATUS_CODE = settings.getString("activity_main_menu.star");
    PIN = settings.getString("PIN");
  }

  public AddDecision withDecisionId(String decisionId){
    this.decisionId = decisionId;
    return this;
  }

  @Override
  public void execute() {

    CommandFactory.Operation operation = CommandFactory.Operation.CREATE_TEMPORARY_DECISION;
    CommandParams _params = new CommandParams();
    _params.setDecisionId( params.getDecisionModel().getId() );
    _params.setDecisionModel( params.getDecisionModel() );
    _params.setDocument(params.getDocument());
    _params.setAssignment(params.isAssignment());
    Command command = operation.getCommand(null, context, document, _params);
    command.execute();

    Timber.tag(TAG).w("ASSIGNMENT: %s", params.isAssignment() );

    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "add_decision";
  }

  @Override
  public void executeLocal() {
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
    queueManager.setExecutedLocal(this);
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


    Decision decision = params.getDecisionModel();
//    decision.setLetterheadFontSize("12");
//    decision.setPerformersFontSize("12");
    decision.setLetterhead(null);
    decision.setShowPosition( false );

    if (params.isAssignment()){
      decision.setAssignment(true);
    }

    String json_m = new Gson().toJson( decision );

//    Timber.w("decision_json_m: %s", json_m);

    RequestBody json = RequestBody.create(
      MediaType.parse("application/json"),
      json_m
    );

    Timber.tag(TAG).e("DECISION");
    Timber.tag(TAG).e("%s", json_m);

    DocumentService operationService = retrofit.create( DocumentService.class );

    Observable<DecisionError> info = operationService.create(
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
//          queueManager.setExecutedWithError(this, Collections.singletonList("http_error"));
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
