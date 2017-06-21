package sapotero.rxtest.managers.menu.commands.approval;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.ApprovalSigningCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.OperationResult;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import timber.log.Timber;

public class ChangePerson extends ApprovalSigningCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String official_id;

  public ChangePerson(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public ChangePerson withPerson(String uid){
    official_id = uid;
    return this;
  }

  @Override
  public void execute() {
    queueManager.add(this);

    store.process(
      store.startTransactionFor( getUid() )
        .setLabel(LabelType.SYNC)
        .setField(FieldType.PROCESSED, true)
        .setState(InMemoryState.LOADING)
    );

    EventBus.getDefault().post( new ShowNextDocumentEvent());
  }

  private String getUid() {
    return params.getDocument() != null ? params.getDocument() : settings.getUid();
  }

  @Override
  public String getType() {
    return "change_person";
  }

  @Override
  public void executeLocal() {
    String uid = getUid();

    int count = dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.MD5, "" )
      .set( RDocumentEntity.PROCESSED, true)
      .where(RDocumentEntity.UID.eq(uid))
      .get()
      .value();

    queueManager.setExecutedLocal(this);

//    store.setLabel(LabelType.SYNC ,uid);
//    store.setField(FieldType.PROCESSED ,true ,uid);

    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );
    remoteOperation(getUid(), official_id, TAG);
  }

  @Override
  protected void onRemoteSuccess() {
  }
}
