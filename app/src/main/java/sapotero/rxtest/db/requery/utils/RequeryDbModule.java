package sapotero.rxtest.db.requery.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.meta.EntityModel;
import io.requery.rx.RxSupport;
import io.requery.rx.SingleEntityStore;
import io.requery.sql.Configuration;
import io.requery.sql.ConfigurationBuilder;
import io.requery.sql.EntityDataStore;
import io.requery.sql.SchemaModifier;
import io.requery.sql.TableCreationMode;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.db.requery.models.Models;

@Module
public final class RequeryDbModule {

  @NonNull
  @Provides
  @Singleton
  SingleEntityStore<Persistable> provideDatabase(Context context) {
    DatabaseSource source = new DatabaseSource(context, Models.DEFAULT, 6);

    EntityModel model = Models.DEFAULT;
    Configuration configuration = new ConfigurationBuilder(source, model)
      .setStatementCacheSize(10)
      .setBatchUpdateSize(10)
      .setWriteExecutor(
        Executors.newSingleThreadExecutor()
      )
      .build();

    SchemaModifier schemaModifier = new SchemaModifier(configuration);


    schemaModifier.createTables(Constant.DEBUG ? TableCreationMode.CREATE_NOT_EXISTS : TableCreationMode.DROP_CREATE );

    return RxSupport.toReactiveStore( new EntityDataStore<Persistable>(configuration) );
  }
}