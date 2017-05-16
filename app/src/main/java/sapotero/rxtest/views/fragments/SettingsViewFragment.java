package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.Settings;
import timber.log.Timber;

public class SettingsViewFragment extends PreferenceFragmentCompat {
  private CompositeSubscription subscriptions;
  @Inject Context context;
  @Inject RxSharedPreferences settings;
  @Inject Settings settings2;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.settings_view);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EsdApplication.getDataComponent().inject(this);

    Timber.tag("SETTINGS").d("settings_view_journals %s", settings.getStringSet("settings_view_journals").get() );

    findPreference("settings_view_show_comment_post").setDependency( context.getResources().getString(R.string.actions_confirm_key) );

    subscriptions = new CompositeSubscription();
    subscriptions.add(
      settings.getBoolean("settings_view_show_urgency").asObservable().subscribe( active -> {
        findPreference("settings_view_only_urgent").setEnabled(active);
        settings.getBoolean("settings_view_only_urgent").set(active);
      },Timber::e)
    );

    // resolved https://tasks.n-core.ru/browse/MVDESD-13341
    // При отклонении проекта не отображается окно ввода комментария
    subscriptions.add(
      settings2.getActionsConfirmPreference()
        .asObservable()
        .subscribe(
          active -> {
            if (active){
              settings.getBoolean("settings_view_show_comment_post").set(true);
              findPreference("settings_view_show_comment_post").setEnabled(true);
            } else {
              settings.getBoolean("settings_view_show_comment_post").set(false);
              findPreference("settings_view_show_comment_post").setEnabled(false);
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
    boolean isFirstRun = settings2.isFirstRun();
    Preference firstFlagPreference = findPreference(Settings.FIRST_RUN_KEY);
    if (firstFlagPreference != null) {
      firstFlagPreference.setEnabled(!isFirstRun);
    }
  }
}
