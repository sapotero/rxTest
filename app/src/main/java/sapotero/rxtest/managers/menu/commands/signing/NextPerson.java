package sapotero.rxtest.managers.menu.commands.signing;

import org.greenrobot.eventbus.EventBus;

import java.util.Set;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
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
    store.process(
      store.startTransactionFor( getUid() )
        .setLabel(LabelType.SYNC)
        .setField(FieldType.PROCESSED, true)
        .setField(FieldType.MD5, "")
        .setState(InMemoryState.LOADING)
    );

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
    remoteOperation(getUid(), official_id, TAG);
  }

  @Override
  protected void onRemoteSuccess() {
    addImageSignTask();
  }

  private String getUid() {
    return params.getDocument() != null ? params.getDocument(): document.getUid();
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
          params.setUser( settings.getLogin() );
          params.setDocument( document.getUid() );
          params.setLabel( image.getTitle() );
          params.setFilePath( String.format( "%s_%s", image.getMd5(), image.getTitle()) );
          params.setImageId( image.getImageId() );


          Command command = operation.getCommand(null, document, params);

          Timber.tag(TAG).e("image: %s", document.getUid());
          queueManager.add(command);
        }
      }
      queueManager.setExecutedRemote(this);

    }
  }

  private RDocumentEntity getDocument(String uid){
    return dataStore.select(RDocumentEntity.class).where(RDocumentEntity.UID.eq(uid)).get().firstOrNull();
  }
}
