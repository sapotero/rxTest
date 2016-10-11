package sapotero.rxtest.jobs.utils;

import android.content.Context;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.di.DependencyInjector;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.jobs.bus.BaseJob;
import sapotero.rxtest.application.EsdApplication;

@Module
public final class JobModule {

  @Provides
  @Singleton
  JobManager provideJobModule(Context context) {
    Configuration.Builder builder = new Configuration.Builder(context)
      .minConsumerCount(1)
      .maxConsumerCount(3)
      .loadFactor(3)
      .injector(new DependencyInjector() {
        @Override
        public void inject(Job job) {
          EsdApplication.mainComponent.inject((BaseJob) job);
        }
      })
      .consumerKeepAlive(120);

    return new JobManager(builder.build());
  }

}