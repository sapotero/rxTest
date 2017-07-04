package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import sapotero.rxtest.db.requery.models.RDocumentEntity;


public class DeleteProcessedImageJob extends BaseJob {

  public static final int PRIORITY = 10;
  private final String uid;
  private String TAG = this.getClass().getSimpleName();

  public DeleteProcessedImageJob(String uid) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
  }

  @Override
  public void onAdded() {
    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .get().firstOrNull();
  }

  @Override
  public void onRun() throws Throwable {

  }


  @Override
  protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }
  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
  }
}