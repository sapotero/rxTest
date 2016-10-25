package sapotero.rxtest.db.requery;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.rx.RxSupport;
import io.requery.rx.SingleEntityStore;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;

@Module
public final class RequeryDbModule {

  @Provides
  @Singleton
  SingleEntityStore<Persistable> provideDatabase(Context context) {
    DatabaseSource source = new DatabaseSource(context, Models.DEFAULT, 1);

    Configuration configuration = source.getConfiguration();
    SingleEntityStore<Persistable> dataStore = RxSupport.toReactiveStore( new EntityDataStore<Persistable>(configuration) );
  return dataStore;
  }
}