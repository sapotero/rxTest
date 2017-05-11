package sapotero.rxtest.utils;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

public class Settings {

  public static final String FIRST_RUN_KEY = "is_first_run";
  public static final String SIGN_WITH_DC_KEY = "SIGN_WITH_DC";

  private RxSharedPreferences settings;

  private Preference<Boolean> firstRunFlag;
  private Preference<Boolean> signWithDc;

  public Settings(RxSharedPreferences settings) {
    this.settings = settings;
    loadSettings();
  }

  private void loadSettings() {
    firstRunFlag = settings.getBoolean(FIRST_RUN_KEY);
    signWithDc = settings.getBoolean(SIGN_WITH_DC_KEY);
  }

  public boolean isFirstRun( ) {
    return getBoolean(firstRunFlag);
  }

  public void setFirstRun(boolean value) {
    setBoolean(firstRunFlag, value);
  }

  public boolean isSignedWithDc( ) {
    return getBoolean(signWithDc);
  }

  public void setSignedWithDc(boolean value) {
    setBoolean(signWithDc, value);
  }

  private boolean getBoolean(Preference<Boolean> booleanPreference) {
    boolean result;

    if (booleanPreference != null) {
      Boolean value = booleanPreference.get();

      if (value != null) {
        result = value;
      } else {
        result = false;
      }
    } else {
      result = false;
    }

    return result;
  }

  private void setBoolean(Preference<Boolean> booleanPreference, boolean value) {
    if (booleanPreference != null) {
      booleanPreference.set(value);
    }
  }
}
