package sapotero.rxtest.utils.click;


import android.os.SystemClock;

import sapotero.rxtest.utils.ISettings;

// Prevents multiple clicks on different views
public class ClickTime {
  private static final int MIN_TIME_BETWEEN_CLICKS = 1000;

  public static boolean passed(ISettings settings) {
    long lastClickTime = settings.getLastClickTime();
    return SystemClock.elapsedRealtime() - lastClickTime >= MIN_TIME_BETWEEN_CLICKS;
  }

  public static void save(ISettings settings) {
    settings.setLastClickTime( SystemClock.elapsedRealtime() );
  }
}
