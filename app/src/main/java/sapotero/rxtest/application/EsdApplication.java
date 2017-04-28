package sapotero.rxtest.application;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import sapotero.rxtest.R;
import sapotero.rxtest.annotations.AnnotationTest;
import sapotero.rxtest.application.components.DaggerDataComponent;
import sapotero.rxtest.application.components.DaggerEsdComponent;
import sapotero.rxtest.application.components.DaggerNetworkComponent;
import sapotero.rxtest.application.components.DaggerValidationComponent;
import sapotero.rxtest.application.components.DataComponent;
import sapotero.rxtest.application.components.EsdComponent;
import sapotero.rxtest.application.components.NetworkComponent;
import sapotero.rxtest.application.components.ValidationComponent;
import sapotero.rxtest.application.config.Constant;
import timber.log.Timber;


@ReportsCrashes(
  mailTo = "esapozhnikov@n-core.ru",
  mode = ReportingInteractionMode.TOAST,
  resToastText = R.string.crashed)

public final class EsdApplication extends Application {

  private static EsdApplication instance;
  private static EsdComponent mainComponent;
  private static DataComponent dataComponent;
  private static ValidationComponent validationComponent;
  private static NetworkComponent networkComponent;

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
    dataComponent = DaggerDataComponent.builder().build();
    validationComponent = DaggerValidationComponent.builder().build();
    networkComponent = DaggerNetworkComponent.builder().build();

    AnnotationTest.getInstance();
  }

  public static EsdComponent getComponent() {
    return mainComponent;
  }

  public static DataComponent getDataComponent() {
    return dataComponent;
  }

  public static ValidationComponent getValidationComponent() {
    return validationComponent;
  }

  public static NetworkComponent getNetworkComponent() {
    return networkComponent;
  }

  public static EsdApplication getInstance() {
    return instance;
  }
}
