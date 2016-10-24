package sapotero.rxtest.db.Storio;


import android.content.Context;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.db.Storio.entities.Auth;
import sapotero.rxtest.db.Storio.entities.AuthSQLiteTypeMapping;
import sapotero.rxtest.db.Storio.entities.Doc;
import sapotero.rxtest.db.Storio.entities.DocSQLiteTypeMapping;
import sapotero.rxtest.db.Storio.entities.DocWithSigner;
import sapotero.rxtest.db.Storio.entities.Signer;
import sapotero.rxtest.db.Storio.entities.SignerSQLiteTypeMapping;
import sapotero.rxtest.db.Storio.resolvers.DocWithSignerDeleteResolver;
import sapotero.rxtest.db.Storio.resolvers.DocWithSignerGetResolver;
import sapotero.rxtest.db.Storio.resolvers.DocWithSignerPutResolver;

@Module
public class StroioDbModule {
  @Provides @NonNull @Singleton
  public StorIOSQLite provideStorIOSQLite(@NonNull StorioDbOpenHelper sqLiteOpenHelper) {
    return DefaultStorIOSQLite.builder()
      .sqliteOpenHelper(sqLiteOpenHelper)
      .addTypeMapping(Auth.class, new AuthSQLiteTypeMapping())
      .addTypeMapping(Doc.class,  new DocSQLiteTypeMapping() )
      .addTypeMapping(Signer.class,  new SignerSQLiteTypeMapping() )
      .addTypeMapping(DocWithSigner.class, SQLiteTypeMapping.<DocWithSigner>builder()
        .putResolver(new DocWithSignerPutResolver())
        .getResolver(new DocWithSignerGetResolver())
        .deleteResolver(new DocWithSignerDeleteResolver())
        .build())
      .build();
  }

  @Provides @NonNull @Singleton
  public StorioDbOpenHelper provideSQLiteOpenHelper(@NonNull Context context) {
    return new StorioDbOpenHelper(context);
  }

}
