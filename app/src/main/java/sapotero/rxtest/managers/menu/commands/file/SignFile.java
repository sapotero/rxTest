package sapotero.rxtest.managers.menu.commands.file;

import java.util.Collections;
import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RSignImageEntity;
import sapotero.rxtest.db.requery.models.queue.FileSignEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import sapotero.rxtest.retrofit.ImagesService;
import sapotero.rxtest.utils.memory.fields.FieldType;
import timber.log.Timber;

public class SignFile extends AbstractCommand {

  public SignFile(CommandParams params) {
    super(params);
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
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
    setAsProcessed();
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).d("Starting executeRemote");
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    RSignImageEntity signImage = getSignImage( getParams().getImageId() );

    if ( signImage == null ) {
      Timber.tag(TAG).d("signImage == null, quit");
      return;
    }

    if ( signImage.isSigned() != null && signImage.isSigned() ) {
      Timber.tag(TAG).d("Image already signed, quit and remove from queue");
      queueManager.setExecutedRemote(this);
      return;
    }

    if ( signImage.isSignTaskStarted() != null && signImage.isSignTaskStarted() ) {
      Timber.tag(TAG).d("Sign task already started, quit");
      return;
    }

    setSignTaskStarted( getParams().getImageId(), true );

    Retrofit retrofit = getRetrofit();

    ImagesService imagesService = retrofit.create( ImagesService.class );

    Timber.tag(TAG).d("Generating sign");


    String file_sign = getSign();

    if (file_sign != null) {
      Timber.tag(TAG).d("Sign generated");

      RequestBody signBody = getSignBody(file_sign);

      Observable<Object> info = imagesService.update(
        getParams().getImageId(),
        getParams().getLogin(),
        settings.getToken(),
        signBody
      );

      Timber.tag(TAG).d("Sending image sign request");

      info.subscribeOn( Schedulers.computation() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            Timber.tag(TAG).i("signed: %s", data);
            queueManager.setExecutedRemote(this);
            setSignSuccess( getParams().getImageId() );
            saveImageSign( file_sign );
            setUpdatedAt();
          },
          error -> onError()
        );

    } else {
      onError();
    }
  }

  private void onError() {
    Timber.tag(TAG).i("Sign error");

    sendErrorCallback( getType() );

    if ( settings.isOnline() ) {
      String errorMessage = "Ошибка подписания электронного образа";
      queueManager.setExecutedWithError( this,  Collections.singletonList( errorMessage ) );
      setSignError( getParams().getImageId() );
    }

    setSignTaskStarted( getParams().getImageId(), false );
  }

  private void saveImageSign(String sign){
    Timber.tag(TAG).i("Saving image sign");

    FileSignEntity task = new FileSignEntity();
    task.setFilename( getParams().getLabel() );
    task.setImageId( getParams().getImageId() );
    task.setDocumentId( getParams().getDocument() );
    task.setSign( sign );

    dataStore
      .insert(task)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .subscribeOn(Schedulers.computation())
      .subscribe(
        data -> Timber.tag(TAG).v("Saved image sign %s [ %s ]", data.getImageId(), data.getDocumentId() ),
        Timber::e
      );
  }

  private void setUpdatedAt() {
    store.process(
      store.startTransactionFor( getParams().getDocument() )
        .setField(FieldType.UPDATED_AT, DateUtil.getTimestamp())
    );

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.UPDATED_AT, DateUtil.getTimestamp())
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();
  }

  private void setSignSuccess(String imageId) {
    dataStore
      .update(RSignImageEntity.class)
      .set( RSignImageEntity.SIGNED, true )
      .set( RSignImageEntity.SIGNING, false )
      .set( RSignImageEntity.ERROR, false )
      .where( RSignImageEntity.IMAGE_ID.eq( imageId ) )
      .get()
      .value();


    Timber.tag(TAG).i("Set sign success");
  }

  private void setSignError(String imageId) {
    dataStore
      .update(RSignImageEntity.class)
      .set( RSignImageEntity.ERROR, true )
      .set( RSignImageEntity.SIGNING, false )
      .where( RSignImageEntity.IMAGE_ID.eq( imageId ) )
      .get()
      .value();

    Timber.tag(TAG).i("Set sign error");
  }

  private void setSignTaskStarted(String imageId, boolean value) {
    dataStore
      .update(RSignImageEntity.class)
      .set( RSignImageEntity.SIGN_TASK_STARTED, value )
      .where( RSignImageEntity.IMAGE_ID.eq( imageId ) )
      .get()
      .value();

    Timber.tag(TAG).i("Set sign task started = %s", value);
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
  }
}
