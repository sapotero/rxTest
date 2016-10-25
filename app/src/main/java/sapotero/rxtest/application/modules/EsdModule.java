package sapotero.rxtest.application.modules;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.db.requery.RequeryDbModule;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.db.utils.DbModule;
import sapotero.rxtest.events.utils.SubscriptionsModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;

@Module(
  includes = {
    DbModule.class,
    RequeryDbModule.class,
    JobModule.class,
    SubscriptionsModule.class,
    OkHttpModule.class,
    SettingsModule.class
  }
)
public final class EsdModule {
  private final Application application;
  private final Context context;

  public EsdModule(Application application) {
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