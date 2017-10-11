package sapotero.rxtest.managers.toolbar;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.DataScope;

@Module
public class ToolbarManagerModule {

  @Provides
  @DataScope
  ToolbarManager provideToolbarManager() {
    return new ToolbarManager();
  }
}
