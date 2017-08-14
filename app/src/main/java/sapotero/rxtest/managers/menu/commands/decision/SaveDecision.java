package sapotero.rxtest.managers.menu.commands.decision;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.document.ForceUpdateDocumentEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import timber.log.Timber;

public class SaveDecision extends DecisionCommand {

  public SaveDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    setChangedInDb();

    update();

    setSyncLabelInMemory();
    setAsProcessed();
  }

  @Override
  public String getType() {
    return "save_decision";
  }

  @Override
  public void executeLocal() {
    sendSuccessCallback();
    queueManager.setExecutedLocal(this);
  }

  private void update() {
    Decision dec = getParams().getDecisionModel();
    Timber.tag(TAG).e("UPDATE %s", new Gson().toJson(dec));

    RDecisionEntity decision = dataStore
      .select(RDecisionEntity.class)
      .where(RDecisionEntity.UID.eq(dec.getId()))
      .get().firstOrNull();

    decision.setTemporary(true);

    if (dec.getUrgencyText() != null) {
      decision.setUrgencyText(dec.getUrgencyText());
    }

    decision.setComment(dec.getComment());
    decision.setDate( dec.getDate());
    decision.setSigner( dec.getSigner() );
    decision.setSignerBlankText(dec.getSignerBlankText());
    decision.setSignerId(dec.getSignerId());
    decision.setSignerPositionS(dec.getSignerPositionS());
    decision.setTemporary(true);
    decision.setApproved(dec.getApproved());
    decision.setChanged(true);
    decision.setRed(dec.getRed());

    if (dec.getBlocks().size() > 0) {
      decision.getBlocks().clear();
    }

    BlockMapper blockMapper = mappers.getBlockMapper();

    for (Block _block : dec.getBlocks()) {
      RBlockEntity block = blockMapper.toEntity(_block);
      block.setDecision(decision);
      decision.getBlocks().add(block);
    }

    dataStore
      .update(decision)
      .toObservable()
      .observeOn(Schedulers.io())
      .subscribeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.tag(TAG).e("UPDATED %s", data.getSigner() );
          EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( data.getUid() ));
          queueManager.add(this);
        },
        error -> {
          queueManager.setExecutedWithError(this, Collections.singletonList("db_error"));
        }
      );

    Timber.tag(TAG).e("1 updateFromJob params%s", new Gson().toJson( params ));
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Decision _decision = getParams().getDecisionModel();
    _decision.setDocumentUid( null );

    Observable<DecisionError> info = getDecisionUpdateOperationObservable(_decision);
    sendDecisionOperationRequest( info );
  }

  @Override
  public void finishOnDecisionSuccess(DecisionError data) {
    finishOperationOnSuccess();
    checkCreatorAndSignerIsCurrentUser(data);
    EventBus.getDefault().post( new UpdateDocumentEvent( data.getDocumentUid() ));
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    finishOperationOnError( errors );
    EventBus.getDefault().post( new ForceUpdateDocumentEvent( getParams().getDocument() ));
  }
}
