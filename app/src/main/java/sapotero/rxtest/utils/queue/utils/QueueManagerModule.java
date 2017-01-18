package sapotero.rxtest.utils.queue.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.utils.queue.QueueManager;

@Module
public final class QueueManagerModule {

  @NonNull
  @Provides
  @Singleton
  QueueManager provideQueueManager(Context context) {
    return new QueueManager(context);
  }
}