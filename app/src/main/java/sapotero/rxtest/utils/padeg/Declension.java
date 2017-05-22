package sapotero.rxtest.utils.padeg;

import padeg.lib.Padeg;
import sapotero.rxtest.db.requery.utils.DecisionConverter;

// Russian names declension
public class Declension {

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

    tempName = DecisionConverter.formatName( tempName );

    return tempName;
  }
}
