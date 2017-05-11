package sapotero.rxtest.managers.menu.commands.approval;

import com.f2prateek.rx.preferences.Preference;

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
import timber.log.Timber;

public class ChangePerson extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private Preference<String> PIN;
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

  private void loadSettings(){
    UID   = settings.getString("activity_main_menu.uid");
    HOST  = settings.getString("settings_username_host");
    STATUS_CODE = settings.getString("activity_main_menu.star");
    PIN = settings.getString("PIN");
  }
  public ChangePerson withPerson(String uid){
    official_id = uid;
    return this;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent());
  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    loadSettings();

    int count = dataStore
      .update(RDocumentEntity.class)
//      .set( RDocumentEntity.FILTER, Fields.Status.PROCESSED.getValue() )
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq( params.getDocument() != null ? params.getDocument(): UID.get()))
      .get()
      .value();

    queueManager.setExecutedLocal(this);

    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

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
    uids.add( params.getDocument() != null ? params.getDocument(): UID.get() );

    String comment = null;
    if ( params.getComment() != null ){
      comment = params.getComment();
    }

    String sign = null;

    try {
      sign = MainService.getFakeSign( PIN.get(), null );
    } catch (Exception e) {
      e.printStackTrace();
    }

    Observable<OperationResult> info = operationService.approval(
      getType(),
      settings2.getLogin(),
      settings2.getToken(),
      uids,
      comment,
      STATUS_CODE.get(),
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
