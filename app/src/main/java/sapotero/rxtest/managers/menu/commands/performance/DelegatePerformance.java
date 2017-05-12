package sapotero.rxtest.managers.menu.commands.performance;

import com.f2prateek.rx.preferences.Preference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public class DelegatePerformance extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> STATUS_CODE;
  private String official_id;

  public DelegatePerformance(DocumentReceiver document){
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

  public DelegatePerformance withPerson(String uid){
    official_id = uid;
    return this;
  }

  @Override
  public void execute() {
    loadSettings();
    EventBus.getDefault().post( new ShowNextDocumentEvent());

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

    Observable<OperationResult> info = operationService.performance(
      getType(),
      settings2.getLogin(),
      settings2.getToken(),
      uids,
      settings2.getUid(),
      STATUS_CODE.get(),
      official_id
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());

          if (callback != null){
            callback.onCommandExecuteSuccess(getType());
          }
        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }
        }
      );

  }

  @Override
  public String getType() {
    return "delegate_performance";
  }

  @Override
  public void executeLocal() {

  }

  @Override
  public void executeRemote() {

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
