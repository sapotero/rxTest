package sapotero.rxtest.utils.load;

public class PercentCalculator {

  public static int calculatePercent(int loaded, int total) {
    float result = 0;

    if (total != 0) {
      result = 100f * loaded / total;

      // TODO: fix this
      if (result > 99.5f) {
        result = 100f;
      }
    }

    return (int) Math.floor(result);
  }
}
