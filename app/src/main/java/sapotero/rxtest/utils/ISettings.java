package sapotero.rxtest.utils;

import com.f2prateek.rx.preferences.Preference;

import java.util.Set;

import sapotero.rxtest.views.custom.stepper.util.AuthType;


public interface ISettings {
  boolean isShowPrimaryConsideration();

  void setShowPrimaryConsideration(boolean value);

  boolean isFirstRun();

  void setFirstRun(boolean value);

  boolean isProject();

  void setIsProject(boolean value);

  boolean isSignedWithDc();

  void setSignedWithDc(boolean value);

  int getTotalDocCount();

  void setTotalDocCount(int value);

  void addTotalDocCount(int value);

  int getDocProjCount();

  void setDocProjCount(int value);

  void addDocProjCount(int value);

  String getLogin();

  void setLogin(String value);

  Preference<String> getLoginPreference();

  Preference<String> getCurrentActivity();

  void setCurrentActivity(Preference<String> current_activity);

  String getToken();

  void setToken(String value);

  String getHost();

  void setHost(String value);

  String getPassword();

  void setPassword(String value);

  String getPin();

  void setPin(String value);

  String getSign();

  void setSign(String value);

  String getUid();

  void setUid(String value);

  String getUpdateTime();

  void setUpdateTime(String value);

  String getStatusCode();

  void setStatusCode(String value);

  String getCurrentUserId();

  void setCurrentUserId(String value);

  String getCurrentUser();

  void setCurrentUser(String value);

  String getCurrentUserOrganization();

  void setCurrentUserOrganization(String value);

  String getCurrentUserPosition();

  void setCurrentUserPosition(String value);

  String getCurrentUserImage();

  void setCurrentUserImage(String value);

  int getMainMenuPosition();

  void setMainMenuPosition(int value);

  String getRegNumber();

  void setRegNumber(String value);

  String getRegDate();

  void setRegDate(String value);

  boolean isLoadFromSearch();

  void setLoadFromSearch(boolean value);

  String getLastSeenUid();

  void setLastSeenUid(String value);

  boolean isDecisionWithAssignment();

  void setDecisionWithAssignment(boolean value);

  String getDecisionActiveUid();

  void setDecisionActiveUid(String value);

  Preference<String> getDecisionActiveUidPreference();

  boolean isActionsConfirm();

  Preference<Boolean> getActionsConfirmPreference();

  boolean isControlConfirm();

  boolean isShowCommentPost();

  void setShowCommentPost(boolean value);

  boolean isShowUrgency();

  Preference<Boolean> getShowUrgencyPreference();

  boolean isOnlyUrgent();

  void setOnlyUrgent(boolean value);

  Set<String> getJournals();

  Preference<Set<String>> getJournalsPreference();

  Set<String> getYears();

  Preference<Set<String>> getYearsPreference();

  String getPrevDialogComment();

  void setPrevDialogComment(String value);

  String getInfocardFontSize();

  void setInfocardFontSize(String value);

  String getStartPage();

  String getImageLoadPeriod();

  String getImageDeletePeriod();

  String getStartJournal();

  boolean isShowWithoutProject();

  boolean isHidePrimaryConsideration();

  boolean isHideButtons();

  boolean isShowDecisionDateUpdate();

  boolean isShowDecisionChangeFont();

  boolean isShowOrigin();

  boolean isShowChangeSigner();

  boolean isShowCreateDecisionPost();

  boolean isShowApproveOnPrimary();

  String getMaxImageSize();

  void setMaxImageSize(String value);

  boolean isDebugEnabled();

  boolean isStartLoadData();

  void setStartLoadData(boolean value);

  AuthType getAuthType();

  void setAuthType(AuthType value);

  Preference<AuthType> getAuthTypePreference();

  boolean isOnline();

  void setOnline(Boolean value);

  Preference<Boolean> getOnlinePreference();

  boolean isFavoritesLoaded();

  void setFavoritesLoaded(Boolean value);

  boolean isProcessedLoaded();

  void setProcessedLoaded(Boolean value);

  int getImageIndex();

  void setImageIndex(int value);

  boolean isUnauthorized();

  void setUnauthorized(Boolean value);

  Preference<Boolean> getUnauthorizedPreference();

  boolean isOrganizationFilterActive();

  void setOrganizationFilterActive(boolean value);

  Set<String> getOrganizationFilterSelection();

  void setOrganizationFilterSelection(Set<String> value);

  Set<String> getNotificatedJournals();

  boolean isSubstituteMode();

  Preference<Boolean> getSubstituteModePreference();

  void setSubstituteMode(boolean value);

  String getOldLogin();

  void setOldLogin(String value);

  String getOldCurrentUserId();

  void setOldCurrentUserId(String value);

  String getOldCurrentUser();

  void setOldCurrentUser(String value);

  String getOldCurrentUserOrganization();

  void setOldCurrentUserOrganization(String value);

  String getOldCurrentUserImage();

  void setOldCurrentUserImage(String value);

  String getColleagueId();

  void setColleagueId(String value);

  boolean isUpdateAuthStarted();

  void setUpdateAuthStarted(boolean value);

  boolean isTabChanged();

  void setTabChanged(boolean value);

  boolean isStartRegularRefresh();

  void setStartRegularRefresh(boolean value);

  long getLastClickTime();

  void setLastClickTime(long value);
}
