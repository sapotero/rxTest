package sapotero.rxtest.managers.menu.commands.shared;

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
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class CheckForControl extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private Preference<String> PIN;
  private String document_id;

  public CheckForControl(Context context, DocumentReceiver document){
    super();
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

    Timber.tag(TAG).i("execute for %s - %s",getType(),document_id);
    queueManager.add(this);

  }

  @Override
  public String getType() {
    return "check_for_control";
  }

  @Override
  public void executeLocal() {
    loadSettings();

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( document_id ))
      .get()
      .toObservable()
      .flatMap( doc -> Observable.just( doc.isControl() ) )
      .subscribe(
        value -> {
          Timber.tag(TAG).i("executeLocal for %s: CONTROL: %s",document_id, value);
          try {

            if (value == null){
              value = false;
            }

            dataStore
              .update(RDocumentEntity.class)
              .set( RDocumentEntity.CONTROL, !value)
              .where(RDocumentEntity.UID.eq(document_id))
              .get()
              .call();

            queueManager.setExecutedLocal(this);
            if ( callback != null ){
              callback.onCommandExecuteSuccess( getType() );
            }

          } catch (Exception e) {
            Timber.tag(TAG).e("error executeLocal for %s [%s]: %s", document_id, getType(), e);
            if ( callback != null ){
              callback.onCommandExecuteError(getType());
            }
          }
        },
        error -> {
          Timber.tag(TAG).i("error %s",error);
          if ( callback != null ){
            callback.onCommandExecuteError(getType());
          }
        });

  }

  @Override
  public void executeRemote() {
    loadSettings();

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

    Observable<OperationResult> info = operationService.shared(
      getType(),
      LOGIN.get(),
      TOKEN.get(),
      uids,
      document_id == null ? UID.get() : document_id,
      STATUS_CODE.get(),
      null,
      null
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          queueManager.setExecutedRemote(this);
        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }
        }
      );

  }

  public CheckForControl withDocumentId(String uid) {
    this.document_id = uid;
    return this;
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
