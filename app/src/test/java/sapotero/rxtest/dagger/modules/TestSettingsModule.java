package sapotero.rxtest.dagger.modules;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.DataScope;
import sapotero.rxtest.utils.Settings;

import static org.mockito.Mockito.mock;

@Module
public class TestSettingsModule {
  @Provides
  @DataScope
  Settings provideSettings() {
    return mock(Settings.class);
  }
}
