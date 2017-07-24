package sapotero.rxtest.dagger.modules;

import android.content.Context;

import org.mockito.Mockito;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.DataScope;

@Module
public class TestEsdModule {
  @Provides
  @DataScope
  Context provideContext() {
    return Mockito.mock(Context.class);
  }
}
