package sapotero.rxtest.utils;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import sapotero.rxtest.R;

public class Settings {

  public static final String SIGN_WITH_DC_KEY = "SIGN_WITH_DC";
  public static final String DOCUMENTS_COUNT_KEY = "documents.count";

  public static String FIRST_RUN_KEY;

  private Context context;
  private RxSharedPreferences settings;

  private Preference<Boolean> firstRunFlag;
  private Preference<Boolean> signWithDc;
  private Preference<Integer> jobCount;

  public Settings(Context context, RxSharedPreferences settings) {
    this.context = context;
    this.settings = settings;
    loadSettings();
  }

  private void loadSettings() {
    FIRST_RUN_KEY = context.getResources().getString(R.string.first_run_key);

    firstRunFlag = settings.getBoolean(FIRST_RUN_KEY);
    signWithDc = settings.getBoolean(SIGN_WITH_DC_KEY);
    jobCount = settings.getInteger(DOCUMENTS_COUNT_KEY);
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

  public int getJobCount() {
    return getInteger(jobCount);
  }

  public void setJobCount(int value) {
    setInteger(jobCount, value);
  }

  public void addJobCount(int value) {
    setJobCount(getJobCount() + value);
  }

  private int getInteger(Preference<Integer> integerPreference) {
    Integer value = integerPreference.get();

    if (value != null) {
      return value;
    } else {
      return 0;
    }
  }

  private void setInteger(Preference<Integer> integerPreference, int value) {
    if (integerPreference != null) {
      integerPreference.set(value);
    }
  }
}
