package sapotero.rxtest.managers.menu.commands.signing;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.retrofit.ImagesService;
import timber.log.Timber;

public class NextPerson extends ApprovalSigningCommand {

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
    queueManager.add(this);
    EventBus.getDefault().post( new ShowNextDocumentEvent());

    setDocOperationProcessedStartedInMemory( getUid() );
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

    boolean isImagesSigned = signImages();

    if ( isImagesSigned ) {
      remoteOperation(getUid(), official_id, TAG);

    } else {
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

  // True if images signed successfully
  private boolean signImages() {
    Timber.tag(TAG).e("Signing images");
    RDocumentEntity doc = getDocument(document.getUid());

    Timber.tag(TAG).e("doc: %s", doc);

    if (doc != null) {

      Set<RImage> images = doc.getImages();
      Timber.tag(TAG).e("images: %s", images);

      if (notEmpty(images)) {
        Retrofit retrofit = getRetrofit();

        for (RImage img : images) {
          RImageEntity image = (RImageEntity) img;

          Timber.tag(TAG).e("image: %s", document.getUid());

          String file_sign = getSign();

          ImagesService imagesService = retrofit.create(ImagesService.class);
          Call<Object> call = imagesService.updateNonRx(
            image.getImageId(),
            settings.getLogin(),
            settings.getToken(),
            file_sign
          );

          try {
            Response<Object> response = call.execute();

            if ( !response.isSuccessful() ) {
              return false;
            }

            Timber.tag(TAG).i("Signed image %s", image.getImageId() );

            saveImageSign( image.getTitle(), image.getImageId(), document.getUid(), file_sign, TAG );

          } catch (IOException e) {
            return false;
          }
        }
      }
    }

    return true;
  }

  private RDocumentEntity getDocument(String uid){
    return dataStore.select(RDocumentEntity.class).where(RDocumentEntity.UID.eq(uid)).get().firstOrNull();
  }
}
