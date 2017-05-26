package sapotero.rxtest.db.requery.utils;

import java.util.Arrays;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.retrofit.models.document.Decision;
import timber.log.Timber;

@Deprecated
public class DecisionConverter {

  public static Decision formatDecision(RDecisionEntity decision) {
    Decision formattedDecision = Mappers.getMappers().getDecisionMapper().toFormattedModel(decision);
    return formattedDecision;
  }

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
}
