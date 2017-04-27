package sapotero.rxtest.application;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import sapotero.rxtest.R;
import sapotero.rxtest.annotations.AnnotationTest;
import sapotero.rxtest.application.components.DaggerEsdComponent;
import sapotero.rxtest.application.components.EsdComponent;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.application.modules.EsdModule;
import timber.log.Timber;


@ReportsCrashes(
  mailTo = "esapozhnikov@n-core.ru",
  mode = ReportingInteractionMode.TOAST,
  resToastText = R.string.crashed)

public final class EsdApplication extends Application {

  public static EsdComponent mainComponent;

  private static EsdApplication instance;

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    ACRA.init(this);
  }

  @Override public void onCreate() {
    super.onCreate();

    Timber.plant(new Timber.DebugTree());

    if (Constant.DEBUG) {

//      if (LeakCanary.isInAnalyzerProcess(this)) {
//        return;
//      }
//      LeakCanary.install(this);

    }

    Stetho.Initializer initializer = Stetho.newInitializerBuilder(this)
        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(getApplicationContext()))
        .build();
    Stetho.initialize(initializer);

    instance = this;

    mainComponent = DaggerEsdComponent.builder().build();

    AnnotationTest.getInstance();
  }

  public static EsdComponent getComponent() {
    return mainComponent;
  }

  public static EsdApplication getInstance() {
    return instance;
  }
}
