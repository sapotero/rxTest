package sapotero.rxtest.utils.queue.utils;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.utils.queue.QueueManager;

@Module
public final class QueueManagerModule {

  @NonNull
  @Provides
  @ManagerScope
  QueueManager provideQueueManager() {
    return new QueueManager();
  }
}