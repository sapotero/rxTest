package sapotero.rxtest.utils.click;

import android.view.MenuItem;

public abstract class Click {

  public static final int TIME_BETWEEN_NEXT_PREV_DOC = 250;
  public static final int TIME_BETWEEN_NEXT_PREV_IMAGE = 250;

  // Time period during which multiple clicks will be ignored
  static final int MIN_TIME_BETWEEN_CLICKS = 1000;

  public interface OnClickListener {
    void onClick();
  }

  public interface OnMenuItemClickListener {
    void onClick(MenuItem item);
  }
}
