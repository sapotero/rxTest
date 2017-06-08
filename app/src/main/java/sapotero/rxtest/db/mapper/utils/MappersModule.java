package sapotero.rxtest.db.mapper.utils;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.DataScope;
import sapotero.rxtest.utils.Settings;

@Module
public final class MappersModule {

  @Provides
  @DataScope
  Mappers provideMappers(Settings settings) {
    Mappers mappers = new Mappers(settings);
    return mappers;
  }
}
