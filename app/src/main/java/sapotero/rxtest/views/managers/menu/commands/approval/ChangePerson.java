package sapotero.rxtest.views.managers.menu.commands.approval;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class ChangePerson extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private String official_id;

  public ChangePerson(Context context, DocumentReceiver document){
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
  public ChangePerson withPerson(String uid){
    official_id = uid;
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

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    queueManager.add(this);
    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

  }

  private void update() {
    try {
      dataStore
        .update(RDocumentEntity.class)
        .set( RDocumentEntity.FILTER, Fields.Status.PROCESSED.getValue() )
        .set( RDocumentEntity.MD5, "" )
        .set( RDocumentEntity.PROCESSED, true)
        .where(RDocumentEntity.UID.eq(UID.get()))
        .get()
        .call();

      if (callback != null ){
        callback.onCommandExecuteSuccess( getType() );
      }
    } catch (Exception e) {
      Timber.tag(TAG).e( e );
    }
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( HOST.get() + "v3/operations/" )
      .client( okHttpClient )
      .build();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add( UID.get() );

    Observable<OperationResult> info = operationService.approval(
      getType(),
      LOGIN.get(),
      TOKEN.get(),
      uids,
      UID.get(),
      STATUS_CODE.get(),
      official_id,
      null
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          queueManager.remove(this);
          if (callback != null){
            callback.onCommandExecuteSuccess(getType());
          }
        },
        error -> {
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
