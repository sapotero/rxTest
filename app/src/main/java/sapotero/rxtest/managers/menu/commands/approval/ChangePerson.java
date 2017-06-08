package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class ChangePerson extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String official_id;

  public ChangePerson(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public ChangePerson withPerson(String uid){
    official_id = uid;
    return this;
  }

  @Override
  public void execute() {
    queueManager.add(this);

    store.process(
      store.startTransactionFor( getUid() )
        .setLabel(LabelType.SYNC)
        .setField(FieldType.PROCESSED, true)
        .setState(InMemoryState.LOADING)
    );

    EventBus.getDefault().post( new ShowNextDocumentEvent());
  }

  private String getUid() {
    return params.getDocument() != null ? params.getDocument() : settings.getUid();
  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    String uid = getUid();

    int count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.PROCESSED, true)
      .where(RDocumentEntity.UID.eq(uid))
      .get()
      .value();

    queueManager.setExecutedLocal(this);

//    store.setLabel(LabelType.SYNC ,uid);
//    store.setField(FieldType.PROCESSED ,true ,uid);

    if (callback != null ){
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
    String uid = getUid();

    uids.add(uid);

    String comment = null;
    if ( params.getComment() != null ){
      comment = params.getComment();
    }

    String sign = null;

    try {
      sign = MainService.getFakeSign( settings.getPin(), null );
    } catch (Exception e) {
      e.printStackTrace();
    }

    Observable<OperationResult> info = operationService.approval(
      getType(),
      settings.getLogin(),
      settings.getToken(),
      uids,
      comment,
      settings.getStatusCode(),
      official_id,
      sign
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          queueManager.setExecutedRemote(this);

          store.process(
            store.startTransactionFor( getUid() )
              .removeLabel(LabelType.SYNC)
              .setField(FieldType.MD5, "")
              .setState(InMemoryState.READY)
          );
        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }

          if ( settings.isOnline() ){
            store.process(
              store.startTransactionFor( getUid() )
                .removeLabel(LabelType.SYNC)
                .setField(FieldType.PROCESSED, false)
                .setState(InMemoryState.READY)
            );
            queueManager.setExecutedWithError(this, Collections.singletonList(error.getLocalizedMessage()));

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
