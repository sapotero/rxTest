package sapotero.rxtest.views.managers.menu.commands.shared;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;

import java.util.ArrayList;

import io.requery.query.Scalar;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class RemoveFromFolder extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private String folder_id;
  private String document_id;

  public RemoveFromFolder(Context context, DocumentReceiver document){
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
  }

  public RemoveFromFolder withFolder(String uid){
    folder_id = uid;
    return this;
  }

  public RemoveFromFolder withDocumentId(String uid) {
    this.document_id = uid;
    return this;
  }

  @Override
  public void execute() {
    Timber.tag(TAG).i("execute for %s - %s: %s",getType(),document_id, queueManager.getConnected());
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "remove_from_folder";
  }

  @Override
  public void executeLocal() {
    loadSettings();

    Scalar<Integer> count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.FAVORITES, false)
      .where(RDocumentEntity.UID.eq(document_id))
      .get();

    queueManager.setExecutedLocal(this);


    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  @Override
  public void executeRemote() {
    loadSettings();
    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(HOST.get() + "v3/operations/")
      .client(okHttpClient)
      .build();

    OperationService operationService = retrofit.create(OperationService.class);

    ArrayList<String> uids = new ArrayList<>();
    uids.add(UID.get());

    Observable<OperationResult> info = operationService.shared(
      getType(),
      LOGIN.get(),
      TOKEN.get(),
      uids,
      document_id == null ? UID.get() : document_id,
      STATUS_CODE.get(),
      folder_id,
      null
    );

    info.subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          queueManager.setExecutedRemote(this);

        },
        error -> {
          if (callback != null) {
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
