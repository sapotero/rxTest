package sapotero.rxtest.application.components;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;

@Singleton
@Component(modules = {
  EsdModule.class,
  SettingsModule.class,
  RequeryDbModule.class,
  QueueManagerModule.class,
})

public interface QueueManagerComponent {
  void inject(MainService service);
}
