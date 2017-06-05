package sapotero.rxtest.utils.padeg;

import java.util.Arrays;

import padeg.lib.Padeg;
import timber.log.Timber;

// Russian names declension
public class Declension {

  public static String formatName(String name){
    Timber.tag("DecisionConverter").e("formatName %s", name);
    try {
      String[] split = name.split(" ");

      Timber.tag("DecisionConverter").e("split %s", Arrays.asList(split));

      if ( split.length >= 2 ){
        String part1 = split[0];
        String part2 = split[1];

        if (part2 != null && part2.contains(".")) {
          String temp = part1;
          part1 = part2;
          part2 = temp;
          name = String.format("%s %s", part1, part2);
        }
      }
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
      try {
        if (forAcquaint) {
          // Если Прошу ознакомить, то винительный падеж
          tempName = Padeg.getFIOPadegFS(tempName, gender, 4);
        } else {
          // По умолчанию и если Прошу доложить, то дательный падеж
          tempName = Padeg.getFIOPadegFS(tempName, gender, 3);
        }
      } catch (Exception error) {
        Timber.tag("Declension").e(error);
      }
    }

    tempName = formatName( tempName );

    return tempName;
  }
}
