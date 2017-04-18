package sapotero.rxtest.managers.menu.commands.templates;

import android.content.Context;

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
import timber.log.Timber;

public class CreateTemplate extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> HOST;

  public CreateTemplate(Context context, DocumentReceiver document){
    super(context);
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
    HOST  = settings.getString("settings_username_host");
    TOKEN = settings.getString("token");
  }

  @Override
  public void execute() {
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "create_template";
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

    Observable<Template> info = templatesService.create(
      LOGIN.get(),
      TOKEN.get(),
      params.getComment()
    );

    info
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          queueManager.setExecutedRemote(this);

          insertTemplate(data);
        },
        error -> {
          if (callback != null){
            callback.onCommandExecuteError(getType());
          }
        }
      );
  }

  private void insertTemplate(Template data) {

    RTemplateEntity template = new RTemplateEntity();
    template.setUid(data.getId());
    template.setType(data.getType());
    template.setTitle(data.getText());
    template.setUser(LOGIN.get());

    dataStore
      .insert(template)
      .toObservable()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        temp -> {
          Timber.tag(TAG).i("success");
        },
        error -> {
          Timber.tag(TAG).e(error);
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
