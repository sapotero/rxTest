package sapotero.rxtest.dagger.modules;

import android.content.Context;
import android.support.annotation.NonNull;

import org.mockito.Mockito;

import dagger.Module;
import dagger.Provides;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.scopes.DataScope;

@Module
public final class TestRequeryDbModule {

  @Provides
  @DataScope
  SingleEntityStore<Persistable> provideDatabase(Context context) {

    SingleEntityStore<Persistable> dataStore = Mockito.mock(SingleEntityStore.class);




    return dataStore;
  }
}