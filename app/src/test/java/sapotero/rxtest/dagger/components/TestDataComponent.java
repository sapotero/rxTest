package sapotero.rxtest.dagger.components;

import dagger.Component;
import sapotero.rxtest.application.components.DataComponent;
import sapotero.rxtest.application.scopes.DataScope;
import sapotero.rxtest.dagger.modules.TestEsdModule;
import sapotero.rxtest.dagger.modules.TestRequeryDbModule;
import sapotero.rxtest.dagger.modules.TestSettingsModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.utils.memory.utils.MemoryStoreModule;

@DataScope
@Component(modules = {
  TestEsdModule.class,
  TestSettingsModule.class,
  TestRequeryDbModule.class,
  MemoryStoreModule.class,
})

public interface TestDataComponent extends DataComponent {
  TestNetworkComponent plusTestNetworkComponent(OkHttpModule okHttpModule);
}
