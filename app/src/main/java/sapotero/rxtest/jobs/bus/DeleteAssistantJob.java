package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import sapotero.rxtest.db.requery.models.RAssistantEntity;


public class DeleteAssistantJob extends BaseJob {

  public static final int PRIORITY = 10;
  private String TAG = this.getClass().getSimpleName();

  public DeleteAssistantJob() {
    super( new Params(PRIORITY).requireNetwork().persist() );

  }

  @Override
  public void onAdded() {
    dataStore
      .delete(RAssistantEntity.class)
      .where(RAssistantEntity.USER.eq(settings.getLogin()))
      .get().value();
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