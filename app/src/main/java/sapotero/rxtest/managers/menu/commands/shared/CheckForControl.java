package sapotero.rxtest.managers.menu.commands.shared;

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

  private String TAG = this.getClass().getSimpleName();

  private String document_id;

  public CheckForControl(DocumentReceiver document){
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

    Timber.tag(TAG).i("execute for %s - %s",getType(),document_id);
    queueManager.add(this);

  }

  @Override
  public String getType() {
    return "check_for_control";
  }

  @Override
  public void executeLocal() {
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
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

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
      settings2.getStatusCode(),
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
