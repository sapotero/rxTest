package sapotero.rxtest.dagger.modules;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.DataScope;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.TestSettings;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Module
public class TestSettingsModule {
  @Provides
  @DataScope
  ISettings provideSettings() {
    ISettings testSettings = mock(TestSettings.class);

    doCallRealMethod().when(testSettings).setLogin(anyString());
    when(testSettings.getLogin()).thenCallRealMethod();

    doCallRealMethod().when(testSettings).setCurrentUserId(anyString());
    when(testSettings.getCurrentUserId()).thenCallRealMethod();

    return testSettings;
  }
}
