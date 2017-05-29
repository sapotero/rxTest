package sapotero.rxtest.managers.menu.commands.shared;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class RemoveFromFolder extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String folder_id;
  private String document_id;

  public RemoveFromFolder(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
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
    Integer count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.FAVORITES, false)
      .set( RDocumentEntity.FROM_FAVORITES_FOLDER, false)
      .where(RDocumentEntity.UID.eq(document_id))
      .get().value();
    Timber.tag(TAG).w( "updated: %s", count );

    store.removeLabel(LabelType.FAVORITES ,document_id);
    store.setLabel(LabelType.SYNC ,document_id);

    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "remove_from_folder";
  }

  @Override
  public void executeLocal() {

    queueManager.setExecutedLocal(this);


    if ( callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
  }

  @Override
  public void executeRemote() {
    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(settings.getHost() + "v3/operations/")
      .client(okHttpClient)
      .build();

    OperationService operationService = retrofit.create(OperationService.class);

    ArrayList<String> uids = new ArrayList<>();
    uids.add(settings.getUid());

    Observable<OperationResult> info = operationService.shared(
      getType(),
      settings.getLogin(),
      settings.getToken(),
      uids,
      document_id == null ? settings.getUid() : document_id,
      settings.getStatusCode(),
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

          store.removeLabel(LabelType.SYNC ,document_id);

        },
        error -> {
          if (callback != null) {
            callback.onCommandExecuteError(getType());
          }

          store.removeLabel(LabelType.SYNC ,document_id);
          store.setLabel(LabelType.FAVORITES ,document_id);
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
