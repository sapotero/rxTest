package sapotero.rxtest.application.components;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.jobs.bus.BaseJob;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.DataLoaderManager;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;

@Singleton
@Component(modules = {
  EsdModule.class,
  SettingsModule.class,
  RequeryDbModule.class,
  OkHttpModule.class,
  JobModule.class,
  QueueManagerModule.class,
})

public interface NetManagerComponent {
  void inject(BaseJob job);
  void inject(DataLoaderManager dataLoaderManager);
  void inject(AbstractCommand abstractCommand);
}
