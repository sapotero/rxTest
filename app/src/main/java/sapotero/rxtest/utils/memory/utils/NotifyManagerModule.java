package sapotero.rxtest.utils.memory.utils;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.managers.notifications.NotifyManager;

@Module
public final class NotifyManagerModule {

  @Provides
  @ManagerScope
  NotifyManager provideNotifyManager(){
    return new NotifyManager();
  }
}
