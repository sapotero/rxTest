package sapotero.rxtest.managers.menu.commands.templates;

import com.f2prateek.rx.preferences.Preference;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.TemplatesService;
import sapotero.rxtest.retrofit.models.Template;

public class RemoveTemplate extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> HOST;

  public RemoveTemplate(DocumentReceiver document){
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
    HOST  = settings.getString("settings_username_host");
  }

  @Override
  public void execute() {
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "remove_template";
  }

  @Override
  public void executeLocal() {
    loadSettings();

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
      .baseUrl( HOST.get() )
      .client( okHttpClient )
      .build();

    TemplatesService templatesService = retrofit.create( TemplatesService.class );

    Observable<Template> info = templatesService.remove(
      params.getUuid(),
      settings2.getLogin(),
      settings2.getToken()
    );

    info
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          queueManager.setExecutedRemote(this);
          remove(data);
        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }
        }
      );
  }

  private void remove(Template data) {
    dataStore.delete(RTemplateEntity.class).where(RTemplateEntity.UID.eq(data.getId())).get().value();
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
