package sapotero.rxtest.db.requery.utils;

import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import timber.log.Timber;


public class DecisionConverter {

  public static Decision formatDecision(RDecisionEntity decision) {
    Decision formated_decision = new Decision();

    formated_decision.setId( decision.getUid() );
    formated_decision.setLetterhead( decision.getLetterhead() );
    formated_decision.setApproved( decision.isApproved() );
    formated_decision.setSigner( decision.getSigner() );
    formated_decision.setSignerId( decision.getSignerId() );
    formated_decision.setSignerBlankText( decision.getSignerBlankText() );
    formated_decision.setSignerPositionS( decision.getSignerPositionS() );
    formated_decision.setSignerIsManager( decision.isSignerIsManager() );
    formated_decision.setSignBase64( decision.getSignBase64() );
    formated_decision.setAssistantId( decision.getAssistantId() );
    formated_decision.setComment( decision.getComment() );
    formated_decision.setDate( decision.getDate() );
    formated_decision.setUrgencyText( decision.getUrgencyText() );
    formated_decision.setShowPosition( decision.isShowPosition() );

    if ( decision.getBlocks().size() > 0 ){
      for (RBlock _b:decision.getBlocks() ) {
        RBlockEntity block = (RBlockEntity) _b;
        Block formated_block = new Block();
        formated_block.setNumber( block.getNumber() );
        formated_block.setText( block.getText() );
        formated_block.setAppealText( block.getAppealText() );
        formated_block.setTextBefore( block.isTextBefore() );
        formated_block.setHidePerformers( block.isHidePerformers() );
        formated_block.setIndentation( "1" );
        formated_block.setFontSize( "14" );

        if ( block.getPerformers().size() > 0 ) {
          for (RPerformer _p : block.getPerformers()) {
            RPerformerEntity performer = (RPerformerEntity) _p;
            Performer formated_performer = new Performer();

            formated_performer.setPerformerId( performer.getPerformerId() );
            formated_performer.setIsOriginal( performer.isIsOriginal() );
            formated_performer.setIsResponsible( performer.isIsResponsible() );
            formated_performer.setGroup( false );
            formated_performer.setOrganization( false );

            formated_block.getPerformers().add(formated_performer);
          }
        }

        formated_decision.getBlocks().add(formated_block);

      }
    }

    return formated_decision;
  }

  public static String formatName(String name){

    try {
      String[] split = name.split(" ");

      if (split.length >= 1 ){
        name = String.format("%s %s", split[1], split[0] );
      }
    } catch (Exception error) {
      Timber.tag("DecisionConverter").e(error);
    }

    return name;
  }
}
