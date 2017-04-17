package sapotero.rxtest.db.requery.utils;

import java.util.Arrays;

import padeg.lib.Padeg;
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
    Timber.tag("DecisionConverter").e("formatName %s", name);
    try {
      String[] split = name.split(" ");

      Timber.tag("DecisionConverter").e("split %s", Arrays.asList(split));

      if (split.length >= 1 ){
        name = String.format("%s %s", split[1], split[0] );
      }
    } catch (Exception error) {
      Timber.tag("DecisionConverter").e(error);
    }

    return name;
  }

  public static String formatTemporaryName(String name){
    Timber.tag("DecisionConverter").e("formatTemporaryName %s", name);

    try {
      // Delete organization (text within "()")from name, if exists
      int startIndex = name.indexOf("(");
      if (startIndex != -1) {
        String toBeReplaced = name.substring(startIndex);
        name = name.replace(toBeReplaced, "");
      }

      name = formatName(name);

    } catch (Exception error) {
      Timber.tag("DecisionConverter").e(error);
    }

    return name;
  }

  public static String getPerformerNameForDecisionPreview(String name, String genderString, String appealText) {

    String tempName = "";

    if (name != null) {
      tempName = name;
    }

    boolean forAcquaint = false;

    if ( appealText != null && appealText.contains("озн") ) {
      forAcquaint = true;
    }

    // true - мужской, false - женский
    boolean gender = true;
    // true - склонять, false - не склонять
    boolean decl = false;

    if ( genderString != null && !genderString.equals("") ) {
      // пол указан
      if ( genderString.toLowerCase().trim().startsWith("м") ) {
        // мужской
        gender = true;
        decl = true;
      } else if ( genderString.toLowerCase().trim().startsWith("ж") ) {
        // женский
        gender = false;
        decl = true;
      } else {
        // пол неизвестен, не склонять
        decl = false;
      }
    } else {
      // пол не указан, не склонять
      decl = false;
    }

    // 1 - именительный падеж
    // 2 - родительный падеж
    // 3 - дательный падеж
    // 4 - винительный падеж
    // 5 - творительный падеж
    // 6 - предложный падеж

    if (decl) {
      if (forAcquaint) {
        // Если Прошу ознакомить, то винительный падеж
        tempName = Padeg.getFIOPadegFS(tempName, gender, 4);
      } else {
        // По умолчанию и если Прошу доложить, то дательный падеж
        tempName = Padeg.getFIOPadegFS(tempName, gender, 3);
      }
    }

    tempName = formatName( tempName );

    return tempName;
  }
}
