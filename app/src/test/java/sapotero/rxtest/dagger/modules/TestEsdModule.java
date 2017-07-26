package sapotero.rxtest.dagger.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.DataScope;

import static org.mockito.Mockito.mock;

@Module
public class TestEsdModule {
  @Provides
  @DataScope
  Context provideContext() {
    return mock(Context.class);
  }
}
