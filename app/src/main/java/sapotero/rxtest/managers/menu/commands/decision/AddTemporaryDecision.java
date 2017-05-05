package sapotero.rxtest.managers.menu.commands.decision;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;
import java.util.UUID;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.utils.DecisionConverter;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import timber.log.Timber;

public class AddTemporaryDecision extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private Preference<String> PIN;
  private String decisionId;

  public AddTemporaryDecision(Context context, DocumentReceiver document){
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
    UID   = settings.getString("activity_main_menu.uid");
    HOST  = settings.getString("settings_username_host");
    STATUS_CODE = settings.getString("activity_main_menu.star");
    PIN = settings.getString("PIN");
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
        RBlockEntity block = new RBlockEntity();

        block.setTextBefore(_block.getTextBefore());
        block.setText(_block.getText());
        block.setAppealText(_block.getAppealText());
        block.setNumber(_block.getNumber());


        block.setToCopy(_block.getToCopy());
        block.setHidePerformers(_block.getHidePerformers());
        block.setToFamiliarization(_block.getToFamiliarization());


        for (Performer _perf: _block.getPerformers()) {
          RPerformerEntity perf = new RPerformerEntity();

          perf.setNumber( _perf.getNumber() );
          perf.setPerformerText( _perf.getPerformerText() );
          perf.setPerformerGender( _perf.getPerformerGender() );
          perf.setOrganizationText( _perf.getOrganizationText() );
          perf.setIsOriginal( _perf.getIsOriginal() );
          perf.setIsResponsible( _perf.getIsResponsible() );
          perf.setIsResponsible( _perf.getIsResponsible() );

          perf.setBlock(block);
          block.getPerformers().add(perf);
        }

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
