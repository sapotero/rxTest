package sapotero.rxtest.views.managers.db.utils;


import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.views.managers.db.managers.DBDocumentManager;

@Module
public final class DBDocumentManagerModule {

  @NonNull
  @Provides
  @Singleton
  DBDocumentManager provideDBDocumentManager(Context context) {
    return new DBDocumentManager(context);
  }
}