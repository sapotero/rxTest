package sapotero.rxtest.utils.click;


import android.os.SystemClock;

import sapotero.rxtest.utils.ISettings;

// Prevents multiple clicks on different views
public class ClickTime {
  public static final int TIME_BETWEEN_NEXT_PREV_DOC = 250;
  public static final int TIME_BETWEEN_NEXT_PREV_IMAGE = 250;

  private static final int MIN_TIME_BETWEEN_CLICKS = 1000;

  public static boolean passed(ISettings settings) {
    long lastClickTime = settings.getLastClickTime();
    return SystemClock.elapsedRealtime() - lastClickTime >= MIN_TIME_BETWEEN_CLICKS;
  }

  public static boolean passed(ISettings settings, int timeBetweenClicks) {
    long lastClickTime = settings.getLastClickTime();
    return SystemClock.elapsedRealtime() - lastClickTime >= timeBetweenClicks;
  }

  public static void save(ISettings settings) {
    settings.setLastClickTime( SystemClock.elapsedRealtime() );
  }
}
