package sapotero.rxtest.managers.menu.commands.signing;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Set;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.view.ShowPrevDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.services.MainService;
import timber.log.Timber;

public class NextPerson extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private Preference<String> PIN;
  private String official_id;
  private String sign;

  public NextPerson(Context context, DocumentReceiver document){
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
    TOKEN = settings.getString("token");
    HOST  = settings.getString("settings_username_host");
    STATUS_CODE = settings.getString("activity_main_menu.star");
    PIN = settings.getString("PIN");
  }
  public NextPerson withPerson(String uid){
    this.official_id = uid;
    return this;
  }
  public NextPerson withSign(String sign){
    this.sign = sign;
    return this;
  }


  @Override
  public void execute() {
    queueManager.add(this);
    EventBus.getDefault().post( new ShowPrevDocumentEvent());
  }


  @Override
  public String getType() {
    return "next_person";
  }

  @Override
  public void executeLocal() {
    loadSettings();
    int count = dataStore
      .update(RDocumentEntity.class)
//      .set( RDocumentEntity.FILTER, Fields.Status.PROCESSED.getValue() )
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(params.getDocument() != null ? params.getDocument(): document.getUid()))
      .get()
      .value();

    if (callback != null){
      callback.onCommandExecuteSuccess(getType());
    }

    queueManager.setExecutedLocal(this);
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
    uids.add( params.getDocument() != null ? params.getDocument(): document.getUid() );


    String comment = null;
    if ( params.getComment() != null ){
      comment = params.getComment();
    }

    try {
      sign = MainService.getFakeSign( context, PIN.get(), null );
    } catch (Exception e) {
      e.printStackTrace();
    }

    Observable<OperationResult> info = operationService.sign(
      getType(),
      LOGIN.get(),
      TOKEN.get(),
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

          addImageSignTask();

          queueManager.setExecutedRemote(this);
        },
        error -> {
          if (callback != null ){
            callback.onCommandExecuteError(getType());
          }


        }
      );
  }

  private void addImageSignTask(){
    Timber.tag(TAG).e("addImageSignTask");
    RDocumentEntity doc = getDocument(document.getUid());

    Timber.tag(TAG).e("doc: %s", doc);

    if (doc != null) {

      Set<RImage> images = doc.getImages();
      Timber.tag(TAG).e("images: %s", images);


      if (images != null && images.size() > 0) {
        for (RImage img: images) {
          RImageEntity image = (RImageEntity) img;

          CommandFactory.Operation operation = CommandFactory.Operation.FILE_SIGN;
          CommandParams params = new CommandParams();
          params.setUser( LOGIN.get() );
          params.setDocument( document.getUid() );
          params.setLabel( image.getTitle() );
          params.setFilePath( String.format( "%s_%s", image.getMd5(), image.getTitle()) );
          params.setImageId( image.getImageId() );


          Command command = operation.getCommand(null, context, document, params);

          Timber.tag(TAG).e("image: %s", document.getUid());
          queueManager.add(command);
        }
      }
      queueManager.setExecutedRemote(this);

    }
  }

  @Override
  public void withParams(CommandParams params) {
    this.params = params;
  }

  @Override
  public CommandParams getParams() {
    return params;
  }

  private RDocumentEntity getDocument(String uid){
    return dataStore.select(RDocumentEntity.class).where(RDocumentEntity.UID.eq(uid)).get().firstOrNull();
  }
}
