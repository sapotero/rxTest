package sapotero.rxtest.application.components;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.events.utils.SubscriptionsModule;
import sapotero.rxtest.jobs.bus.BaseJob;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.LoginActivity;
import sapotero.rxtest.views.activities.MainActivity;
import sapotero.rxtest.views.activities.SettingsActivity;
import sapotero.rxtest.views.adapters.DecisionAdapter;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.services.AuthService;

@Singleton
@Component(modules = {
  EsdModule.class,
  JobModule.class,
  SubscriptionsModule.class,
  OkHttpModule.class,
  SettingsModule.class
})

public interface EsdComponent {
  void inject(LoginActivity activity);
  void inject(MainActivity  activity);
  void inject(InfoActivity  activity);
  void inject(SettingsActivity activity);
  void inject(DecisionConstructorActivity activity);

  void inject(AuthService service);
  void inject(DecisionAdapter activity);

  void inject(DecisionFragment activity);

  void inject(BaseJob job);

  Application application();
}