package sapotero.rxtest.dagger.components;

import dagger.Subcomponent;
import sapotero.rxtest.application.components.ManagerComponent;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.memory.MemoryStoreTest;
import sapotero.rxtest.utils.memory.utils.MemoryStoreModule;
import sapotero.rxtest.utils.memory.utils.NotifyManagerModule;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;

@ManagerScope
@Subcomponent(modules = {
  JobModule.class,
  QueueManagerModule.class,
  OperationManagerModule.class,
  MemoryStoreModule.class,
  NotifyManagerModule.class
})

public interface TestManagerComponent extends ManagerComponent {
  void inject(MemoryStoreTest test);
}