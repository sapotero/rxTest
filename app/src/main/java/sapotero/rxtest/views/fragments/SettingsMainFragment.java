package sapotero.rxtest.views.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import sapotero.rxtest.R;

public class SettingsMainFragment extends PreferenceFragmentCompat {
  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.settings_main);
  }
}
