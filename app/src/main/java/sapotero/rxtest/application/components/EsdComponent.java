package sapotero.rxtest.application.components;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.events.utils.SubscriptionsModule;
import sapotero.rxtest.jobs.bus.BaseJob;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.LoginActivity;
import sapotero.rxtest.views.activities.MainActivity;

@Singleton
@Component(modules = {EsdModule.class, JobModule.class, SubscriptionsModule.class, OkHttpModule.class})

public interface EsdComponent {
  void inject(InfoActivity  activity);
  void inject(LoginActivity activity);
  void inject(MainActivity  activity);

  void inject(BaseJob job);

  Application application();
}