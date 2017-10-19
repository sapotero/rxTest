package sapotero.rxtest.utils.click;


import android.os.SystemClock;

import sapotero.rxtest.utils.ISettings;

// Prevents multiple clicks on different views
public class ClickTime extends Click {

  public static boolean passed(ISettings settings) {
    long lastClickTime = settings.getLastClickTime();
    return SystemClock.elapsedRealtime() - lastClickTime >= MIN_TIME_BETWEEN_CLICKS;
  }

  private static boolean passed(ISettings settings, int timeBetweenClicks) {
    long lastClickTime = settings.getLastClickTime();
    return SystemClock.elapsedRealtime() - lastClickTime >= timeBetweenClicks;
  }

  public static void save(ISettings settings) {
    settings.setLastClickTime( SystemClock.elapsedRealtime() );
  }

  public static void click(ISettings settings, OnClickListener onClickListener) {
    if ( passed( settings ) ) {
      save( settings );
      if ( onClickListener != null ) {
        onClickListener.onClick();
      }
    }
  }

  public static void click(ISettings settings, int timeBetweenClicks, OnClickListener onClickListener) {
    if ( passed( settings, timeBetweenClicks ) ) {
      save( settings );
      if ( onClickListener != null ) {
        onClickListener.onClick();
      }
    }
  }
}
