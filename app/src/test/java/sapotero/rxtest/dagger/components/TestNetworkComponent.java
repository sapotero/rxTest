package sapotero.rxtest.dagger.components;

import dagger.Subcomponent;
import sapotero.rxtest.application.components.NetworkComponent;
import sapotero.rxtest.application.scopes.NetworkScope;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.services.task.CheckNetworkTask;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;

@NetworkScope
@Subcomponent(modules = {
  OkHttpModule.class
})

public interface TestNetworkComponent extends NetworkComponent {
  TestManagerComponent plusTestManagerComponent(
          JobModule jobModule,
          QueueManagerModule queueManagerModule,
          OperationManagerModule operationManagerModule);

}
