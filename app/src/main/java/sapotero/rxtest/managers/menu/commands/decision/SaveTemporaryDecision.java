package sapotero.rxtest.managers.menu.commands.decision;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import timber.log.Timber;

public class SaveTemporaryDecision extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  private String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> UID;
  private Preference<String> HOST;
  private Preference<String> STATUS_CODE;
  private Preference<String> PIN;
  private RDecisionEntity decision;
  private String decisionId;

  public SaveTemporaryDecision(Context context, DocumentReceiver document){
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
  public SaveTemporaryDecision withDecision(RDecisionEntity decision){
    this.decision = decision;
    return this;
  }
  public SaveTemporaryDecision withDecisionId(String decisionId){
    this.decisionId = decisionId;
    return this;
  }

  @Override
  public void execute() {
    queueManager.add(this);
    update();
  }

  @Override
  public String getType() {
    return "save_temporary_decision";
  }

  @Override
  public void executeLocal() {
    queueManager.setExecutedLocal(this);

  }

  private void update() {

    Timber.tag(TAG).e("UPDATE %s", params.getDecisionModel());

    if (params.getDecisionModel() != null) {

      Decision dec = params.getDecisionModel();

      Timber.tag(TAG).e("ID %s", dec.getId());

      RDecisionEntity decision = dataStore
        .select(RDecisionEntity.class)
        .where(RDecisionEntity.UID.eq(dec.getId()))
        .get()
        .firstOrNull();
//
//      if (decision != null) {
//        decision.setUrgencyText(dec.getUrgencyText() + " ");
//        decision.setComment(dec.getComment() + " ");
//        decision.setDate(dec.getDate());
//        decision.setSigner(dec.getSigner() + " ");
//        decision.setSignerId(dec.getSignerId());
//        decision.setSignerPositionS(dec.getSignerPositionS());
//        decision.setSignerBlankText(dec.getSignerText() + " ");
//        decision.setTemporary(true);
//        decision.setApproved(false);
//        decision.setChanged(false);
//        decision.setRed(false);
//        decision.setChanged(false);
//
//        decision.setDate(dec.getDate());
//
//        if (dec.getBlocks().size() > 0) {
//          decision.getBlocks().clear();
//        }
//
//        for (Block _block : dec.getBlocks()) {
//          RBlockEntity block = new RBlockEntity();
//
//          block.setTextBefore(_block.getTextBefore());
//          block.setText(_block.getText());
//          block.setAppealText(_block.getAppealText());
//          block.setNumber(_block.getNumber());
//
//
//          block.setToCopy(_block.getToCopy());
//          block.setHidePerformers(_block.getHidePerformers());
//          block.setToFamiliarization(_block.getToFamiliarization());
//
//
//          for (Performer _perf : _block.getPerformers()) {
//            RPerformerEntity perf = new RPerformerEntity();
//
//            perf.setNumber(_perf.getNumber());
//            perf.setPerformerText(_perf.getPerformerText());
//            perf.setOrganizationText(_perf.getOrganizationText());
//            perf.setIsOriginal(_perf.getIsOriginal());
//            perf.setIsResponsible(_perf.getIsResponsible());
//            perf.setIsResponsible(_perf.getIsResponsible());
//
//            perf.setBlock(block);
//            block.getPerformers().add(perf);
//          }
//
//          block.setDecision(decision);
//          decision.getBlocks().add(block);
//        }

        dataStore
          .update(RDecisionEntity.class)
          .set(RDecisionEntity.TEMPORARY, true)
          .where(RDecisionEntity.UID.eq(params.getDecisionId()))
          .get();
      EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( params.getDecisionModel().getId() ));
//          .toObservable()
//          .subscribeOn(Schedulers.computation())
//          .observeOn(AndroidSchedulers.mainThread())
//          .subscribe(
//            data -> {
//              Timber.tag(TAG).e("Updated: %s", data.getId());
//              EventBus.getDefault().post(new UpdateCurrentDocumentEvent(params.getDocument()));
//            },
//            error -> {
//              Timber.tag(TAG).e("Error: %s", error);
//            });
//      }
    }
  }

  @Override
  public void executeRemote() {
    loadSettings();
    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }
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
