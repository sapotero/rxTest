package sapotero.rxtest.db.mapper.utils;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.DataScope;

@Module
public final class MappersModule {

  @Provides
  @DataScope
  Mappers provideMappers() {
    Mappers mappers = new Mappers();
    return mappers;
  }
}
