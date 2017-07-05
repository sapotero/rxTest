package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import timber.log.Timber;


public class ReloadProcessedImageJob extends BaseJob {

  public static final int PRIORITY = 10;
  private final String uid;
  private String TAG = this.getClass().getSimpleName();

  public ReloadProcessedImageJob(String uid) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
  }

  @Override
  public void onAdded() {
    if (uid != null) {
      Timber.e("DOCUMENT UID: %s", uid);
      RDocumentEntity doc = dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq(uid))
        .get().firstOrNull();

      if (doc != null) {

        doc.setMd5("");

        dataStore
          .update(doc)
          .subscribeOn( Schedulers.io() )
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(
            data -> {
              jobManager.addJobInBackground( new UpdateDocumentJob(doc.getUid()) );
            }, Timber::e);

      }
    }
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