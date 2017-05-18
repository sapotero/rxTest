package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.Settings;
import timber.log.Timber;

public class SettingsViewFragment extends PreferenceFragmentCompat {
  private CompositeSubscription subscriptions;

  @Inject Context context;
  @Inject Settings settings;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.settings_view);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EsdApplication.getDataComponent().inject(this);

    Timber.tag("SETTINGS").d("settings_view_journals %s", settings.getJournals() );

    findPreference( context.getResources().getString(R.string.show_comment_post_key) )
            .setDependency( context.getResources().getString(R.string.actions_confirm_key) );

    subscriptions = new CompositeSubscription();
    subscriptions.add(
      settings.getShowUrgencyPreference().asObservable().subscribe(active -> {
        findPreference( context.getResources().getString(R.string.only_urgent_key) ).setEnabled(active);
        settings.setOnlyUrgent(active);
      },Timber::e)
    );

    // resolved https://tasks.n-core.ru/browse/MVDESD-13341
    // При отклонении проекта не отображается окно ввода комментария
    subscriptions.add(
      settings.getActionsConfirmPreference()
        .asObservable()
        .subscribe(
          active -> {
            if (active){
              settings.setShowCommentPost(true);
              findPreference( context.getResources().getString(R.string.show_comment_post_key) ).setEnabled(true);
            } else {
              settings.setShowCommentPost(false);
              findPreference( context.getResources().getString(R.string.show_comment_post_key) ).setEnabled(false);
            }
          },
          Timber::e
        )
    );
  }

  @Override
  public void onResume() {
    super.onResume();
    subscriptions = new CompositeSubscription();

    // Enable First run flag preference only if not first run
    boolean isFirstRun = settings.isFirstRun();
    Preference firstFlagPreference = findPreference(Settings.FIRST_RUN_KEY);
    if (firstFlagPreference != null) {
      firstFlagPreference.setEnabled(!isFirstRun);
    }
  }
}
