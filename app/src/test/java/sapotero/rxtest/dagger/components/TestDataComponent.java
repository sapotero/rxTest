package sapotero.rxtest.dagger.components;

import dagger.Component;
import sapotero.rxtest.application.components.DataComponent;
import sapotero.rxtest.application.scopes.DataScope;
import sapotero.rxtest.dagger.modules.TestEsdModule;
import sapotero.rxtest.dagger.modules.TestRequeryDbModule;
import sapotero.rxtest.dagger.modules.TestSettingsModule;
import sapotero.rxtest.mapper.AssistantMapperTest;
import sapotero.rxtest.mapper.ColleagueMapperTest;
import sapotero.rxtest.mapper.DocumentMapperTest;
import sapotero.rxtest.mapper.FavoriteUserMapperTest;
import sapotero.rxtest.mapper.PrimaryConsiderationMapperTest;
import sapotero.rxtest.mapper.TemplateMapperTest;
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

  void inject(PrimaryConsiderationMapperTest primaryConsiderationMapperTest);

  void inject(DocumentMapperTest documentMapperTest);

  void inject(AssistantMapperTest assistantMapperTest);

  void inject(ColleagueMapperTest colleagueMapperTest);

  void inject(FavoriteUserMapperTest favoriteUserMapperTest);

  void inject(TemplateMapperTest templateMapperTest);
}
