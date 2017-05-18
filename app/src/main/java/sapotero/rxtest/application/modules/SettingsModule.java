package sapotero.rxtest.application.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.DataScope;
import sapotero.rxtest.utils.Settings;

@Module
public final class SettingsModule {

  @Provides
  @DataScope
  RxSharedPreferences provideSettingsModule(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return RxSharedPreferences.create(preferences);
  }

  @Provides
  @DataScope
  Settings provideSettings(Context context, RxSharedPreferences rxSharedPreferences) {
    Settings settings = new Settings(context, rxSharedPreferences);
    return settings;
  }
}
