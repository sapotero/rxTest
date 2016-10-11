package sapotero.rxtest.events.utils;

import android.content.Context;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.subscriptions.CompositeSubscription;

@Module
public final class SubscriptionsModule {

  @Provides
  @Singleton
  CompositeSubscription provideSubscriptionsModule(Context context) {

    return new CompositeSubscription();
  }

}