package sapotero.rxtest.utils.memory.utils;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.utils.memory.MemoryStore;


@Module
public final class MemoryStoreModule {

  @Provides
  @Inject
  @ManagerScope
  MemoryStore provideJobModule() {
    return new MemoryStore();
  }

}