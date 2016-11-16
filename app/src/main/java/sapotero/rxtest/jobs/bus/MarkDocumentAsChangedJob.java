package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.bus.MarkDocumentAsChangedJobEvent;

public class MarkDocumentAsChangedJob extends BaseJob {

  public static final int PRIORITY = 1;
  private String TAG = this.getClass().getSimpleName();

  private String  uid;

  public MarkDocumentAsChangedJob(String uid) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid   = uid;
  }

  @Override
  public void onAdded() {

  }

  @Override
  public void onRun() throws Throwable {
    int rows = dataStore.update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .where(RDocumentEntity.UID.eq(uid))
      .get().value();

    if ( rows > 0 ){
      EventBus.getDefault().post( new MarkDocumentAsChangedJobEvent(uid) );
    }
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
