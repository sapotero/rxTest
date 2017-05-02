package sapotero.rxtest.managers.menu.commands.report;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.events.view.ShowPrevDocumentEvent;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class ReturnToPrimaryConsideration extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private Preference<String> PIN;

  public ReturnToPrimaryConsideration(Context context, DocumentReceiver document){
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
    loadSettings();

    EventBus.getDefault().post( new ShowPrevDocumentEvent());

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

    Observable<OperationResult> info = operationService.report(
      getType(),
      LOGIN.get(),
      TOKEN.get(),
      uids,
      UID.get(),
      STATUS_CODE.get()
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
    return "return_to_the_primary_consideration";
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
