package sapotero.rxtest.utils;

import com.f2prateek.rx.preferences.Preference;

import java.util.Set;

import sapotero.rxtest.views.custom.stepper.util.AuthType;

public class TestSettings implements ISettings {

  private boolean firstRunFlag;
  private boolean signWithDc;
  private int totalDocCount;
  private int docProjCount;
  public String login;
  private String token;
  private String current_activity;
  private String host;
  private String password;
  private String pin;
  private String sign;
  private String uid;
  private String statusCode;
  private String currentUserId;
  private String currentUser;
  private String currentUserOrganization;
  private String currentUserPosition;
  private int mainMenuPosition;
  private String regNumber;
  private String regDate;
  private boolean loadFromSearch;
  private String lastSeenUid;
  private boolean decisionWithAssignment;
  private int decisionActiveId;
  private boolean actionsConfirm;
  private boolean controlConfirm;
  private boolean showCommentPost;
  private boolean showUrgency;
  private boolean onlyUrgent;
  private Set<String> journals;
  private Set<String> years;
  private String prevDialogComment;
  private String startPage;
  private String image_load_period;
  private String image_delete_period;
  private String startJournal;
  private boolean showWithoutProject;
  private boolean hidePrimaryConsideration;
  private boolean hideButtons;
  private boolean showDecisionDateUpdate;
  private boolean showDecisionChangeFont;
  private boolean showOrigin;
  private boolean showChangeSigner;
  private boolean showCreateDecisionPost;
  private boolean showApproveOnPrimary;
  private String maxImageSize;
  private boolean debugEnabled;
  private boolean startLoadData;
  private AuthType authType;
  private boolean online;
  private boolean favoritesLoaded;
  private boolean processedLoaded;
  private int imageIndex;
  private boolean unauthorized;
  private boolean showPrimaryConsideration;
  private boolean organizationFilterActive;
  private Set<String> organizationFilterSelection;

  public TestSettings() {
    login = "dummyLogin";
  }

  @Override
  public boolean isShowPrimaryConsideration() {
    return showPrimaryConsideration;
  }

  @Override
  public void setShowPrimaryConsideration(boolean value) {
    showPrimaryConsideration = value;
  }

  @Override
  public boolean isFirstRun() {
    return firstRunFlag;
  }

  @Override
  public void setFirstRun(boolean value) {
    firstRunFlag = value;
  }

  @Override
  public boolean isSignedWithDc() {
    return signWithDc;
  }

  @Override
  public void setSignedWithDc(boolean value) {
    signWithDc = value;
  }

  @Override
  public int getTotalDocCount() {
    return totalDocCount;
  }

  @Override
  public void setTotalDocCount(int value) {
    totalDocCount = value;
  }

  @Override
  public void addTotalDocCount(int value) {
    totalDocCount += value;
  }

  @Override
  public int getDocProjCount() {
    return docProjCount;
  }

  @Override
  public void setDocProjCount(int value) {
    docProjCount = value;
  }

  @Override
  public void addDocProjCount(int value) {
    docProjCount += value;
  }

  @Override
  public String getLogin() {
    return login;
  }

  @Override
  public void setLogin(String value) {
    login = value;
  }

  @Override
  public Preference<String> getLoginPreference() {
    return null;
  }

  @Override
  public Preference<String> getCurrentActivity() {
    return null;
  }

  @Override
  public void setCurrentActivity(Preference<String> current_activity) {
  }

  @Override
  public String getToken() {
    return token;
  }

  @Override
  public void setToken(String value) {
    token = value;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public void setHost(String value) {
    host = value;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public void setPassword(String value) {
    password = value;
  }

  @Override
  public String getPin() {
    return pin;
  }

  @Override
  public void setPin(String value) {
    pin = value;
  }

  @Override
  public String getSign() {
    return sign;
  }

  @Override
  public void setSign(String value) {
    sign = value;
  }

  @Override
  public String getUid() {
    return uid;
  }

  @Override
  public void setUid(String value) {
    uid = value;
  }

  @Override
  public String getStatusCode() {
    return statusCode;
  }

  @Override
  public void setStatusCode(String value) {
    statusCode = value;
  }

  @Override
  public String getCurrentUserId() {
    return currentUserId;
  }

  @Override
  public void setCurrentUserId(String value) {
    currentUserId = value;
  }

  @Override
  public String getCurrentUser() {
    return currentUser;
  }

  @Override
  public void setCurrentUser(String value) {
    currentUser = value;
  }

  @Override
  public String getCurrentUserOrganization() {
    return currentUserOrganization;
  }

  @Override
  public void setCurrentUserOrganization(String value) {
    currentUserOrganization = value;
  }

  @Override
  public String getCurrentUserPosition() {
    return currentUserPosition;
  }

  @Override
  public void setCurrentUserPosition(String value) {
    currentUserPosition = value;
  }

  @Override
  public int getMainMenuPosition() {
    return mainMenuPosition;
  }

  @Override
  public void setMainMenuPosition(int value) {
    mainMenuPosition = value;
  }

  @Override
  public String getRegNumber() {
    return regNumber;
  }

  @Override
  public void setRegNumber(String value) {
    regNumber = value;
  }

  @Override
  public String getRegDate() {
    return regDate;
  }

  @Override
  public void setRegDate(String value) {
    regDate = value;
  }

  @Override
  public boolean isLoadFromSearch() {
    return loadFromSearch;
  }

  @Override
  public void setLoadFromSearch(boolean value) {
    loadFromSearch = value;
  }

  @Override
  public String getLastSeenUid() {
    return lastSeenUid;
  }

  @Override
  public void setLastSeenUid(String value) {
    lastSeenUid = value;
  }

  @Override
  public boolean isDecisionWithAssignment() {
    return decisionWithAssignment;
  }

  @Override
  public void setDecisionWithAssignment(boolean value) {
    decisionWithAssignment = value;
  }

  @Override
  public int getDecisionActiveId() {
    return decisionActiveId;
  }

  @Override
  public void setDecisionActiveId(int value) {
    decisionActiveId = value;
  }

  @Override
  public boolean isActionsConfirm() {
    return actionsConfirm;
  }

  @Override
  public Preference<Boolean> getActionsConfirmPreference() {
    return null;
  }

  @Override
  public boolean isControlConfirm() {
    return controlConfirm;
  }

  @Override
  public boolean isShowCommentPost() {
    return showCommentPost;
  }

  @Override
  public void setShowCommentPost(boolean value) {
    showCommentPost = value;
  }

  @Override
  public boolean isShowUrgency() {
    return showUrgency;
  }

  @Override
  public Preference<Boolean> getShowUrgencyPreference() {
    return null;
  }

  @Override
  public boolean isOnlyUrgent() {
    return onlyUrgent;
  }

  @Override
  public void setOnlyUrgent(boolean value) {
    onlyUrgent = value;
  }

  @Override
  public Set<String> getJournals() {
    return journals;
  }

  @Override
  public Preference<Set<String>> getJournalsPreference() {
    return null;
  }

  @Override
  public Set<String> getYears() {
    return years;
  }

  @Override
  public Preference<Set<String>> getYearsPreference() {
    return null;
  }

  @Override
  public String getPrevDialogComment() {
    return prevDialogComment;
  }

  @Override
  public void setPrevDialogComment(String value) {
    prevDialogComment = value;
  }

  @Override
  public String getStartPage() {
    return startPage;
  }

  @Override
  public String getImageLoadPeriod() {
    return image_load_period;
  }

  @Override
  public String getImageDeletePeriod() {
    return image_delete_period;
  }

  @Override
  public String getStartJournal() {
    return startJournal;
  }

  @Override
  public boolean isShowWithoutProject() {
    return showWithoutProject;
  }

  @Override
  public boolean isHidePrimaryConsideration() {
    return hidePrimaryConsideration;
  }

  @Override
  public boolean isHideButtons() {
    return hideButtons;
  }

  @Override
  public boolean isShowDecisionDateUpdate() {
    return showDecisionDateUpdate;
  }

  @Override
  public boolean isShowDecisionChangeFont() {
    return showDecisionChangeFont;
  }

  @Override
  public boolean isShowOrigin() {
    return showOrigin;
  }

  @Override
  public boolean isShowChangeSigner() {
    return showChangeSigner;
  }

  @Override
  public boolean isShowCreateDecisionPost() {
    return showCreateDecisionPost;
  }

  @Override
  public boolean isShowApproveOnPrimary() {
    return showApproveOnPrimary;
  }

  @Override
  public String getMaxImageSize() {
    return maxImageSize;
  }

  @Override
  public void setMaxImageSize(String value) {
    maxImageSize = value;
  }

  @Override
  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  @Override
  public boolean isStartLoadData() {
    return startLoadData;
  }

  @Override
  public void setStartLoadData(boolean value) {
    startLoadData = value;
  }

  @Override
  public AuthType getAuthType() {
    return authType;
  }

  @Override
  public void setAuthType(AuthType value) {
    authType = value;
  }

  @Override
  public Preference<AuthType> getAuthTypePreference() {
    return null;
  }

  @Override
  public boolean isOnline() {
    return online;
  }

  @Override
  public void setOnline(Boolean value) {
    online = value;
  }

  @Override
  public Preference<Boolean> getOnlinePreference() {
    return null;
  }

  @Override
  public boolean isFavoritesLoaded() {
    return favoritesLoaded;
  }

  @Override
  public void setFavoritesLoaded(Boolean value) {
    favoritesLoaded = value;
  }

  @Override
  public boolean isProcessedLoaded() {
    return processedLoaded;
  }

  @Override
  public void setProcessedLoaded(Boolean value) {
    processedLoaded = value;
  }

  @Override
  public int getImageIndex() {
    return imageIndex;
  }

  @Override
  public void setImageIndex(int value) {
    imageIndex = value;
  }

  @Override
  public boolean isUnauthorized() {
    return unauthorized;
  }

  @Override
  public void setUnauthorized(Boolean value) {
    unauthorized = value;
  }

  @Override
  public Preference<Boolean> getUnauthorizedPreference() {
    return null;
  }

  @Override
  public boolean isOrganizationFilterActive() {
    return organizationFilterActive;
  }

  @Override
  public void setOrganizationFilterActive(boolean value) {
    organizationFilterActive = value;
  }

  @Override
  public Set<String> getOrganizationFilterSelection() {
    return organizationFilterSelection;
  }

  @Override
  public void setOrganizationFilterSelection(Set<String> value) {
    organizationFilterSelection = value;
  }
}
