package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;
import java.util.Date;

import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import timber.log.Timber;

// Creates documents from processed folder (no journal,
public class CreateProcessedDocumentsJob extends DocumentJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();

  private String uid;
  private String folder;

  public CreateProcessedDocumentsJob(String uid, String folder) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("DocJob") );
    this.uid = uid;
    this.folder = folder;
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
    DocumentMapper documentMapper = mappers.getDocumentMapper();
    RDocumentEntity doc = new RDocumentEntity();

    int period = 1;

    try {
      period = Integer.valueOf( settings.getImageDeletePeriod() );
    } catch (NumberFormatException e) {
      Timber.e(e);
    }

    long current = new Date().getTime()/1000 + period * 7 * 24 * 60 * 60;

    doc.setProcessedDate( new BigDecimal( current ).intValueExact() );

    // Положить документ в папку Обработанные, если не адресован текущему пользователю
    if ( !addressedToCurrentUser( document, doc, documentMapper ) ) {
      documentMapper.setSimpleFields(doc, document);
      documentMapper.setNestedFields(doc, document, true);

      documentMapper.setJournal(doc, "");
      documentMapper.setFilter(doc, "");
      documentMapper.setShared(doc, false);
      doc.setFolder(folder);
      doc.setFromProcessedFolder(true);
      doc.setProcessed(true);
      doc.setProcessedDate( new BigDecimal( current ).intValueExact() );

      saveDocument(document, doc, false, TAG);
    }
  }

  @Override
  public void doAfterUpdate(RDocumentEntity document) {
    if (document != null) {
      Timber.tag(TAG).e( "doAfterUpdate %s - %s / %s", uid, null, null );
      store.process( document, null, null );
    }
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error creating processed document (job cancelled)") );
  }
}
