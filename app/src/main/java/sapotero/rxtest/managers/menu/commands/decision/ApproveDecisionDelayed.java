package sapotero.rxtest.managers.menu.commands.decision;

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
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.retrofit.models.wrapper.DecisionWrapper;
import sapotero.rxtest.services.MainService;
import timber.log.Timber;

public class ApproveDecisionDelayed extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  public ApproveDecisionDelayed(DocumentReceiver document){
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
    queueManager.add(this);
  }


  public void update() {

    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    try {
      dataStore
        .update(RDecisionEntity.class)
        .set( RDecisionEntity.APPROVED, true)
        .where(RDecisionEntity.UID.eq( params.getDecisionId() )).get().call();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Override
  public String getType() {
    return "approve_decision_delayed";
  }

  @Override
  public void executeLocal() {
    update();

    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    queueManager.setExecutedLocal(this);
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

    String sign = null;

    try {
      sign = MainService.getFakeSign( settings.getPin(), null );
    } catch (Exception e) {
      e.printStackTrace();
    }

    RDecisionEntity decision= getDecision(params.getDecisionId());
    if ( decision != null) {

      Decision _decision = mappers.getDecisionMapper().toFormattedModel(decision);
      _decision.setDocumentUid( null );
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
        params.getDecisionId(),
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
//            queueManager.setExecutedWithError(this, Collections.singletonList("http_error"));
            EventBus.getDefault().post( new ForceUpdateDocumentEvent( params.getDecisionModel().getDocumentUid() ));
          }
        );
    } else {
      Timber.tag(TAG).i("error: no decision yet");
      if (callback != null){
        callback.onCommandExecuteError(getType());
      }
    }

  }

  private RDecisionEntity getDecision(String uid){
    return dataStore.select(RDecisionEntity.class).where(RDecisionEntity.UID.eq(uid)).get().firstOrNull();
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
