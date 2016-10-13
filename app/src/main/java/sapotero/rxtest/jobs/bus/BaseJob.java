package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

public abstract class BaseJob extends Job {
  @Inject BriteDatabase db;
  @Inject OkHttpClient okHttpClient;

  protected BaseJob(Params params) {
    super(params);
  }
}