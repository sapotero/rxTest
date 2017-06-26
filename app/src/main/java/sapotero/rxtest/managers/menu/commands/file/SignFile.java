package sapotero.rxtest.managers.menu.commands.file;

import java.io.File;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.images.RSignImageEntity;
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

      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            Timber.tag(TAG).i("signed: %s", data);
            queueManager.setExecutedRemote(this);
            setSignSuccess( getParams().getImageId() );
            saveImageSign( file_sign );
          },
          error -> {
            if (callback != null) {
              callback.onCommandExecuteError(getType());
            }

            if ( settings.isOnline() ) {
              setSignError( getParams().getImageId() );
            }
          }
        );
    }
  }

  private void saveImageSign(String sign){
    FileSignEntity task = new FileSignEntity();
    task.setFilename( params.getLabel() );
    task.setImageId( params.getImageId() );
    task.setDocumentId( params.getDocument() );
    task.setSign( sign );

    dataStore
      .insert(task)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .subscribeOn(Schedulers.computation())
      .subscribe(
        data -> Timber.tag(TAG).v("inserted %s [ %s ]", data.getImageId(), data.getDocumentId() ),
        Timber::e
      );
  }

  private void setSignSuccess(String imageId) {
    dataStore
      .update(RSignImageEntity.class)
      .set( RSignImageEntity.SIGNED, true )
      .set( RSignImageEntity.SIGNING, false )
      .where( RSignImageEntity.IMAGE_ID.eq( imageId ) )
      .get()
      .value();
  }

  private void setSignError(String imageId) {
    dataStore
      .update(RSignImageEntity.class)
      .set( RSignImageEntity.ERROR, true )
      .set( RSignImageEntity.SIGNING, false )
      .where( RSignImageEntity.IMAGE_ID.eq( imageId ) )
      .get()
      .value();
  }
}
