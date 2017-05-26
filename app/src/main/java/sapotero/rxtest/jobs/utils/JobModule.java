package sapotero.rxtest.jobs.utils;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.scopes.ManagerScope;
import sapotero.rxtest.jobs.bus.BaseJob;

@Module
public final class JobModule {

  @Provides
  @ManagerScope
  JobManager provideJobModule(Context context) {
    Configuration.Builder builder = new Configuration.Builder(context)
      .minConsumerCount(8)
      .maxConsumerCount(16)
      .injector(job -> EsdApplication.getManagerComponent().inject((BaseJob) job))
      .consumerKeepAlive(60);

    return new JobManager(builder.build());
  }

}