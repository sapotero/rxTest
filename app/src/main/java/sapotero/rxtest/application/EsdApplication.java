package sapotero.rxtest.application;

import android.app.Application;
import android.content.Context;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.facebook.stetho.Stetho;

import javax.inject.Inject;

import sapotero.rxtest.annotations.AnnotationTest;
import sapotero.rxtest.application.components.DaggerEsdComponent;
import sapotero.rxtest.application.components.EsdComponent;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.application.modules.EsdModule;
import timber.log.Timber;

public final class EsdApplication extends Application {

  public static EsdComponent mainComponent;
  public Application app;
  private static Context context;

  private static String username;

  @Inject RxSharedPreferences settings;

  @Override public void onCreate() {
    super.onCreate();

    Timber.plant(new Timber.DebugTree());

    if (Constant.DEBUG) {

//      if (LeakCanary.isInAnalyzerProcess(this)) {
//        return;
//      }
//      LeakCanary.install(this);
//

    }

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


  }

  public static String getUserName(){
    return username;
  }

  public static Context getContext(){
    return context;
  }

  public static EsdComponent getComponent(Context context) {
    return ((EsdApplication) context.getApplicationContext()).mainComponent;
  }
}
