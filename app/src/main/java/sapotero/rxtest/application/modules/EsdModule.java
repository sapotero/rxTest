package sapotero.rxtest.application.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.EsdApplication;

@Module
public final class EsdModule {
  private final Context context;

  public EsdModule() {
    context = EsdApplication.getInstance().getApplicationContext();
  }

  @Provides
  @Singleton
  Context provideContext() {
    return context;
  }
}