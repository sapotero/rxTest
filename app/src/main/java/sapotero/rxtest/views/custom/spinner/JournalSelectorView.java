package sapotero.rxtest.views.custom.spinner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ANKIT
 */
@SuppressLint("AppCompatCustomView")
public class JournalSelectorView extends TextView {

  public JournalSelectorView(Context context) {
    super(context);
  }

  public JournalSelectorView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public JournalSelectorView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public JournalSelectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }
}