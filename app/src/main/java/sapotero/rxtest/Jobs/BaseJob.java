package sapotero.rxtest.Jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

public abstract class BaseJob extends Job {
  @Inject
  BriteDatabase db;

  protected BaseJob(Params params) {
    super(params);
  }
}