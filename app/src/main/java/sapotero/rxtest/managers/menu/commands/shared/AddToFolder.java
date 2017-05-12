package sapotero.rxtest.managers.menu.commands.shared;

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

public class AddToFolder extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> STATUS_CODE;
  private String folder_id;
  private String document_id;

  public AddToFolder(DocumentReceiver document){
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
    STATUS_CODE = settings.getString("activity_main_menu.star");
  }

  public AddToFolder withFolder(String uid){
    folder_id = uid;
    return this;
  }

  public AddToFolder withDocumentId(String uid) {
    this.document_id = uid;
    return this;
  }

  @Override
  public void execute() {
    Timber.tag(TAG).i("execute for %s - %s",getType(),document_id);
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "add_to_folder";
  }

  @Override
  public void executeLocal() {
    loadSettings();

    Integer count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.FAVORITES, true)
      .where(RDocumentEntity.UID.eq(document_id))
      .get().value();
    Timber.tag(TAG).w( "updated: %s", count );

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
      .baseUrl( settings2.getHost() + "v3/operations/" )
      .client( okHttpClient )
      .build();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add( settings2.getUid() );

    Observable<OperationResult> info = operationService.shared(
      getType(),
      settings2.getLogin(),
      settings2.getToken(),
      uids,
      document_id == null ? settings2.getUid() : document_id,
      STATUS_CODE.get(),
      folder_id,
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


  @Override
  public void withParams(CommandParams params) {
    this.params = params;
  }

  @Override
  public CommandParams getParams() {
    return params;
  }
}
