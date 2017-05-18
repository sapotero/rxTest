package sapotero.rxtest.utils.memory.utils;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ManagerScope;


@Module
public final class InMemoryStoreModule {

  @Provides
  @ManagerScope
  InMemoryStoreModule provideJobModule(Context context) {
    return new InMemoryStoreModule();
  }

}