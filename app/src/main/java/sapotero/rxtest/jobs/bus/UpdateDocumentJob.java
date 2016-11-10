package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import io.requery.meta.QueryAttribute;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.bus.UpdateDocumentJobEvent;

public class UpdateDocumentJob extends BaseJob {

  public static final int PRIORITY = 1;
  private String TAG = this.getClass().getSimpleName();

  private String  uid;
  private String  field;
  private Boolean value;

  public UpdateDocumentJob(String uid, String field, boolean value) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid   = uid;
    this.field = field;
    this.value = value;
  }

  @Override
  public void onAdded() {

  }

  @Override
  public void onRun() throws Throwable {
    QueryAttribute<RDocumentEntity, Boolean> udpateFiled = Objects.equals(field, "favorites") ? RDocumentEntity.FAVORITES : RDocumentEntity.CONTROL;
    int rows = dataStore.update(RDocumentEntity.class)
      .set(udpateFiled, value)
      .where(RDocumentEntity.UID.eq(uid))
      .get().value();

    Log.d( TAG, " UpdateDocumentJob " + rows + "  " + uid );
    if ( rows > 0 ){
      EventBus.getDefault().post( new UpdateDocumentJobEvent(uid, field, value) );
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
