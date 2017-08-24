package sapotero.rxtest.managers.menu.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateUtil {

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.ROOT);

  public static String getTimestamp(){
    return new Timestamp(System.currentTimeMillis()).toString();
  }

  public static Boolean isSomeTimePassed(String time){
    Boolean result = false;

    try {
      long currentTime = System.currentTimeMillis();
      long testTime = Long.valueOf(time);

      result = currentTime > (testTime + 5*60*1000 );
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }

    return result;
  }
}
