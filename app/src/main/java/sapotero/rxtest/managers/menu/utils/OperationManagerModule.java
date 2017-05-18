package sapotero.rxtest.managers.menu.utils;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.utils.Settings;

@Module
public final class OperationManagerModule {

  @NonNull
  @Provides
  @ManagerScope
  OperationManager provideOperationManager(Settings settings) {
    return new OperationManager( settings );
  }
}
