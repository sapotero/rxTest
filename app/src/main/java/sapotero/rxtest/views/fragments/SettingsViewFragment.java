package sapotero.rxtest.views.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;

public class SettingsViewFragment extends PreferenceFragmentCompat {
  private CompositeSubscription subscriptions;
  @Inject RxSharedPreferences settings;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.settings_view);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EsdApplication.getComponent( getContext() ).inject(this);

    subscriptions = new CompositeSubscription();
    subscriptions.add(
      settings.getBoolean("settings_view_show_urgency").asObservable().subscribe( active -> {
        findPreference("settings_view_only_urgent").setEnabled(active);
        settings.getBoolean("settings_view_only_urgent").set(active);
      })
    );
  }

  @Override
  public void onResume() {
    super.onResume();
    subscriptions = new CompositeSubscription();
  }
}
