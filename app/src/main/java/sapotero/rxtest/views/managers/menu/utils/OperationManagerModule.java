package sapotero.rxtest.views.managers.menu.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.managers.menu.OperationManager;


@Module
public final class OperationManagerModule {

  @NonNull
  @Provides
  @Singleton
  OperationManager provideOperationManager(Context context) {
    return new OperationManager( EsdApplication.getContext() );
  }
}
