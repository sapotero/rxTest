package sapotero.rxtest.application;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import sapotero.rxtest.R;
import sapotero.rxtest.annotations.AnnotationTest;
import sapotero.rxtest.application.components.DaggerDataComponent;
import sapotero.rxtest.application.components.DataComponent;
import sapotero.rxtest.application.components.ManagerComponent;
import sapotero.rxtest.application.components.NetworkComponent;
import sapotero.rxtest.application.components.ValidationComponent;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.db.requery.utils.validation.ValidationModule;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;
import timber.log.Timber;

// на проде отправлем ошибки сюда
// rgiliazov6@mvd.ru

@ReportsCrashes(
  mailTo = "esapozhnikov@n-core.ru",
  mode = ReportingInteractionMode.TOAST,
  resToastText = R.string.crashed)

public final class EsdApplication extends Application {

  private static EsdApplication application;

  private static DataComponent dataComponent;
  private static ValidationComponent validationComponent;
  private static NetworkComponent networkComponent;
  private static ManagerComponent managerComponent;

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

//    Stetho.Initializer initializer = Stetho.newInitializerBuilder(this)
//        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
//        .enableDumpapp(Stetho.defaultDumperPluginsProvider(getApplicationContext()))
//        .build();
//    Stetho.initialize(initializer);

    application = this;

    initComponents();

    AnnotationTest.getInstance();
  }

  private void initComponents() {
    dataComponent = DaggerDataComponent.builder().build();
    validationComponent = dataComponent.plusValidationComponent(new ValidationModule());
    networkComponent = dataComponent.plusNetworkComponent(new OkHttpModule());
    managerComponent = networkComponent.plusManagerComponent(
            new JobModule(), new QueueManagerModule(), new OperationManagerModule() );
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

  public static EsdApplication getApplication() {
    return application;
  }
}
