package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.bus.UpdateDecisionPreviewEvent;


public class UpdateDecisionPreviewJob extends BaseJob {
  public static final int PRIORITY = 1;
  private String id;
  private String TAG = this.getClass().getSimpleName();

  public UpdateDecisionPreviewJob() {
    super( new Params(PRIORITY).requireNetwork().persist() );
  }

  @Override
  public void onAdded() {
    EventBus.getDefault().post( new UpdateDecisionPreviewEvent("") );
  }

  @Override
  public void onRun() throws Throwable {
    Log.d( TAG, "I" );
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
