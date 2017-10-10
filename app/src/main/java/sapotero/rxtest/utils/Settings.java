package sapotero.rxtest.utils;

import android.content.Context;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.HashSet;
import java.util.Set;

import sapotero.rxtest.R;
import sapotero.rxtest.views.custom.stepper.util.AuthType;

public class Settings implements ISettings {

  private static final String CURRENT_ACTIVITY_KEY = "current_activity_key";
  private static final String SIGN_WITH_DC_KEY = "SIGN_WITH_DC";
  private static final String IS_ONLINE = "is_online";
  private static final String DOCUMENTS_TOTAL_COUNT_KEY = "documents.total.count";
  private static final String DOCPROJ_COUNT_KEY = "docproj.count";
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
  private static final String CURRENT_USER_IMAGE_KEY = "current_user_image";
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
  private static final String IMAGE_INDEX_KEY = "image.index";
  private static final String UNAUTHORIZED_KEY = "user.unauthorized";
  private static final String SHOW_PRIMARY_CONSIDERATION = "show_primary_consideration";
  private static final String ORGANIZATION_FILTER_ACTIVE_KEY = "organization.filter.active";
  private static final String ORGANIZATION_FILTER_SELECTION_KEY = "organization.filter.savedselection";
  private static final String IS_SUBSTITUTE_MODE_KEY = "substitute.mode";
  private static final String OLD_LOGIN_KEY = "old_login";
  private static final String OLD_CURRENT_USER_ID_KEY = "old_current_user_id";
  private static final String OLD_CURRENT_USER_KEY = "old_current_user";
  private static final String OLD_CURRENT_USER_ORGANIZATION_KEY = "old_current_user_organization";
  private static final String OLD_CURRENT_USER_IMAGE_KEY = "old_current_user_image";
  private static final String COLLEAGUE_ID_KEY = "colleague.id";
  private static final String UPDATE_AUTH_STARTED_KEY = "update.auth.started";
  private static final String TAB_CHANGED_KEY = "tab.changed";
  private static final String START_REGULAR_REFRESH_KEY = "start.regular.refresh";

  public static String FIRST_RUN_KEY;
  public static String IS_PROJECT;
  private static String UPDATE_TIME_KEY;
  private static String HOST_KEY;
  private static String INFOCARD_FONTSIZE;
  private static String ACTIONS_CONFIRM_KEY;
  private static String CONTROL_CONFIRM_KEY;
  private static String SHOW_COMMENT_POST_KEY;
  private static String SHOW_URGENCY_KEY;
  private static String ONLY_URGENT_KEY;
  private static String JOURNALS_KEY;
  private static String YEARS_KEY;
  private static String START_PAGE_KEY;
  private static String START_JOURNAL_KEY;
  private static String IMAGE_LOADED_KEY;
  private static String IMAGE_DELETE_KEY;
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
  private static String NOTIFICATED_JOURNALS_KEY;


  private Context context;
  private RxSharedPreferences settings;

  private Preference<Boolean> firstRunFlag;
  private Preference<Boolean> isProject;
  private Preference<Boolean> signWithDc;
  private Preference<Integer> totalDocCount;
  private Preference<Integer> docProjCount;
  private Preference<String> login;
  private Preference<String> token;
  private Preference<String> infocard_fontSize;
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
  private Preference<String> currentUserImage;
  private Preference<Integer> mainMenuPosition;
  private Preference<String> regNumber;
  private Preference<String> regDate;
  private Preference<Boolean> loadFromSearch;
  private Preference<String> lastSeenUid;
  private Preference<Boolean> decisionWithAssignment;
  private Preference<String> decisionActiveId;
  private Preference<Boolean> actionsConfirm;
  private Preference<Boolean> controlConfirm;
  private Preference<Boolean> showCommentPost;
  private Preference<Boolean> showUrgency;
  private Preference<Boolean> onlyUrgent;
  private Preference<Set<String>> journals;
  private Preference<Set<String>> years;
  private Preference<String> prevDialogComment;
  private Preference<String> startPage;
  private Preference<String> image_load_period;
  private Preference<String> image_delete_period;
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
  private Preference<Integer> imageIndex;
  private Preference<Boolean> unauthorized;
  private Preference<Boolean> showPrimaryConsideration;
  private Preference<Boolean> organizationFilterActive;
  private Preference<Set<String>> organizationFilterSelection;
  private Preference<Set<String>> notificatedJournals;
  private Preference<Boolean> isSubstituteMode;
  private Preference<String> oldLogin;
  private Preference<String> oldCurrentUserId;
  private Preference<String> oldCurrentUser;
  private Preference<String> oldCurrentUserOrganization;
  private Preference<String> oldCurrentUserImage;
  private Preference<String> colleagueId;
  private Preference<Boolean> isUpdateAuthStarted;
  private Preference<Boolean> isTabChanged;
  private Preference<String> updateTime;
  private Preference<Boolean> isStartRegularRefresh;

  public Settings(Context context, RxSharedPreferences settings) {
    this.context = context;
    this.settings = settings;
    loadSettings();
  }

  private void loadSettings() {
    UPDATE_TIME_KEY                = context.getResources().getString(R.string.update_time_key);
    FIRST_RUN_KEY                  = context.getResources().getString(R.string.first_run_key);
    IS_PROJECT                     = context.getResources().getString(R.string.is_project_key);
    HOST_KEY                       = context.getResources().getString(R.string.host_key);
    INFOCARD_FONTSIZE              = context.getResources().getString(R.string.zoomTextSize_key);
    ACTIONS_CONFIRM_KEY            = context.getResources().getString(R.string.actions_confirm_key);
    CONTROL_CONFIRM_KEY            = context.getResources().getString(R.string.control_confirm_key);
    SHOW_COMMENT_POST_KEY          = context.getResources().getString(R.string.show_comment_post_key);
    SHOW_URGENCY_KEY               = context.getResources().getString(R.string.show_urgency_key);
    ONLY_URGENT_KEY                = context.getResources().getString(R.string.only_urgent_key);
    JOURNALS_KEY                   = context.getResources().getString(R.string.journals_key);
    YEARS_KEY                      = context.getResources().getString(R.string.years_key);
    START_PAGE_KEY                 = context.getResources().getString(R.string.start_page_key);
    START_JOURNAL_KEY              = context.getResources().getString(R.string.start_journal_key);
    IMAGE_LOADED_KEY               = context.getResources().getString(R.string.processed_load_period_key);
    IMAGE_DELETE_KEY               = context.getResources().getString(R.string.processed_delete_period_key);
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
    NOTIFICATED_JOURNALS_KEY       = context.getResources().getString(R.string.notificated_journals_key);

    firstRunFlag                   = settings.getBoolean(FIRST_RUN_KEY);
    isProject                      = settings.getBoolean(IS_PROJECT);
    signWithDc                     = settings.getBoolean(SIGN_WITH_DC_KEY);
    totalDocCount                  = settings.getInteger(DOCUMENTS_TOTAL_COUNT_KEY);
    docProjCount                   = settings.getInteger(DOCPROJ_COUNT_KEY);
    updateTime                     = settings.getString(UPDATE_TIME_KEY);
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
    currentUserImage               = settings.getString(CURRENT_USER_IMAGE_KEY);
    mainMenuPosition               = settings.getInteger(MAIN_MENU_POSITION_KEY);
    regNumber                      = settings.getString(REGNUMBER_KEY);
    regDate                        = settings.getString(REGDATE_KEY);
    loadFromSearch                 = settings.getBoolean(LOAD_FROM_SEARCH_KEY);
    lastSeenUid                    = settings.getString(LAST_SEEN_UID_KEY);
    decisionWithAssignment         = settings.getBoolean(DECISION_WITH_ASSIGNMENT_KEY);
    decisionActiveId               = settings.getString(DECISION_ACTIVE_ID_KEY);
    actionsConfirm                 = settings.getBoolean(ACTIONS_CONFIRM_KEY);
    controlConfirm                 = settings.getBoolean(CONTROL_CONFIRM_KEY);
    showCommentPost                = settings.getBoolean(SHOW_COMMENT_POST_KEY);
    showUrgency                    = settings.getBoolean(SHOW_URGENCY_KEY);
    onlyUrgent                     = settings.getBoolean(ONLY_URGENT_KEY);
    journals                       = settings.getStringSet(JOURNALS_KEY);
    years                          = settings.getStringSet(YEARS_KEY);
    prevDialogComment              = settings.getString(PREV_DIALOG_COMMENT_KEY);
    startPage                      = settings.getString(START_PAGE_KEY);
    image_load_period              = settings.getString(IMAGE_LOADED_KEY);
    image_delete_period            = settings.getString(IMAGE_DELETE_KEY);
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
    infocard_fontSize              = settings.getString(INFOCARD_FONTSIZE);
    online                         = settings.getBoolean(IS_ONLINE);
    favoritesLoaded                = settings.getBoolean(FAVORITES_LOADED_KEY);
    processedLoaded                = settings.getBoolean(PROCESSED_LOADED_KEY);
    imageIndex                     = settings.getInteger(IMAGE_INDEX_KEY);
    unauthorized                   = settings.getBoolean(UNAUTHORIZED_KEY);
    showPrimaryConsideration       = settings.getBoolean(SHOW_PRIMARY_CONSIDERATION);
    organizationFilterActive       = settings.getBoolean(ORGANIZATION_FILTER_ACTIVE_KEY);
    organizationFilterSelection    = settings.getStringSet(ORGANIZATION_FILTER_SELECTION_KEY);
    notificatedJournals            = settings.getStringSet(NOTIFICATED_JOURNALS_KEY);
    isSubstituteMode               = settings.getBoolean(IS_SUBSTITUTE_MODE_KEY);
    oldLogin                       = settings.getString(OLD_LOGIN_KEY);
    oldCurrentUserId               = settings.getString(OLD_CURRENT_USER_ID_KEY);
    oldCurrentUser                 = settings.getString(OLD_CURRENT_USER_KEY);
    oldCurrentUserOrganization     = settings.getString(OLD_CURRENT_USER_ORGANIZATION_KEY);
    oldCurrentUserImage            = settings.getString(OLD_CURRENT_USER_IMAGE_KEY);
    colleagueId                    = settings.getString(COLLEAGUE_ID_KEY);
    isUpdateAuthStarted            = settings.getBoolean(UPDATE_AUTH_STARTED_KEY);
    isTabChanged                   = settings.getBoolean(TAB_CHANGED_KEY);
    isStartRegularRefresh          = settings.getBoolean(START_REGULAR_REFRESH_KEY);
  }

  @Override
  public boolean isShowPrimaryConsideration() {
    return getBoolean(showPrimaryConsideration);
  }

  @Override
  public void setShowPrimaryConsideration(boolean value) {
    setBoolean(showPrimaryConsideration, value);
  }

  @Override
  public boolean isFirstRun() {
    return getBoolean(firstRunFlag);
  }

  @Override
  public void setIsProject(boolean value) {
    setBoolean(isProject, value);
  }


  @Override
  public boolean isProject() {
    return getBoolean(isProject);
  }

  @Override
  public void setFirstRun(boolean value) {
    setBoolean(firstRunFlag, value);
  }


  @Override
  public boolean isSignedWithDc() {
    return getBoolean(signWithDc);
  }

  @Override
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

  @Override
  public int getTotalDocCount() {
    return getInteger(totalDocCount);
  }

  @Override
  public void setTotalDocCount(int value) {
    setInteger(totalDocCount, value);
  }

  @Override
  public void addTotalDocCount(int value) {
    setTotalDocCount(getTotalDocCount() + value);
  }

  @Override
  public int getDocProjCount() {
    return getInteger(docProjCount);
  }

  @Override
  public void setDocProjCount(int value) {
    setInteger(docProjCount, value);
  }

  @Override
  public void addDocProjCount(int value) {
    setDocProjCount(getDocProjCount() + value);
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

  @Override
  public String getLogin() {
    return getString(login);
  }

  @Override
  public void setLogin(String value) {
    setString(login, value);
  }

  @Override
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

  @Override
  public Preference<String> getCurrentActivity() {
    return current_activity;
  }

  @Override
  public void setCurrentActivity(Preference<String> current_activity) {
    this.current_activity = current_activity;
  }

  @Override
  public String getToken() {
    return getString(token);
  }

  @Override
  public void setToken(String value) {
    setString(token, value);
  }

  @Override
  public String getHost() {
    return getString(host);
  }

  @Override
  public void setHost(String value) {
    setString(host, value);
  }

  @Override
  public String getPassword() {
    return getString(password);
  }

  @Override
  public void setPassword(String value) {
    setString(password, value);
  }

  @Override
  public String getPin() {
    return getString(pin);
  }

  @Override
  public void setPin(String value) {
    setString(pin, value);
  }

  @Override
  public String getSign() {
    return getString(sign);
  }

  @Override
  public void setSign(String value) {
    setString(sign, value);
  }

  @Override
  public String getUid() {
    return getString(uid);
  }

  @Override
  public void setUid(String value) {
    setString(uid, value);
  }



  @Override
  public String getUpdateTime() {
    return getString(updateTime);
  }

  @Override
  public void setUpdateTime(String value) {
    setString(updateTime, value);
  }



  @Override
  public String getStatusCode() {
    return getString(statusCode);
  }

  @Override
  public void setStatusCode(String value) {
    setString(statusCode, value);
  }

  @Override
  public String getCurrentUserId() {
    return getString(currentUserId);
  }

  @Override
  public void setCurrentUserId(String value) {
    setString(currentUserId, value);
  }

  @Override
  public String getCurrentUser() {
    return getString(currentUser);
  }

  @Override
  public void setCurrentUser(String value) {
    setString(currentUser, value);
  }

  @Override
  public String getCurrentUserOrganization() {
    return getString(currentUserOrganization);
  }

  @Override
  public void setCurrentUserOrganization(String value) {
    setString(currentUserOrganization, value);
  }

  @Override
  public String getCurrentUserPosition() {
    return getString(currentUserPosition);
  }

  @Override
  public void setCurrentUserPosition(String value) {
    setString(currentUserPosition, value);
  }

  @Override
  public String getCurrentUserImage() {
    return getString(currentUserImage);
  }

  @Override
  public void setCurrentUserImage(String value) {
    setString(currentUserImage, value);
  }

  @Override
  public int getMainMenuPosition() {
    return getInteger(mainMenuPosition);
  }

  @Override
  public void setMainMenuPosition(int value) {
    setInteger(mainMenuPosition, value);
  }

  @Override
  public String getRegNumber() {
    return getString(regNumber);
  }

  @Override
  public void setRegNumber(String value) {
    setString(regNumber, value);
  }

  @Override
  public String getRegDate() {
    return getString(regDate);
  }

  @Override
  public void setRegDate(String value) {
    setString(regDate, value);
  }

  @Override
  public boolean isLoadFromSearch() {
    return getBoolean(loadFromSearch);
  }

  @Override
  public void setLoadFromSearch(boolean value) {
    setBoolean(loadFromSearch, value);
  }

  @Override
  public String getLastSeenUid() {
    return getString(lastSeenUid);
  }

  @Override
  public void setLastSeenUid(String value) {
    setString(lastSeenUid, value);
  }

  @Override
  public boolean isDecisionWithAssignment() {
    return getBoolean(decisionWithAssignment);
  }

  @Override
  public void setDecisionWithAssignment(boolean value) {
    setBoolean(decisionWithAssignment, value);
  }

  @Override
  public String getDecisionActiveUid() {
    return getString(decisionActiveId);
  }

  @Override
  public void setDecisionActiveUid(String value) {
    setString(decisionActiveId, value);
  }

  @Override
  public Preference<String> getDecisionActiveUidPreference() {
   return decisionActiveId;
  }

  @Override
  public boolean isActionsConfirm() {
    return getBoolean(actionsConfirm);
  }

  @Override
  public Preference<Boolean> getActionsConfirmPreference() {
    return actionsConfirm;
  }

  @Override
  public boolean isControlConfirm() {
    return getBoolean(controlConfirm);
  }

  @Override
  public boolean isShowCommentPost() {
    return getBoolean(showCommentPost);
  }

  @Override
  public void setShowCommentPost(boolean value) {
    setBoolean(showCommentPost, value);
  }

  @Override
  public boolean isShowUrgency() {
    return getBoolean(showUrgency);
  }

  @Override
  public Preference<Boolean> getShowUrgencyPreference() {
    return showUrgency;
  }

  @Override
  public boolean isOnlyUrgent() {
    return getBoolean(onlyUrgent);
  }

  @Override
  public void setOnlyUrgent(boolean value) {
    setBoolean(onlyUrgent, value);
  }

  private Set<String> getStringSet(Preference<Set<String>> stringSetPreference) {
    Set<String> value = stringSetPreference.get();

    if (value != null) {
      return value;
    } else {
      return new HashSet<>();
    }
  }

  private void setStringSet(Preference<Set<String>> stringSetPreference, Set<String> value) {
    if (stringSetPreference != null) {
      stringSetPreference.set(value);
    }
  }

  @Override
  public Set<String> getJournals() {
    return getStringSet(journals);
  }

  @Override
  public Preference<Set<String>> getJournalsPreference() {
    return journals;
  }

  @Override
  public Set<String> getYears() {
    return getStringSet(years);
  }

  @Override
  public Preference<Set<String>> getYearsPreference() {
    return years;
  }

  @Override
  public String getPrevDialogComment() {
    return getString(prevDialogComment);
  }

  @Override
  public void setPrevDialogComment(String value) {
    setString(prevDialogComment, value);
  }

  @Override
  public String getInfocardFontSize() {
    return getString(infocard_fontSize);
  }

  @Override
  public void setInfocardFontSize(String value) {
    setString(infocard_fontSize, value);
  }

  @Override
  public String getStartPage() {
    return getString(startPage);
  }

  @Override
  public String getImageLoadPeriod() {
    return getString(image_load_period);
  }

  @Override
  public String getImageDeletePeriod() {
    return getString(image_delete_period);
  }

  @Override
  public String getStartJournal() {
    return getString(startJournal);
  }

  @Override
  public boolean isShowWithoutProject() {
    return getBoolean(showWithoutProject);
  }

  @Override
  public boolean isHidePrimaryConsideration() {
    return getBoolean(hidePrimaryConsideration);
  }

  @Override
  public boolean isHideButtons() {
    return getBoolean(hideButtons);
  }

  @Override
  public boolean isShowDecisionDateUpdate() {
    return getBoolean(showDecisionDateUpdate);
  }

  @Override
  public boolean isShowDecisionChangeFont() {
    return getBoolean(showDecisionChangeFont);
  }

  @Override
  public boolean isShowOrigin() {
    return getBoolean(showOrigin);
  }

  @Override
  public boolean isShowChangeSigner() {
    return getBoolean(showChangeSigner);
  }

  @Override
  public boolean isShowCreateDecisionPost() {
    return getBoolean(showCreateDecisionPost);
  }

  @Override
  public boolean isShowApproveOnPrimary() {
    return getBoolean(showApproveOnPrimary);
  }

  @Override
  public String getMaxImageSize() {
    return getString(maxImageSize);
  }

  @Override
  public void setMaxImageSize(String value) {
    setString(maxImageSize, value);
  }

  @Override
  public boolean isDebugEnabled() {
    return getBoolean(debugEnabled);
  }

  @Override
  public boolean isStartLoadData() {
    return getBoolean(startLoadData);
  }

  @Override
  public void setStartLoadData(boolean value) {
    setBoolean(startLoadData, value);
  }

  @Override
  public AuthType getAuthType() {
    return authType.get();
  }

  @Override
  public void setAuthType(AuthType value) {
    authType.set(value);
  }

  @Override
  public Preference<AuthType> getAuthTypePreference() {
    return authType;
  }

  @Override
  public boolean isOnline() {
    return getBoolean(online);
  }

  @Override
  public void setOnline(Boolean value) {
    setBoolean(online, value);
  }

  @Override
  public Preference<Boolean> getOnlinePreference() {
    return online;
  }

  @Override
  public boolean isFavoritesLoaded() {
    return getBoolean(favoritesLoaded);
  }

  @Override
  public void setFavoritesLoaded(Boolean value) {
    setBoolean(favoritesLoaded, value);
  }

  @Override
  public boolean isProcessedLoaded() {
    return getBoolean(processedLoaded);
  }

  @Override
  public void setProcessedLoaded(Boolean value) {
    setBoolean(processedLoaded, value);
  }

  @Override
  public int getImageIndex() {
    return getInteger(imageIndex);
  }

  @Override
  public void setImageIndex(int value) {
    setInteger(imageIndex, value);
  }

  @Override
  public boolean isUnauthorized() {
    return getBoolean(unauthorized);
  }

  @Override
  public void setUnauthorized(Boolean value) {
    setBoolean(unauthorized, value);
  }

  @Override
  public Preference<Boolean> getUnauthorizedPreference() {
    return unauthorized;
  }

  @Override
  public boolean isOrganizationFilterActive() {
    return getBoolean(organizationFilterActive);
  }

  @Override
  public void setOrganizationFilterActive(boolean value) {
    setBoolean(organizationFilterActive, value);
  }

  @Override
  public Set<String> getOrganizationFilterSelection() {
    return getStringSet(organizationFilterSelection);
  }

  @Override
  public void setOrganizationFilterSelection(Set<String> value) {
    setStringSet(organizationFilterSelection, value);
  }

  @Override
  public Set<String> getNotificatedJournals() {
    return getStringSet(notificatedJournals);
  }


  public boolean isSubstituteMode() {
    return getBoolean(isSubstituteMode);
  }

  @Override
  public void setSubstituteMode(boolean value) {
    setBoolean(isSubstituteMode, value);
  }

  @Override
  public Preference<Boolean> getSubstituteModePreference() {
    return isSubstituteMode;
  }

  @Override
  public String getOldLogin() {
    return getString(oldLogin);
  }

  @Override
  public void setOldLogin(String value) {
    setString(oldLogin, value);
  }

  @Override
  public String getOldCurrentUserId() {
    return getString(oldCurrentUserId);
  }

  @Override
  public void setOldCurrentUserId(String value) {
    setString(oldCurrentUserId, value);
  }

  @Override
  public String getOldCurrentUser() {
    return getString(oldCurrentUser);
  }

  @Override
  public void setOldCurrentUser(String value) {
    setString(oldCurrentUser, value);
  }

  @Override
  public String getOldCurrentUserOrganization() {
    return getString(oldCurrentUserOrganization);
  }

  @Override
  public void setOldCurrentUserOrganization(String value) {
    setString(oldCurrentUserOrganization, value);
  }

  @Override
  public String getOldCurrentUserImage() {
    return getString(oldCurrentUserImage);
  }

  @Override
  public void setOldCurrentUserImage(String value) {
    setString(oldCurrentUserImage, value);
  }

  @Override
  public String getColleagueId() {
    return getString(colleagueId);
  }

  @Override
  public void setColleagueId(String value) {
    setString(colleagueId, value);
  }

  @Override
  public boolean isUpdateAuthStarted() {
    return getBoolean(isUpdateAuthStarted);
  }

  @Override
  public void setUpdateAuthStarted(boolean value) {
    setBoolean(isUpdateAuthStarted, value);
  }

  @Override
  public boolean isTabChanged() {
    return getBoolean(isTabChanged);
  }

  @Override
  public void setTabChanged(boolean value) {
    setBoolean(isTabChanged, value);
  }

  @Override
  public boolean isStartRegularRefresh() {
    return getBoolean(isStartRegularRefresh);
  }

  @Override
  public void setStartRegularRefresh(boolean value) {
    setBoolean(isStartRegularRefresh, value);
  }
}
