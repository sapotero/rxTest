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
import sapotero.rxtest.application.components.DaggerJobManagerComponent;
import sapotero.rxtest.application.components.DaggerManagerComponent;
import sapotero.rxtest.application.components.DaggerNetworkComponent;
import sapotero.rxtest.application.components.DaggerOperationManagerComponent;
import sapotero.rxtest.application.components.DaggerValidationComponent;
import sapotero.rxtest.application.components.DataComponent;
import sapotero.rxtest.application.components.JobManagerComponent;
import sapotero.rxtest.application.components.ManagerComponent;
import sapotero.rxtest.application.components.NetworkComponent;
import sapotero.rxtest.application.components.OperationManagerComponent;
import sapotero.rxtest.application.components.ValidationComponent;
import sapotero.rxtest.application.config.Constant;
import timber.log.Timber;


@ReportsCrashes(
  mailTo = "esapozhnikov@n-core.ru",
  mode = ReportingInteractionMode.TOAST,
  resToastText = R.string.crashed)

public final class EsdApplication extends Application {

  private static EsdApplication instance;

  private static DataComponent dataComponent;
  private static ValidationComponent validationComponent;
  private static NetworkComponent networkComponent;
  private static ManagerComponent managerComponent;
  private static JobManagerComponent jobManagerComponent;
  private static OperationManagerComponent operationManagerComponent;

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

    dataComponent = DaggerDataComponent.builder().build();
    validationComponent = DaggerValidationComponent.builder().build();
    networkComponent = DaggerNetworkComponent.builder().build();
    managerComponent = DaggerManagerComponent.builder().build();
    jobManagerComponent = DaggerJobManagerComponent.builder().build();
    operationManagerComponent = DaggerOperationManagerComponent.builder().build();

    AnnotationTest.getInstance();
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

  public static ManagerComponent getManagerComponent() {
    return managerComponent;
  }

  public static JobManagerComponent getJobManagerComponent() {
    return jobManagerComponent;
  }

  public static OperationManagerComponent getOperationManagerComponent() {
    return operationManagerComponent;
  }

  public static EsdApplication getInstance() {
    return instance;
  }
}
