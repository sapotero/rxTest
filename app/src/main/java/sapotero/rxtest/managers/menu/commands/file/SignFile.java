package sapotero.rxtest.managers.menu.commands.file;

import java.io.File;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.queue.FileSignEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.ImagesService;
import sapotero.rxtest.services.MainService;
import timber.log.Timber;

public class SignFile extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String official_id;
  private String sign;

  public SignFile(DocumentReceiver document){
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
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "file_sign";
  }

  @Override
  public void executeLocal() {

    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl( settings.getHost() )
      .client( okHttpClient )
      .build();

    ImagesService imagesService = retrofit.create( ImagesService.class );

    File file = new File( EsdApplication.getApplication().getApplicationContext().getFilesDir(), params.getFilePath() );

    String file_sign = null;
    try {
      file_sign = MainService.getFakeSign( settings.getPin(), file );
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (file_sign != null) {
      Observable<Object> info = imagesService.update(
        getParams().getImageId(),
        settings.getLogin(),
        settings.getToken(),
        file_sign
      );

      String finalFile_sign = file_sign;
      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            Timber.tag(TAG).i("signed: %s", data);
            queueManager.setExecutedRemote(this);

            addSigned(finalFile_sign);
          },
          error -> {
            if (callback != null) {
              callback.onCommandExecuteError(getType());
            }

          }
        );
    }

  }
  private void addSigned(String sign){
    FileSignEntity task = new FileSignEntity();
    task.setFilename( params.getLabel() );
    task.setImageId(  params.getImageId() );
    task.setDocumentId( params.getDocument() );
    task.setSign( sign );

    dataStore
      .insert(task)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .subscribeOn(Schedulers.computation())
      .subscribe(data -> {
        Timber.tag(TAG).v("inserted %s [ %s ]", data.getImageId(), data.getDocumentId() );
      },
        Timber::e);
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
