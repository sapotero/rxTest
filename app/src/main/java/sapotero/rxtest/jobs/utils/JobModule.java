package sapotero.rxtest.jobs.utils;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.jobs.bus.BaseJob;

@Module
public final class JobModule {

  @Provides
  @Singleton
  JobManager provideJobModule(Context context) {
    Configuration.Builder builder = new Configuration.Builder(context)
      .minConsumerCount(1)
      .maxConsumerCount(25)
      .loadFactor(1)
      .injector(job -> EsdApplication.mainComponent.inject((BaseJob) job))
      .consumerKeepAlive(60);

    return new JobManager(builder.build());
  }

}