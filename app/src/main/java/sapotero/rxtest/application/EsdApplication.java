package sapotero.rxtest.application;

import android.app.Application;
import android.content.Context;

import sapotero.rxtest.BuildConfig;
import sapotero.rxtest.application.components.DaggerEsdComponent;
import sapotero.rxtest.application.components.EsdComponent;
import sapotero.rxtest.application.modules.EsdModule;
import timber.log.Timber;

public final class EsdApplication extends Application {

  public static EsdComponent mainComponent;
  public Application app;

  @Override public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }

    mainComponent = DaggerEsdComponent.builder().esdModule(new EsdModule(this)).build();
    app = this;
  }

  public static EsdComponent getComponent(Context context) {
    return ((EsdApplication) context.getApplicationContext()).mainComponent;
  }
}
