package sapotero.rxtest.managers.db.utils;


import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.managers.db.managers.DBDocumentManager;

@Module
public final class DBDocumentManagerModule {

  @NonNull
  @Provides
  @Singleton
  DBDocumentManager provideDBDocumentManager() {
    return new DBDocumentManager();
  }
}