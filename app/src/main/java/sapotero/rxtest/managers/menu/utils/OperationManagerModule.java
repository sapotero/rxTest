package sapotero.rxtest.managers.menu.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.managers.menu.OperationManager;

@Module
public final class OperationManagerModule {

  @NonNull
  @Provides
  @ManagerScope
  OperationManager provideOperationManager(Context context, RxSharedPreferences rxSharedPreferences) {
    return new OperationManager( context, rxSharedPreferences );
  }
}
