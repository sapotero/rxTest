package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import timber.log.Timber;

public class SaveTemporaryDecision extends AbstractCommand {

  private final DocumentReceiver document;

  private String TAG = this.getClass().getSimpleName();

  public SaveTemporaryDecision(DocumentReceiver document){
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
    Timber.tag(TAG).e("execute %s", params);
//    update();
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "save_temporary_decision";
  }

  @Override
  public void executeLocal() {
    Timber.tag(TAG).e("executeLocal %s", params);
    queueManager.setExecutedLocal(this);
  }

  private void update() {
    Decision dec = params.getDecisionModel();

    Timber.tag(TAG).e("UPDATE %s", new Gson().toJson(dec));

    RDecisionEntity decision = dataStore
      .select(RDecisionEntity.class)
      .where(RDecisionEntity.UID.eq(dec.getId()))
      .get()
      .firstOrNull();



    if (decision != null) {

      decision.setTemporary(true);

      if (dec.getUrgencyText() != null) {
        decision.setUrgencyText(dec.getUrgencyText());
      }
      decision.setComment(dec.getComment() + " ");
      decision.setDate( dec.getDate());
      decision.setSigner(dec.getSigner() + " ");
      decision.setSignerId(dec.getSignerId());
      decision.setSignerPositionS(dec.getSignerPositionS());
      decision.setSignerBlankText(dec.getSignerText() + " ");
      decision.setTemporary(true);
      decision.setApproved(dec.getApproved());
      decision.setChanged(true);
      decision.setRed(dec.getRed());
      decision.setPerformerFontSize(dec.getPerformersFontSize());
      decision.setLetterheadFontSize(dec.getLetterheadFontSize());

      if (dec.getBlocks().size() > 0) {
        decision.getBlocks().clear();
      }

      for (Block _block : dec.getBlocks()) {
        RBlockEntity block = new RBlockEntity();

        block.setTextBefore(_block.getTextBefore());
        block.setText(_block.getText());
        block.setAppealText(_block.getAppealText());
        block.setNumber(_block.getNumber());
        block.setFontSize(_block.getFontSize());


        block.setToCopy(_block.getToCopy());
        block.setHidePerformers(_block.getHidePerformers());
        block.setToFamiliarization(_block.getToFamiliarization());


        for (Performer _perf : _block.getPerformers()) {
          RPerformerEntity perf = new RPerformerEntity();

          perf.setNumber(_perf.getNumber());
          perf.setPerformerText(_perf.getPerformerText());
          perf.setPerformerGender(_perf.getPerformerGender());
          perf.setOrganizationText(_perf.getOrganizationText());
          perf.setIsOriginal(_perf.getIsOriginal());
          perf.setIsResponsible(_perf.getIsResponsible());
          perf.setIsResponsible(_perf.getIsResponsible());

          perf.setBlock(block);
          block.getPerformers().add(perf);
        }

        block.setDecision(decision);
        decision.getBlocks().add(block);
      }

      dataStore
        .update(decision)
        .toObservable()
        .observeOn(Schedulers.computation())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(
          data -> {
            Timber.tag(TAG).e("UPDATED %s", dec.getBlocks().get(0).getText() );
            EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( params.getDecisionModel().getId() ));
            queueManager.setExecutedRemote(this);
          },
          error -> {
//            queueManager.setExecutedWithError(this, Collections.singletonList("db_error"));
          }
        );
    }
  }

  @Override
  public void executeRemote() {
//    queueManager.setExecutedRemote(this);
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
