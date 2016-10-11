package sapotero.rxtest;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

public final class EsdApplication extends Application {
  public static Integer POSITION = 0;
  public static String HOST     = "http://mobile.sed.a-soft.org/";
  public static String LOGIN    = "LOGIN";
  public static String PASSWORD = "PASSWORD";
  public static String TOKEN    = "TOKEN";

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
