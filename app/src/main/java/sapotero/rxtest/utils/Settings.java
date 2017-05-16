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
  public static final String CURRENT_USER_ID_KEY = "current_user_id";
  public static final String CURRENT_USER_KEY = "current_user";
  public static final String CURRENT_USER_ORGANIZATION_KEY = "current_user_organization";
  public static final String POSITION_KEY = "position";
  public static final String MAIN_MENU_POSITION_KEY = "activity_main_menu.position";
  public static final String REGNUMBER_KEY = "activity_main_menu.regnumber";
  public static final String REGDATE_KEY = "activity_main_menu.date";
  public static final String LOAD_FROM_SEARCH_KEY = "load_from_search";
  public static final String LAST_SEEN_UID_KEY = "activity_main_menu.last_seen_uid";
  public static final String FROM_SIGN_KEY = "activity_main_menu.from_sign";
  public static final String DECISION_WITH_ASSIGNMENT_KEY = "decision_with_assignment";
  public static final String DECISION_ACTIVE_ID_KEY = "decision.active.id";

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
  private Preference<String> currentUserId;
  private Preference<String> currentUser;
  private Preference<String> currentUserOrganization;
  private Preference<Integer> position;
  private Preference<Integer> mainMenuPosition;
  private Preference<String> regNumber;
  private Preference<String> regDate;
  private Preference<Boolean> loadFromSearch;
  private Preference<String> lastSeenUid;
  private Preference<Boolean> fromSign;
  private Preference<Boolean> decisionWithAssignment;
  private Preference<Integer> decisionActiveId;

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
    currentUserId = settings.getString(CURRENT_USER_ID_KEY);
    currentUser = settings.getString(CURRENT_USER_KEY);
    currentUserOrganization = settings.getString(CURRENT_USER_ORGANIZATION_KEY);
    position = settings.getInteger(POSITION_KEY);
    mainMenuPosition = settings.getInteger(MAIN_MENU_POSITION_KEY);
    regNumber = settings.getString(REGNUMBER_KEY);
    regDate = settings.getString(REGDATE_KEY);
    loadFromSearch = settings.getBoolean(LOAD_FROM_SEARCH_KEY);
    lastSeenUid = settings.getString(LAST_SEEN_UID_KEY);
    fromSign = settings.getBoolean(FROM_SIGN_KEY);
    decisionWithAssignment = settings.getBoolean(DECISION_WITH_ASSIGNMENT_KEY);
    decisionActiveId = settings.getInteger(DECISION_ACTIVE_ID_KEY);
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
    if (stringPreference != null) {
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

  public String getCurrentUserId() {
    return getString(currentUserId);
  }

  public void setCurrentUserId(String value) {
    setString(currentUserId, value);
  }

  public String getCurrentUser() {
    return getString(currentUser);
  }

  public void setCurrentUser(String value) {
    setString(currentUser, value);
  }

  public String getCurrentUserOrganization() {
    return getString(currentUserOrganization);
  }

  public void setCurrentUserOrganization(String value) {
    setString(currentUserOrganization, value);
  }

  public int getPosition() {
    return getInteger(position);
  }

  public void setPosition(int value) {
    setInteger(position, value);
  }

  public int getMainMenuPosition() {
    return getInteger(mainMenuPosition);
  }

  public void setMainMenuPosition(int value) {
    setInteger(mainMenuPosition, value);
  }

  public String getRegNumber() {
    return getString(regNumber);
  }

  public void setRegNumber(String value) {
    setString(regNumber, value);
  }

  public String getRegDate() {
    return getString(regDate);
  }

  public void setRegDate(String value) {
    setString(regDate, value);
  }

  public boolean isLoadFromSearch() {
    return getBoolean(loadFromSearch);
  }

  public void setLoadFromSearch(boolean value) {
    setBoolean(loadFromSearch, value);
  }

  public String getLastSeenUid() {
    return getString(lastSeenUid);
  }

  public void setLastSeenUid(String value) {
    setString(lastSeenUid, value);
  }

  public boolean isFromSign() {
    return getBoolean(fromSign);
  }

  public void setFromSign(boolean value) {
    setBoolean(fromSign, value);
  }

  public boolean isDecisionWithAssignment() {
    return getBoolean(decisionWithAssignment);
  }

  public void setDecisionWithAssignment(boolean value) {
    setBoolean(decisionWithAssignment, value);
  }

  public int getDecisionActiveId() {
    return getInteger(decisionActiveId);
  }

  public void setDecisionActiveId(int value) {
    setInteger(decisionActiveId, value);
  }
}
