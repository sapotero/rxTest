package sapotero.rxtest.application.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.scopes.DataScope;

@Module
public final class EsdModule {
  private final Context context;

  public EsdModule() {
    context = EsdApplication.getInstance().getApplicationContext();
  }

  @Provides
  @DataScope
  Context provideContext() {
    return context;
  }
}