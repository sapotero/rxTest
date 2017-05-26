package sapotero.rxtest.managers.menu.commands.decision;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;
import java.util.UUID;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.DecisionConverter;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import timber.log.Timber;

public class AddTemporaryDecision extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  private String decisionId;

  public AddTemporaryDecision(DocumentReceiver document){
    super();
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public AddTemporaryDecision withDecisionId(String decisionId){
    this.decisionId = decisionId;
    return this;
  }

  @Override
  public void execute() {
    addDecision();
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "add_temporary_decision";
  }

  @Override
  public void executeLocal() {
    queueManager.setExecutedLocal(this);
  }

  private void addDecision() {

    String uid = null;

    if (params.getDocument() != null && !Objects.equals(params.getDocument(), "")){
      uid = params.getDocument();
    }

    if (document.getUid() != null && !Objects.equals(document.getUid(), "")){
      uid = document.getUid();
    }

    Timber.tag(TAG).e("DOCUMENT_UID: %s", uid);

    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().firstOrNull();

    Timber.tag(TAG).e("doc: %s", doc);

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .set(RDocumentEntity.MD5, "")
      .where(RDocumentEntity.UID.eq( uid ))
      .get()
      .value();



    Decision dec = params.getDecisionModel();

    Timber.tag(TAG).e("dec: %s", dec);

    if (dec != null && doc != null) {

      RDecisionEntity decision = new RDecisionEntity();
      decision.setUid(UUID.randomUUID().toString() );
      decision.setUrgencyText( dec.getUrgencyText() );
      decision.setComment( dec.getComment()+" " );
      decision.setDate( dec.getDate() );
      decision.setLetterhead( "Бланк резолюции" );
      decision.setShowPosition( false );
      decision.setSignBase64( null );
      decision.setSigner( dec.getSigner() );
      decision.setSignerId( dec.getSignerId() );
      decision.setSignerPositionS( dec.getSignerPositionS() );

      String name = null;

      try {

        name = DecisionConverter.formatName(dec.getSignerText());
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (name != null) {
        decision.setSignerBlankText( name );
      } else {
        decision.setSignerBlankText( dec.getSignerText() );
      }


      decision.setTemporary( true );
      decision.setApproved( false );
      decision.setChanged( false );
      decision.setRed( false );
      decision.setAssistantId( null );
      decision.setChanged( false );

      decision.setDate( dec.getDate() );

      for (Block _block: dec.getBlocks()) {
        RBlockEntity block = mappers.getBlockMapper().toEntity(_block);
        block.setDecision(decision);
        decision.getBlocks().add(block);
      }

      doc.getDecisions().add(decision);

      dataStore
        .update(doc)
        .toObservable()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          data -> {
            Timber.tag(TAG).e("Updated: %s", data.getId());
//            EventBus.getDefault().post( new UpdateCurrentDocumentEvent( params.getDocument() ));
            EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( params.getDecisionModel().getId() ));
          },
          error -> {
            Timber.tag(TAG).e("Error: %s", error);
          });
    }
  }

  @Override
  public void executeRemote() {
   queueManager.setExecutedRemote(this);
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
