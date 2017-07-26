package sapotero.rxtest.dagger.components;

import dagger.Component;
import sapotero.rxtest.application.components.DataComponent;
import sapotero.rxtest.application.scopes.DataScope;
import sapotero.rxtest.dagger.modules.TestEsdModule;
import sapotero.rxtest.dagger.modules.TestSettingsModule;
import sapotero.rxtest.db.mapper.AssistantMapper;
import sapotero.rxtest.db.mapper.ColleagueMapper;
import sapotero.rxtest.db.mapper.FavoriteUserMapper;
import sapotero.rxtest.db.mapper.PrimaryConsiderationMapper;
import sapotero.rxtest.db.mapper.TemplateMapper;
import sapotero.rxtest.db.mapper.utils.MappersModule;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.mapper.AssistantMapperTest;
import sapotero.rxtest.mapper.ColleagueMapperTest;
import sapotero.rxtest.mapper.DocumentMapperTest;
import sapotero.rxtest.mapper.FavoriteUserMapperTest;
import sapotero.rxtest.mapper.PrimaryConsiderationMapperTest;
import sapotero.rxtest.mapper.TemplateMapperTest;
import sapotero.rxtest.utils.memory.utils.MemoryStoreModule;

@DataScope
@Component(modules = {
  TestEsdModule.class,
  TestSettingsModule.class,
  RequeryDbModule.class,
  MemoryStoreModule.class,
  MappersModule.class
})

public interface TestDataComponent extends DataComponent {
  void inject(PrimaryConsiderationMapper primaryConsiderationMapper);
  void inject(PrimaryConsiderationMapperTest primaryConsiderationMapperTest);

  void inject(DocumentMapperTest documentMapperTest);

  void inject(AssistantMapper assistantMapper);
  void inject(AssistantMapperTest assistantMapperTest);

  void inject(ColleagueMapper colleagueMapper);
  void inject(ColleagueMapperTest colleagueMapperTest);

  void inject(FavoriteUserMapper favoriteUserMapperTest);
  void inject(FavoriteUserMapperTest favoriteUserMapperTest);

  void inject(TemplateMapper templateMapperTest);
  void inject(TemplateMapperTest templateMapperTest);
}
