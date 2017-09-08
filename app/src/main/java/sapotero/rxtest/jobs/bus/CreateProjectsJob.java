package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import timber.log.Timber;

// Creates projects (statuses: approval and signing)
public class CreateProjectsJob extends DocumentJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();

  private String uid;
  private String status;
  private boolean shared = false;

  public CreateProjectsJob(String uid, String status, boolean shared, String login, String currentUserId) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("DocJob") );
    this.uid = uid;
    this.status = status;
    this.shared = shared;
    this.login = login;
    this.currentUserId = currentUserId;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    loadDocument(uid, TAG);
  }

  @Override
  public void doAfterLoad(DocumentInfo document) {
    RDocumentEntity doc = createDocument(document, status, shared);
    saveDocument(document, doc, false, TAG);
  }

  @Override
  public void doAfterUpdate(RDocumentEntity document) {
    if (document != null) {
      Timber.tag(TAG).e( "doAfterUpdate %s - %s / %s", uid, status, null );
      store.process( document, status, null );
    }
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error creating project (job cancelled)") );
  }
}
