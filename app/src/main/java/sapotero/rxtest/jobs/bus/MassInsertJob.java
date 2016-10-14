package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.squareup.sqlbrite.BriteDatabase;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.db.models.RxAuth;
import sapotero.rxtest.events.bus.MassInsertDoneEvent;
import timber.log.Timber;

public class MassInsertJob extends BaseJob {
  public static final int PRIORITY = 1;

  private String TAG = MassInsertJob.this.getClass().getSimpleName();
  private Integer add_count = 50;

  public MassInsertJob(Integer count) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    add_count = count;
    Timber.tag(TAG).v( "construct "+ add_count );
  }

  @Override
  public void onAdded() {
    EventBus.getDefault().post( new MassInsertDoneEvent("DONE " + add_count) );
  }

  @Override
  public void onRun() throws Throwable {
    EventBus.getDefault().post( new MassInsertDoneEvent(TAG + " start") );
    BriteDatabase.Transaction transaction = db.newTransaction();

    try {
      for (Integer i = 0; i< add_count; i++){
        db.insert( RxAuth.TABLE,
          new RxAuth.Builder()
            .login("test" + Math.random() )
            .token("test" + Math.random() )
            .collegue_login("")
            .collegue_token("")
            .build()
        );
      }
      transaction.markSuccessful();
    } finally {
      transaction.end();
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
