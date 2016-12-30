package sapotero.rxtest.jobs.service;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.service.AuthServiceAuthSignInEvent;
import sapotero.rxtest.jobs.bus.BaseJob;

public class AuthServiceCheckSignJob extends BaseJob {

  private String TAG = this.getClass().getSimpleName();

  public static final int PRIORITY = 10;
  private final String password;

    public AuthServiceCheckSignJob(String password) {
      super( new Params(PRIORITY).requireNetwork().persist() );
      this.password = password;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
      EventBus.getDefault().post( new AuthServiceAuthSignInEvent(password) );
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
