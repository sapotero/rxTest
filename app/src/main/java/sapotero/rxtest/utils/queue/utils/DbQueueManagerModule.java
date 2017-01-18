package sapotero.rxtest.utils.queue.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.utils.queue.db.DbQueueManager;

@Module
public final class DbQueueManagerModule {

  @NonNull
  @Provides
  @Singleton
  DbQueueManager provideDbQueueManager(Context context) {
    return new DbQueueManager(context);
  }
}