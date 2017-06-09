package sapotero.rxtest.managers.menu.commands.shared;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class CheckControlLabel extends AbstractCommand {

  private final DocumentReceiver document;
  private String TAG = this.getClass().getSimpleName();
  private String document_id;

  public CheckControlLabel(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public CheckControlLabel withDocumentId(String uid) {
    this.document_id = uid;
    return this;
  }

  @Override
  public void execute() {

    Timber.tag(TAG).i("execute for %s - %s",getType(),document_id);
    queueManager.add(this);

    store.process(
      store.startTransactionFor(document_id)
      .setLabel(LabelType.SYNC)
      .setLabel(LabelType.CONTROL)
    );

  }

  @Override
  public String getType() {
    return "check_for_control";
  }


  @Override
  public void executeLocal() {
    setControl(true);
    queueManager.setExecutedLocal(this);

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
      .baseUrl( settings.getHost() + "v3/operations/" )
      .client( okHttpClient )
      .build();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add( settings.getUid() );

    Observable<OperationResult> info = operationService.shared(
      getType(),
      settings.getLogin(),
      settings.getToken(),
      uids,
      document_id == null ? settings.getUid() : document_id,
      settings.getStatusCode(),
      null,
      null
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          Timber.tag(TAG).i("ok: %s", result.getOk());
          Timber.tag(TAG).i("error: %s", result.getMessage());
          Timber.tag(TAG).i("type: %s", result.getType());

          if ( Objects.equals(result.getType(), "danger") && result.getMessage() != null){
            EventBus.getDefault().post( new ShowSnackEvent( result.getMessage() ));
            setAsError();
            setControl(false);
            queueManager.setExecutedWithError(this, Collections.singletonList( result.getMessage() ));
          } else {
            if (callback != null){
              callback.onCommandExecuteSuccess(getType());
            }
            store.process(
              store.startTransactionFor(document_id)
                .removeLabel(LabelType.SYNC)
            );
            queueManager.setExecutedRemote(this);
          }
        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }
          setAsError();
          queueManager.setExecutedWithError(this, Collections.singletonList(error.getLocalizedMessage()));
          setControl(false);
        }
      );

  }

  private void setAsError() {
    store.process(
      store.startTransactionFor(document_id)
        .removeLabel(LabelType.SYNC)
        .removeLabel(LabelType.CONTROL)
    );

  }

  private void setControl(Boolean control) {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.CONTROL, control)
      .where(RDocumentEntity.UID.eq(document_id))
      .get()
      .value();
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
