package sapotero.rxtest.dagger.components;

import dagger.Component;
import dagger.Subcomponent;
import sapotero.rxtest.application.components.ManagerComponent;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.dagger.modules.TestEsdModule;
import sapotero.rxtest.dagger.modules.TestSettingsModule;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.mapper.Test;
import sapotero.rxtest.utils.memory.utils.MemoryStoreModule;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;

@ManagerScope
@Subcomponent(modules = {
  JobModule.class,
  QueueManagerModule.class,
  OperationManagerModule.class,
  MemoryStoreModule.class
})

public interface TestManagerComponent extends ManagerComponent {
  void inject(Test test);
}
