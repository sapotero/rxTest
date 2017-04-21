package sapotero.rxtest.application.modules;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.db.requery.utils.validation.ValidationModule;
import sapotero.rxtest.events.utils.SubscriptionsModule;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.db.utils.DBDocumentManagerModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;

@Module(
  includes = {
//    DbModule.class,
    RequeryDbModule.class,
    JobModule.class,
    SubscriptionsModule.class,
    OkHttpModule.class,
    SettingsModule.class,
    DBDocumentManagerModule.class,
    QueueManagerModule.class,
    ValidationModule.class
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