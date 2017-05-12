package sapotero.rxtest.utils;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import sapotero.rxtest.R;

public class Settings {

  public static final String SIGN_WITH_DC_KEY = "SIGN_WITH_DC";
  public static final String DOCUMENTS_COUNT_KEY = "documents.count";
  public static final String LOGIN_KEY = "login";
  public static final String TOKEN_KEY = "token";
  public static final String PASSWORD_KEY = "password";
  public static final String PIN_KEY = "PIN";
  public static final String SIGN_KEY = "START_UP_SIGN";
  public static final String UID_KEY = "activity_main_menu.uid";
  public static final String STATUS_CODE_KEY = "activity_main_menu.star";

  public static String FIRST_RUN_KEY;
  public static String HOST_KEY;

  private Context context;
  private RxSharedPreferences settings;

  private Preference<Boolean> firstRunFlag;
  private Preference<Boolean> signWithDc;
  private Preference<Integer> jobCount;
  private Preference<String> login;
  private Preference<String> token;
  private Preference<String> host;
  private Preference<String> password;
  private Preference<String> pin;
  private Preference<String> sign;
  private Preference<String> uid;
  private Preference<String> statusCode;

  public Settings(Context context, RxSharedPreferences settings) {
    this.context = context;
    this.settings = settings;
    loadSettings();
  }

  private void loadSettings() {
    FIRST_RUN_KEY = context.getResources().getString(R.string.first_run_key);
    HOST_KEY = context.getResources().getString(R.string.host_key);

    firstRunFlag = settings.getBoolean(FIRST_RUN_KEY);
    signWithDc = settings.getBoolean(SIGN_WITH_DC_KEY);
    jobCount = settings.getInteger(DOCUMENTS_COUNT_KEY);
    login = settings.getString(LOGIN_KEY);
    token = settings.getString(TOKEN_KEY);
    host  = settings.getString(HOST_KEY);
    password = settings.getString(PASSWORD_KEY);
    pin = settings.getString(PIN_KEY);
    sign = settings.getString(SIGN_KEY);
    uid = settings.getString(UID_KEY);
    statusCode = settings.getString(STATUS_CODE_KEY);
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

  public String getLogin() {
    return getString(login);
  }

  public void setLogin(String value) {
    setString(login, value);
  }

  public Preference<String> getLoginPreference() {
    return login;
  }

  private String getString(Preference<String> stringPreference) {
    String value = stringPreference.get();

    if (value != null) {
      return value;
    } else {
      return "";
    }
  }

  private void setString(Preference<String> stringPreference, String value) {
    if (stringPreference != null && value != null) {
      stringPreference.set(value);
    }
  }

  public String getToken() {
    return getString(token);
  }

  public void setToken(String value) {
    setString(token, value);
  }

  public String getHost() {
    return getString(host);
  }

  public void setHost(String value) {
    setString(host, value);
  }

  public String getPassword() {
    return getString(password);
  }

  public void setPassword(String value) {
    setString(password, value);
  }

  public String getPin() {
    return getString(pin);
  }

  public void setPin(String value) {
    setString(pin, value);
  }

  public String getSign() {
    return getString(sign);
  }

  public void setSign(String value) {
    setString(sign, value);
  }

  public String getUid() {
    return getString(uid);
  }

  public void setUid(String value) {
    setString(uid, value);
  }

  public String getStatusCode() {
    return getString(statusCode);
  }

  public void setStatusCode(String value) {
    setString(statusCode, value);
  }
}
