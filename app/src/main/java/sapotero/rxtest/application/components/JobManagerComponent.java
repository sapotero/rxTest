package sapotero.rxtest.application.components;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.adapters.DecisionAdapter;

@Singleton
@Component(modules = {
  EsdModule.class,
  SettingsModule.class,
  RequeryDbModule.class,
  JobModule.class,
})

public interface JobManagerComponent {
  void inject(InfoActivity activity);
  void inject(DecisionAdapter adapter);
}
