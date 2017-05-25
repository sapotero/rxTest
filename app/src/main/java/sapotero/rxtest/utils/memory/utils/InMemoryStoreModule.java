package sapotero.rxtest.utils.memory.utils;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.utils.memory.InMemoryDocumentStorage;


@Module
public final class InMemoryStoreModule {

  @Provides
  @Inject
  @ManagerScope
  InMemoryDocumentStorage provideJobModule() {
    return new InMemoryDocumentStorage();
  }

}