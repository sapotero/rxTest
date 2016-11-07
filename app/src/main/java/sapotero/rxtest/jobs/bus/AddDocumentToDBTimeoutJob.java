package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.bus.AddDocumentToDBTimeoutEvent;

public class AddDocumentToDBTimeoutJob extends BaseJob {

  public static final int PRIORITY = 1;
  private String TAG = this.getClass().getSimpleName();

  public AddDocumentToDBTimeoutJob() {
    super( new Params(PRIORITY).requireNetwork().persist() );
  }

  @Override
  public void onAdded() {

  }

  @Override
  public void onRun() throws Throwable {
    EventBus.getDefault().post( new AddDocumentToDBTimeoutEvent() );
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
