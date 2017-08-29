package sapotero.rxtest.managers.menu.utils;

public class DateUtil {

  public static String getTimestamp(){
    return String.valueOf( System.currentTimeMillis() / 1000L);
  }

  public static String getTimestampEarly(){
    return String.valueOf( System.currentTimeMillis() / 1000L - 6*60);
  }

  public static Boolean isSomeTimePassed(String time){
    Boolean result = false;

    try {
      long currentTime = System.currentTimeMillis()/ 1000L;
      long testTime = Long.valueOf(time);

      result = currentTime > (testTime + 5*60);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }

    return result;
  }

  public static Boolean isSomeTimePassed(String time, int waitTime){
    Boolean result = false;

    try {
      long currentTime = System.currentTimeMillis()/ 1000L;
      long testTime = Long.valueOf(time);

      result = currentTime > (testTime + waitTime);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }

    return result;
  }
}
