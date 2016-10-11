package sapotero.rxtest;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.db.utils.DbModule;

@Module(
  includes = {
    DbModule.class,
  }
)
public final class EsdModule {
  private final Application application;
  private final Context context;

  EsdModule(Application application) {
    this.application = application;
    this.context = application.getApplicationContext();
  }

  @Provides
  @Singleton
  Application provideApplication() {
    return application;
  }

  @Provides
  public Context provideContext() {
    return context;
  }
}