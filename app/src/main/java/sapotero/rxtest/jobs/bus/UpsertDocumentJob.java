package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.models.documents.Document;
import timber.log.Timber;

public class UpsertDocumentJob extends BaseJob {

  public static final int PRIORITY = 1;

  private Document document;
  private String index  = null;
  private String filter = null;


  public UpsertDocumentJob(Document uid, String index, String filter) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.document = uid;
    this.index = index;
    this.filter = filter;
  }


  @Override
  protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override
  public void onAdded() {

  }

  @Override
  public void onRun() throws Throwable {

    Integer count = dataStore
      .count(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(document.getUid()))
      .get()
      .value();

    if (count > 0){

      dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq( document.getUid() ))
        .get()
        .toObservable()
        .subscribe( doc -> {
          if (!Objects.equals(doc.getMd5(), document.getMd5())){
            jobManager.addJobInBackground( new UpdateDocumentJob(document.getUid(), index, filter) );
          }
        },
          Timber::e);


    } else {
      jobManager.addJobInBackground( new CreateDocumentsJob(document.getUid(), index, filter, false) );
    }

  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error updating document (job cancelled)") );
  }
}
