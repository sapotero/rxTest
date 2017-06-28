package sapotero.rxtest.managers.menu.commands.signing;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Set;

import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.db.requery.models.images.RSignImageEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class NextPerson extends ApprovalSigningCommand {

  private static final int ALL_IMAGES_SIGNED = 0;
  private static final int IMAGE_SIGN_ERROR = 1;
  private static final int NOT_ALL_IMAGES_SIGNED = 2;

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String official_id;
  private String sign;

  public NextPerson(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
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
    setDocOperationProcessedStartedInMemory( getUid() );

    resetSignImageError();

    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent());
  }

  private void resetSignImageError() {
    Timber.tag(TAG).e("Resetting sign image errors");
    RDocumentEntity doc = getDocument( document.getUid() );

    if ( doc != null ) {
      Set<RImage> images = doc.getImages();
      if ( notEmpty( images ) ) {
        for (RImage img : images) {
          RImageEntity image = (RImageEntity) img;
          setSignErrorFalse( image.getImageId() );
        }
      }
    }
  }

  @Override
  public String getType() {
    return "next_person";
  }

  @Override
  public void executeLocal() {
    int count = dataStore
      .update(RDocumentEntity.class)
//      .set( RDocumentEntity.FILTER, Fields.Status.PROCESSED.getValue() )
      .set( RDocumentEntity.PROCESSED, true)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(getUid()))
      .get()
      .value();

    if (callback != null){
      callback.onCommandExecuteSuccess(getType());
    }

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    int result = signImages();

    if ( result == ALL_IMAGES_SIGNED ) {
      remoteOperation(getUid(), official_id, TAG);
    }

    if ( result == IMAGE_SIGN_ERROR ) {
      String errorMessage = "Электронные образы не были подписаны";
      Timber.tag(TAG).i("error: %s", errorMessage);

      if (callback != null){
        callback.onCommandExecuteError( errorMessage );
      }

      finishOperationProcessedOnError( this, getUid(), Collections.singletonList( errorMessage ) );
    }
  }

  private String getUid() {
    return params.getDocument() != null ? params.getDocument(): document.getUid();
  }

  private int signImages() {
    int result = ALL_IMAGES_SIGNED;

    Timber.tag(TAG).e("Signing images");
    RDocumentEntity doc = getDocument(document.getUid());

    Timber.tag(TAG).e("doc: %s", doc);

    if ( doc == null ) {
      return result;
    }

    Set<RImage> images = doc.getImages();
    Timber.tag(TAG).e("images: %s", images);

    if ( !notEmpty( images ) ) {
      return result;
    }

    for (RImage img : images) {
      RImageEntity image = (RImageEntity) img;
      Timber.tag(TAG).e("image: %s", document.getUid());
      boolean isSigned = image.isSigned() != null ? image.isSigned() : false;

      if ( !isSigned ) {
        RSignImageEntity signImage = getSignImage( image.getImageId() );

        if ( signImage == null ) {
          signImage = createNewSignImage( image.getImageId() );
        }

        if ( signImage.isError() ) {
          return IMAGE_SIGN_ERROR;
        }

        if ( !signImage.isSigned() ) {
          result = NOT_ALL_IMAGES_SIGNED;
          if ( !signImage.isSigning() ) {
            Timber.tag(TAG).e("Creating image sign task");
            addImageSignTask( image );
            setSigning( image.getImageId() );
          }
        }

      }
    }

    return result;
  }

  private RSignImageEntity createNewSignImage(String imageId) {
    RSignImageEntity signImage = new RSignImageEntity();
    signImage.setImageId( imageId );
    signImage.setSigned( false );
    signImage.setSigning( false );
    signImage.setSignTaskStarted( false );
    signImage.setError( false );

    dataStore
      .insert(signImage)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .subscribeOn(Schedulers.computation())
      .subscribe(
        data -> Timber.tag(TAG).v( "inserted RSignImage %s", data.getImageId() ),
        Timber::e
      );

    return signImage;
  }

  private void setSigning(String imageId) {
    dataStore
      .update(RSignImageEntity.class)
      .set( RSignImageEntity.SIGNING, true)
      .where( RSignImageEntity.IMAGE_ID.eq( imageId ) )
      .get()
      .value();
  }

  private void addImageSignTask(RImageEntity image) {
    Timber.tag(TAG).e("addImageSignTask");

    CommandFactory.Operation operation = CommandFactory.Operation.FILE_SIGN;
    CommandParams params = new CommandParams();
    params.setUser( settings.getLogin() );
    params.setDocument( document.getUid() );
    params.setLabel( image.getTitle() );
    params.setFilePath( String.format( "%s_%s", image.getMd5(), image.getTitle()) );
    params.setImageId( image.getImageId() );
    Command command = operation.getCommand(null, document, params);
    Timber.tag(TAG).e("image: %s", document.getUid());
    queueManager.add(command);
  }

  private RDocumentEntity getDocument(String uid){
    return dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().firstOrNull();
  }

  private void setSignErrorFalse(String imageId) {
    int count = dataStore
      .update(RSignImageEntity.class)
      .set( RSignImageEntity.ERROR, false )
      .where( RSignImageEntity.IMAGE_ID.eq( imageId ) )
      .get()
      .value();

    Timber.tag(TAG).i("Set sign error false count = %s", count);
  }
}
