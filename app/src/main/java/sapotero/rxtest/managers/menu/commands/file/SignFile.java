package sapotero.rxtest.managers.menu.commands.file;

import java.io.File;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.queue.FileSignEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.ImagesService;
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

    Retrofit retrofit = getRetrofit();

    ImagesService imagesService = retrofit.create( ImagesService.class );

    File file = new File( EsdApplication.getApplication().getApplicationContext().getFilesDir(), params.getFilePath() );

    String file_sign = getSign();

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

            addSigned(params.getLabel(), params.getImageId(), params.getDocument(), finalFile_sign, TAG);
          },
          error -> {
            if (callback != null) {
              callback.onCommandExecuteError(getType());
            }

          }
        );
    }

  }
}
