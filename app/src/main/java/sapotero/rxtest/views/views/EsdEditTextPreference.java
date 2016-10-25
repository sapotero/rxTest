package sapotero.rxtest.views.views;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

public class EsdEditTextPreference extends EditTextPreference {
  public EsdEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public EsdEditTextPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void setText(String text) {
    super.setText(text);
    setSummary(text);
  }
}