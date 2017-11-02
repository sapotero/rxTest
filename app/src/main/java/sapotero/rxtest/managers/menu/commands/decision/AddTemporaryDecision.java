package sapotero.rxtest.managers.menu.commands.decision;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import sapotero.rxtest.utils.padeg.Declension;
import timber.log.Timber;

public class AddTemporaryDecision extends DecisionCommand {

  public AddTemporaryDecision(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "add_temporary_decision";
  }

  @Override
  public void executeLocal() {
    addDecision();
    addToQueue();

    setSyncLabelInMemory();
    setAsProcessed();

    queueManager.setExecutedLocal(this);
  }

  private void addDecision() {
    getParams().getDecisionModel().setTemporary( true );
    updateInMemory();

    String uid = getParams().getDocument();

    Timber.tag(TAG).e("DOCUMENT_UID: %s", uid);

    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get().firstOrNull();

    Timber.tag(TAG).e("doc: %s", doc);

    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    setChangedInDb();

    Decision dec = getParams().getDecisionModel();

    Timber.tag(TAG).e("dec: %s", dec);

    if (dec != null && doc != null) {

      RDecisionEntity decision = new RDecisionEntity();
      decision.setUid( dec.getId() );
      decision.setUrgencyText( dec.getUrgencyText() );

      if (dec.getComment() != null & !Objects.equals(dec.getComment(), "")){
        decision.setComment( dec.getComment());
      } else {
        decision.setComment("");
      }

      decision.setDate( dec.getDate() );
      decision.setLetterhead( "Бланк резолюции" );
      decision.setShowPosition( false );
      decision.setSignBase64( null );
      decision.setSigner( dec.getSigner() );
      decision.setSignerId( dec.getSignerId() );
      decision.setSignerPositionS( dec.getSignerPositionS() );

      String name = null;

      try {

        name = Declension.formatName(dec.getSignerText());
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (name != null) {
        decision.setSignerBlankText( name );
      } else {
        decision.setSignerBlankText( dec.getSignerText() );
      }

      decision.setApproved( dec.getApproved() );
      decision.setChanged( true );
      decision.setTemporary( true );
      decision.setRed( dec.getRed() );
      decision.setAssistantId( null );

      decision.setDate( dec.getDate() );

      for (Block _block: dec.getBlocks()) {
        RBlockEntity block = new BlockMapper().toEntity(_block);
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
            EventBus.getDefault().post( new InvalidateDecisionSpinnerEvent( getParams().getDecisionModel().getId() ));
          },
          error -> Timber.tag(TAG).e("Error: %s", error));
    }
  }

  @Override
  public void executeRemote() {
   queueManager.setExecutedRemote(this);
  }

  @Override
  public void finishOnDecisionSuccess(DecisionError data) {
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
  }
}
