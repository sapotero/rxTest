package sapotero.rxtest.utils.validators;

public class IntegerValidator {

  public static boolean isInt(String value) {
    try {
      java.lang.Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

}
