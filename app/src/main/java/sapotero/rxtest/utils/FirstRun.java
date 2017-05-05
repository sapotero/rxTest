package sapotero.rxtest.utils;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

public class FirstRun {
  private RxSharedPreferences settings;
  private Preference<Boolean> firstRunFlag;

  public FirstRun(RxSharedPreferences rxSharedPreferences) {
    settings = rxSharedPreferences;
    firstRunFlag = settings.getBoolean("is_first_run");
  }

  public boolean isFirstRun( ) {
    return getBooleanFromSettings(firstRunFlag);
  }

  public void setFirstRun(boolean value) {
    setBooleanInSettings(firstRunFlag, value);
  }

  public boolean getBooleanFromSettings(Preference<Boolean> booleanPreference) {
    boolean result;

    if (booleanPreference != null) {
      Boolean value = booleanPreference.get( );

      if (value != null) {
        result = value;
      } else {
        // If no preference found, return true (it is first run)
        result = true;
      }
    } else {
      result = false;
    }

    return result;
  }

  public void setBooleanInSettings(Preference<Boolean> booleanPreference, boolean value) {
    if (booleanPreference != null) {
      booleanPreference.set(value);
    }
  }

  public boolean getBooleanFromSettings(String key) {
    boolean result;

    if (key != null && !key.equals("")) {
      Preference<Boolean> booleanPreference = settings.getBoolean(key);
      result = getBooleanFromSettings(booleanPreference);
    } else {
      result = false;
    }

    return result;
  }
}
