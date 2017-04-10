package sapotero.rxtest.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.facebook.stetho.Stetho;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.annotations.AnnotationTest;
import sapotero.rxtest.application.components.DaggerEsdComponent;
import sapotero.rxtest.application.components.EsdComponent;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.utils.padeg.TestString;
import timber.log.Timber;


@ReportsCrashes(
  mailTo = "esapozhnikov@n-core.ru",
  mode = ReportingInteractionMode.TOAST,
  resToastText = R.string.crashed)

public final class EsdApplication extends Application {

  public static EsdComponent mainComponent;
  public Application app;

  @SuppressLint("StaticFieldLeak")
  private static Context context;

  private static String username;

  @Inject RxSharedPreferences settings;


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

    Runtime rt = Runtime.getRuntime();
    long maxMemory = rt.maxMemory();
    Timber.e( "MAX Memory Available: %s", Long.toString(maxMemory/1024/1024) );


    Stetho.Initializer initializer = Stetho.newInitializerBuilder(this)
        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(getApplicationContext()))
        .build();
    Stetho.initialize(initializer);



    mainComponent = DaggerEsdComponent.builder().esdModule(new EsdModule(this)).build();
    app = this;

    context=getApplicationContext();

    AnnotationTest.getInstance();
    getComponent(this).inject(this);

    settings.getString("login").asObservable().subscribe(name -> {
      Timber.e( "USERNAME: %s", name );
      username = name;
    });

    try {
      TestString strings = new TestString();
      strings.declAll();

      List<TestString.ResultItem> results = strings.getResultItems();

      List<String> items = new ArrayList<>();

      for (TestString.ResultItem result : results ) {
        items.add( String.format(" %s - %s, %s \n", result.getFio(), result.getAppointment(), result.getOffice() ) );
      }

      Timber.e( "Items: \n%s\n", items );
    } catch (Exception e) {
      e.printStackTrace();
    }


  }

  public static Context getContext(){
    return context;
  }

  public static EsdComponent getComponent(Context context) {
    return ((EsdApplication) context.getApplicationContext()).mainComponent;
  }
}
