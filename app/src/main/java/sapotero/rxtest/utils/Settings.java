package sapotero.rxtest.utils;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.Set;

import sapotero.rxtest.R;
import sapotero.rxtest.views.custom.stepper.util.AuthType;

public class Settings {

  private static final String CURRENT_ACTIVITY_KEY = "current_activity_key";
  private static final String SIGN_WITH_DC_KEY = "SIGN_WITH_DC";
  private static final String IS_ONLINE = "is_online";
  private static final String DOCUMENTS_COUNT_KEY = "documents.put";
  private static final String LOGIN_KEY = "login";
  private static final String TOKEN_KEY = "token";
  private static final String PASSWORD_KEY = "password";
  private static final String PIN_KEY = "PIN";
  private static final String SIGN_KEY = "START_UP_SIGN";
  private static final String UID_KEY = "activity_main_menu.uid";
  private static final String STATUS_CODE_KEY = "activity_main_menu.stat";
  private static final String CURRENT_USER_ID_KEY = "current_user_id";
  private static final String CURRENT_USER_KEY = "current_user";
  private static final String CURRENT_USER_ORGANIZATION_KEY = "current_user_organization";
  private static final String CURRENT_USER_POSITION_KEY = "current_user_position";
  private static final String MAIN_MENU_POSITION_KEY = "activity_main_menu.position";
  private static final String REGNUMBER_KEY = "activity_main_menu.regnumber";
  private static final String REGDATE_KEY = "activity_main_menu.date";
  private static final String LOAD_FROM_SEARCH_KEY = "load_from_search";
  private static final String LAST_SEEN_UID_KEY = "activity_main_menu.last_seen_uid";
  private static final String DECISION_WITH_ASSIGNMENT_KEY = "decision_with_assignment";
  private static final String DECISION_ACTIVE_ID_KEY = "decision.active.id";
  private static final String PREV_DIALOG_COMMENT_KEY = "prev_dialog_comment";
  private static final String START_LOAD_DATA_KEY = "start_load_data";
  private static final String STEPPER_AUTH_TYPE_KEY = "stepper.auth_type";
  private static final String FAVORITES_LOADED_KEY = "favorites.loaded";
  private static final String PROCESSED_LOADED_KEY = "processed.loaded";
  private static final String IN_THE_SAME_TAB_KEY = "in.the.same.tab";
  private static final String IMAGE_INDEX_KEY = "image.index";

  public static String FIRST_RUN_KEY;
  private static String HOST_KEY;
  private static String ACTIONS_CONFIRM_KEY;
  private static String CONTROL_CONFIRM_KEY;
  private static String SHOW_COMMENT_POST_KEY;
  private static String SHOW_URGENCY_KEY;
  private static String ONLY_URGENT_KEY;
  private static String JOURNALS_KEY;
  private static String START_PAGE_KEY;
  private static String START_JOURNAL_KEY;
  private static String SHOW_WITHOUT_PROJECT_KEY;
  private static String HIDE_PRIMARY_CONSIDERATION_KEY;
  private static String HIDE_BUTTONS_KEY;
  private static String SHOW_DECISION_DATE_UPDATE_KEY;
  private static String SHOW_DECISION_CHANGE_FONT_KEY;
  private static String SHOW_ORIGIN_KEY;
  private static String SHOW_CHANGE_SIGNER_KEY;
  private static String SHOW_CREATE_DECISION_POST_KEY;
  private static String SHOW_APPROVE_ON_PRIMARY_KEY;
  private static String MAX_IMAGE_SIZE_KEY;
  private static String DEBUG_ENABLED_KEY;

  private Context context;
  private RxSharedPreferences settings;

  private Preference<Boolean> firstRunFlag;
  private Preference<Boolean> signWithDc;
  private Preference<Integer> jobCount;
  private Preference<String> login;
  private Preference<String> token;
  private Preference<String> current_activity;
  private Preference<String> host;
  private Preference<String> password;
  private Preference<String> pin;
  private Preference<String> sign;
  private Preference<String> uid;
  private Preference<String> statusCode;
  private Preference<String> currentUserId;
  private Preference<String> currentUser;
  private Preference<String> currentUserOrganization;
  private Preference<String> currentUserPosition;
  private Preference<Integer> mainMenuPosition;
  private Preference<String> regNumber;
  private Preference<String> regDate;
  private Preference<Boolean> loadFromSearch;
  private Preference<String> lastSeenUid;
  private Preference<Boolean> decisionWithAssignment;
  private Preference<Integer> decisionActiveId;
  private Preference<Boolean> actionsConfirm;
  private Preference<Boolean> controlConfirm;
  private Preference<Boolean> showCommentPost;
  private Preference<Boolean> showUrgency;
  private Preference<Boolean> onlyUrgent;
  private Preference<Set<String>> journals;
  private Preference<String> prevDialogComment;
  private Preference<String> startPage;
  private Preference<String> startJournal;
  private Preference<Boolean> showWithoutProject;
  private Preference<Boolean> hidePrimaryConsideration;
  private Preference<Boolean> hideButtons;
  private Preference<Boolean> showDecisionDateUpdate;
  private Preference<Boolean> showDecisionChangeFont;
  private Preference<Boolean> showOrigin;
  private Preference<Boolean> showChangeSigner;
  private Preference<Boolean> showCreateDecisionPost;
  private Preference<Boolean> showApproveOnPrimary;
  private Preference<String> maxImageSize;
  private Preference<Boolean> debugEnabled;
  private Preference<Boolean> startLoadData;
  private Preference<AuthType> authType;
  private Preference<Boolean> online;
  private Preference<Boolean> favoritesLoaded;
  private Preference<Boolean> processedLoaded;
  private Preference<Boolean> inTheSameTab;
  private Preference<Integer> imageIndex;

  public Settings(Context context, RxSharedPreferences settings) {
    this.context = context;
    this.settings = settings;
    loadSettings();
  }

  private void loadSettings() {
    FIRST_RUN_KEY                  = context.getResources().getString(R.string.first_run_key);
    HOST_KEY                       = context.getResources().getString(R.string.host_key);
    ACTIONS_CONFIRM_KEY            = context.getResources().getString(R.string.actions_confirm_key);
    CONTROL_CONFIRM_KEY            = context.getResources().getString(R.string.control_confirm_key);
    SHOW_COMMENT_POST_KEY          = context.getResources().getString(R.string.show_comment_post_key);
    SHOW_URGENCY_KEY               = context.getResources().getString(R.string.show_urgency_key);
    ONLY_URGENT_KEY                = context.getResources().getString(R.string.only_urgent_key);
    JOURNALS_KEY                   = context.getResources().getString(R.string.journals_key);
    START_PAGE_KEY                 = context.getResources().getString(R.string.start_page_key);
    START_JOURNAL_KEY                 = context.getResources().getString(R.string.start_journal_key);
    SHOW_WITHOUT_PROJECT_KEY       = context.getResources().getString(R.string.show_without_project_key);
    HIDE_PRIMARY_CONSIDERATION_KEY = context.getResources().getString(R.string.hide_primary_consideration_key);
    HIDE_BUTTONS_KEY               = context.getResources().getString(R.string.hide_buttons_key);
    SHOW_DECISION_DATE_UPDATE_KEY  = context.getResources().getString(R.string.show_decision_date_update_key);
    SHOW_DECISION_CHANGE_FONT_KEY  = context.getResources().getString(R.string.show_decision_change_font_key);
    SHOW_ORIGIN_KEY                = context.getResources().getString(R.string.show_origin_key);
    SHOW_CHANGE_SIGNER_KEY         = context.getResources().getString(R.string.show_change_signer_key);
    SHOW_CREATE_DECISION_POST_KEY  = context.getResources().getString(R.string.show_create_decision_post_key);
    SHOW_APPROVE_ON_PRIMARY_KEY    = context.getResources().getString(R.string.show_approve_on_primary_key);
    MAX_IMAGE_SIZE_KEY             = context.getResources().getString(R.string.max_image_size_key);
    DEBUG_ENABLED_KEY              = context.getResources().getString(R.string.debug_enabled_key);

    firstRunFlag                   = settings.getBoolean(FIRST_RUN_KEY);
    signWithDc                     = settings.getBoolean(SIGN_WITH_DC_KEY);
    jobCount                       = settings.getInteger(DOCUMENTS_COUNT_KEY);
    login                          = settings.getString(LOGIN_KEY);
    login                          = settings.getString(LOGIN_KEY);
    token                          = settings.getString(TOKEN_KEY);
    host                           = settings.getString(HOST_KEY);
    password                       = settings.getString(PASSWORD_KEY);
    pin                            = settings.getString(PIN_KEY);
    sign                           = settings.getString(SIGN_KEY);
    uid                            = settings.getString(UID_KEY);
    statusCode                     = settings.getString(STATUS_CODE_KEY);
    currentUserId                  = settings.getString(CURRENT_USER_ID_KEY);
    currentUser                    = settings.getString(CURRENT_USER_KEY);
    currentUserOrganization        = settings.getString(CURRENT_USER_ORGANIZATION_KEY);
    currentUserPosition            = settings.getString(CURRENT_USER_POSITION_KEY);
    mainMenuPosition               = settings.getInteger(MAIN_MENU_POSITION_KEY);
    regNumber                      = settings.getString(REGNUMBER_KEY);
    regDate                        = settings.getString(REGDATE_KEY);
    loadFromSearch                 = settings.getBoolean(LOAD_FROM_SEARCH_KEY);
    lastSeenUid                    = settings.getString(LAST_SEEN_UID_KEY);
    decisionWithAssignment         = settings.getBoolean(DECISION_WITH_ASSIGNMENT_KEY);
    decisionActiveId               = settings.getInteger(DECISION_ACTIVE_ID_KEY);
    actionsConfirm                 = settings.getBoolean(ACTIONS_CONFIRM_KEY);
    controlConfirm                 = settings.getBoolean(CONTROL_CONFIRM_KEY);
    showCommentPost                = settings.getBoolean(SHOW_COMMENT_POST_KEY);
    showUrgency                    = settings.getBoolean(SHOW_URGENCY_KEY);
    onlyUrgent                     = settings.getBoolean(ONLY_URGENT_KEY);
    journals                       = settings.getStringSet(JOURNALS_KEY);
    prevDialogComment              = settings.getString(PREV_DIALOG_COMMENT_KEY);
    startPage                      = settings.getString(START_PAGE_KEY);
    startJournal                   = settings.getString(START_JOURNAL_KEY);
    showWithoutProject             = settings.getBoolean(SHOW_WITHOUT_PROJECT_KEY);
    hidePrimaryConsideration       = settings.getBoolean(HIDE_PRIMARY_CONSIDERATION_KEY);
    hideButtons                    = settings.getBoolean(HIDE_BUTTONS_KEY);
    showDecisionDateUpdate         = settings.getBoolean(SHOW_DECISION_DATE_UPDATE_KEY);
    showDecisionChangeFont         = settings.getBoolean(SHOW_DECISION_CHANGE_FONT_KEY);
    showOrigin                     = settings.getBoolean(SHOW_ORIGIN_KEY);
    showChangeSigner               = settings.getBoolean(SHOW_CHANGE_SIGNER_KEY);
    showCreateDecisionPost         = settings.getBoolean(SHOW_CREATE_DECISION_POST_KEY);
    showApproveOnPrimary           = settings.getBoolean(SHOW_APPROVE_ON_PRIMARY_KEY);
    maxImageSize                   = settings.getString(MAX_IMAGE_SIZE_KEY);
    debugEnabled                   = settings.getBoolean(DEBUG_ENABLED_KEY);
    startLoadData                  = settings.getBoolean(START_LOAD_DATA_KEY);
    authType                       = settings.getEnum(STEPPER_AUTH_TYPE_KEY, AuthType.class);
    current_activity               = settings.getString(CURRENT_ACTIVITY_KEY);
    online                         = settings.getBoolean(IS_ONLINE);
    favoritesLoaded                = settings.getBoolean(FAVORITES_LOADED_KEY);
    processedLoaded                = settings.getBoolean(PROCESSED_LOADED_KEY);
    inTheSameTab                   = settings.getBoolean(IN_THE_SAME_TAB_KEY);
    imageIndex                     = settings.getInteger(IMAGE_INDEX_KEY);
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

  public Preference<String> getCurrentActivity() {
    return current_activity;
  }

  public void setCurrentActivity(Preference<String> current_activity) {
    this.current_activity = current_activity;
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

  public String getCurrentUserPosition() {
    return getString(currentUserPosition);
  }

  public void setCurrentUserPosition(String value) {
    setString(currentUserPosition, value);
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

  public boolean isActionsConfirm() {
    return getBoolean(actionsConfirm);
  }

  public Preference<Boolean> getActionsConfirmPreference() {
    return actionsConfirm;
  }

  public boolean isControlConfirm() {
    return getBoolean(controlConfirm);
  }

  public boolean isShowCommentPost() {
    return getBoolean(showCommentPost);
  }

  public void setShowCommentPost(boolean value) {
    setBoolean(showCommentPost, value);
  }

  public boolean isShowUrgency() {
    return getBoolean(showUrgency);
  }

  public Preference<Boolean> getShowUrgencyPreference() {
    return showUrgency;
  }

  public boolean isOnlyUrgent() {
    return getBoolean(onlyUrgent);
  }

  public void setOnlyUrgent(boolean value) {
    setBoolean(onlyUrgent, value);
  }

  private Set<String> getStringSet(Preference<Set<String>> stringSetPreference) {
    return stringSetPreference.get();
  }

  public Set<String> getJournals() {
    return getStringSet(journals);
  }

  public Preference<Set<String>> getJournalsPreference() {
    return journals;
  }

  public String getPrevDialogComment() {
    return getString(prevDialogComment);
  }

  public void setPrevDialogComment(String value) {
    setString(prevDialogComment, value);
  }

  public String getStartPage() {
    return getString(startPage);
  }

  public String getStartJournal() {
    return getString(startJournal);
  }

  public boolean isShowWithoutProject() {
    return getBoolean(showWithoutProject);
  }

  public boolean isHidePrimaryConsideration() {
    return getBoolean(hidePrimaryConsideration);
  }

  public boolean isHideButtons() {
    return getBoolean(hideButtons);
  }

  public boolean isShowDecisionDateUpdate() {
    return getBoolean(showDecisionDateUpdate);
  }

  public boolean isShowDecisionChangeFont() {
    return getBoolean(showDecisionChangeFont);
  }

  public boolean isShowOrigin() {
    return getBoolean(showOrigin);
  }

  public boolean isShowChangeSigner() {
    return getBoolean(showChangeSigner);
  }

  public boolean isShowCreateDecisionPost() {
    return getBoolean(showCreateDecisionPost);
  }

  public boolean isShowApproveOnPrimary() {
    return getBoolean(showApproveOnPrimary);
  }

  public String getMaxImageSize() {
    return getString(maxImageSize);
  }

  public void setMaxImageSize(String value) {
    setString(maxImageSize, value);
  }

  public boolean isDebugEnabled() {
    return getBoolean(debugEnabled);
  }

  public boolean isStartLoadData() {
    return getBoolean(startLoadData);
  }

  public void setStartLoadData(boolean value) {
    setBoolean(startLoadData, value);
  }

  public AuthType getAuthType() {
    return authType.get();
  }

  public void setAuthType(AuthType value) {
    authType.set(value);
  }

  public Preference<AuthType> getAuthTypePreference() {
    return authType;
  }

  public boolean isOnline() {
    return getBoolean(online);
  }

  public void setOnline(Boolean value) {
    online.set(value);
  }

  public boolean isFavoritesLoaded() {
    return getBoolean(favoritesLoaded);
  }

  public void setFavoritesLoaded(Boolean value) {
    favoritesLoaded.set(value);
  }

  public boolean isProcessedLoaded() {
    return getBoolean(processedLoaded);
  }

  public void setProcessedLoaded(Boolean value) {
    processedLoaded.set(value);
  }

  public boolean isInTheSameTab() {
    return getBoolean(inTheSameTab);
  }

  public void setInTheSameTab(Boolean value) {
    inTheSameTab.set(value);
  }

  public int getImageIndex() {
    return getInteger(imageIndex);
  }

  public void setImageIndex(int value) {
    setInteger(imageIndex, value);
  }
}
