package sapotero.rxtest.application;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import sapotero.rxtest.R;
import sapotero.rxtest.annotations.AnnotationTest;
import sapotero.rxtest.application.components.DaggerDataComponent;
import sapotero.rxtest.application.components.DataComponent;
import sapotero.rxtest.application.components.ManagerComponent;
import sapotero.rxtest.application.components.NetworkComponent;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;
import sapotero.rxtest.utils.transducers.ReduceTest;
import timber.log.Timber;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APPLICATION_LOG;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.BUILD;
import static org.acra.ReportField.BUILD_CONFIG;
import static org.acra.ReportField.CRASH_CONFIGURATION;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.DEVICE_FEATURES;
import static org.acra.ReportField.DEVICE_ID;
import static org.acra.ReportField.DISPLAY;
import static org.acra.ReportField.DROPBOX;
import static org.acra.ReportField.DUMPSYS_MEMINFO;
import static org.acra.ReportField.ENVIRONMENT;
import static org.acra.ReportField.EVENTSLOG;
import static org.acra.ReportField.FILE_PATH;
import static org.acra.ReportField.INITIAL_CONFIGURATION;
import static org.acra.ReportField.INSTALLATION_ID;
import static org.acra.ReportField.IS_SILENT;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.MEDIA_CODEC_LIST;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.RADIOLOG;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.SETTINGS_GLOBAL;
import static org.acra.ReportField.SETTINGS_SECURE;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.STACK_TRACE_HASH;
import static org.acra.ReportField.THREAD_DETAILS;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_CRASH_DATE;
import static org.acra.ReportField.USER_EMAIL;
import static org.acra.ReportField.USER_IP;

// на проде отправлем ошибки сюда
// rgiliazov6@mvd.ru
// в бою android-app-logs.sed.mvd.ru

@ReportsCrashes(formUri = "http://android-app-logs.sed.mvd.ru/send",
//@ReportsCrashes(formUri = "http://10.0.32.77/send",
  mailTo = "rgiliazov6@mvd.ru",
  customReportContent = {
    REPORT_ID,
    APP_VERSION_CODE,
    APP_VERSION_NAME,
    PACKAGE_NAME,
    FILE_PATH,
    PHONE_MODEL,
    ANDROID_VERSION,
    BUILD,
    BRAND,
    PRODUCT,
    TOTAL_MEM_SIZE,
    AVAILABLE_MEM_SIZE,
    BUILD_CONFIG,
    CUSTOM_DATA,
    STACK_TRACE,
    STACK_TRACE_HASH,
    INITIAL_CONFIGURATION,
    CRASH_CONFIGURATION,
    DISPLAY,
    USER_COMMENT,
    USER_APP_START_DATE,
    USER_CRASH_DATE,
    DUMPSYS_MEMINFO,
    DROPBOX,
    LOGCAT,
    EVENTSLOG,
    RADIOLOG,
    IS_SILENT,
    DEVICE_ID,
    INSTALLATION_ID,
    USER_EMAIL,
    DEVICE_FEATURES,
    ENVIRONMENT,
    SETTINGS_SECURE,
    SETTINGS_GLOBAL,
    SHARED_PREFERENCES,
    APPLICATION_LOG,
    MEDIA_CODEC_LIST,
    THREAD_DETAILS,
    USER_IP
  },
  reportType = HttpSender.Type.JSON,
  httpMethod = HttpSender.Method.POST,
  mode = ReportingInteractionMode.TOAST,
  resToastText = R.string.crash_toast_text
)

public final class EsdApplication extends Application {

  private static EsdApplication application;

  private static DataComponent dataComponent;
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

    Stetho.Initializer initializer = Stetho.newInitializerBuilder(this)
        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(getApplicationContext()))
        .build();
    Stetho.initialize(initializer);

    application = this;

    initComponents();

    AnnotationTest.getInstance();


    String[] array = new String[] {"1", "2", "2", "2", "222", "20"};
    ReduceTest.calculate(array);
    
  }

  private void initComponents() {
    dataComponent = DaggerDataComponent.builder().build();
    networkComponent = dataComponent.plusNetworkComponent(new OkHttpModule());
    managerComponent = networkComponent.plusManagerComponent(
            new JobModule(), new QueueManagerModule(), new OperationManagerModule() );
  }

  public static DataComponent getDataComponent() {
    return dataComponent;
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
