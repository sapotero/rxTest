package sapotero.rxtest.jobs.rx;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.bus.SetActiveDecisonEvent;
import sapotero.rxtest.jobs.bus.BaseJob;
import sapotero.rxtest.retrofit.models.document.Decision;
import timber.log.Timber;

public class SetActiveDecisionJob extends BaseJob {

  public static final int PRIORITY = 1;
  private Integer decision;
  private String TAG = SetActiveDecisionJob.class.getSimpleName();

  public SetActiveDecisionJob(Integer DECISON) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.decision = DECISON;
  }

  @Override
  public void onAdded() {
    EventBus.getDefault().post( new SetActiveDecisonEvent( decision ) );
  }

  @Override
  public void onRun() throws Throwable {
    Timber.tag(TAG).v( "onRun"  );
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
