package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import timber.log.Timber;

// Updates ordinary documents, projects, documents from favorite folder and documents from processed folder
public class UpdateDocumentJob extends DocumentJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();

  private String uid;

  public UpdateDocumentJob(String uid) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    loadDocument(uid, TAG);
  }

  @Override
  public void doAfterLoad(DocumentInfo documentReceived) {

    RDocumentEntity documentExisting = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().firstOrNull();

    if ( exist( documentExisting ) ) {
      if ( !Objects.equals( documentReceived.getMd5(), documentExisting.getMd5() ) ) {
        Timber.tag(TAG).d( "MD5 not equal %s - %s", documentReceived.getMd5(), documentExisting.getMd5() );

        DocumentMapper documentMapper = mappers.getDocumentMapper();
        documentMapper.setBaseFields( documentExisting, documentReceived );
        deleteDecisions( documentExisting );

        boolean isFromProcessedFolder = Boolean.TRUE.equals( documentExisting.isFromProcessedFolder() );

        documentMapper.setNestedFields( documentExisting, documentReceived, isFromProcessedFolder );

        if ( !isFromProcessedFolder ) {
          // если прилетело обновление и документ не из папки обработанных - уберем из обработанных
          documentExisting.setProcessed( false );
        }

        updateDocument( documentReceived, documentExisting, TAG );

      } else {
        Timber.tag(TAG).d("MD5 equal");
      }
    }
  }

  private void deleteDecisions(RDocumentEntity documentExisting) {
    documentExisting.getDecisions().clear();
    dataStore
      .delete(RDecisionEntity.class)
      .where(RDecisionEntity.DOCUMENT_ID.eq(documentExisting.getId()))
      .get().value();
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error updating document (job cancelled)") );
  }
}