package sapotero.rxtest.managers.menu.commands.signing;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

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
    EventBus.getDefault().post( new ShowNextDocumentEvent());
    store.process(
      store.startTransactionFor( getUid() )
        .setLabel(LabelType.SYNC)
        .setField(FieldType.PROCESSED, true)
        .setField(FieldType.MD5, "")
    );

  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    int count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(
        getUid()))
      .get()
      .value();
    if (callback != null){
      callback.onCommandExecuteSuccess(getType());
    }
    queueManager.setExecutedLocal(this);
  }

  private String getUid() {
    return params.getDocument() != null ? params.getDocument(): settings.getUid();
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
    uids.add(getUid());

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


    Observable<OperationResult> info = operationService.sign(
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
          );
        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }

          store.process(
            store.startTransactionFor( getUid() )
              .removeLabel(LabelType.SYNC)
              .setField(FieldType.PROCESSED, false)
          );

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
