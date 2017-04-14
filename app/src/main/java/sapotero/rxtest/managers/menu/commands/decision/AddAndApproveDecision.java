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
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.services.MainService;
import timber.log.Timber;

public class AddAndApproveDecision extends AbstractCommand {

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

  public AddAndApproveDecision(Context context, DocumentReceiver document){
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

  @Override
  public void execute() {
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent() );
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
    loadSettings();
    queueManager.setAsRunning(this);

    Timber.tag(TAG).i( "type: %s", new Gson().toJson(params) );

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( HOST.get() )
      .client( okHttpClient )
      .build();


    Decision decision = params.getDecisionModel();
    decision.setLetterheadFontSize("15");
    decision.setPerformersFontSize("12");
    decision.setLetterhead(null);

    if (params.isAssignment()){
      decision.setAssignment(true);
    }

    decision.setApproved(true);

    String sign = null;

    try {
      sign = MainService.getFakeSign( context, PIN.get(), null );
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
      LOGIN.get(),
      TOKEN.get(),
      json
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {

          queueManager.setExecutedRemote(this);

//          if (data.getErrors() !=null && data.getErrors().size() > 0){
//            queueManager.setExecutedWithError(this, data.getErrors());
//            EventBus.getDefault().post( new ForceUpdateDocumentEvent( data.getDocumentUid() ));
//          } else {
//
//            if (callback != null ){
//              callback.onCommandExecuteSuccess( getType() );
//              EventBus.getDefault().post( new UpdateDocumentEvent( document.getUid() ));
//            }
//
//            queueManager.setExecutedRemote(this);
//          }

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
