package sapotero.rxtest.jobs.rx;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import sapotero.rxtest.events.rx.InsertRxDocumentsEvent;
import sapotero.rxtest.jobs.bus.BaseJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import timber.log.Timber;

public class InsertRxDocumentsJob extends BaseJob {
  public static final int PRIORITY = 1;
  private final List<Document> documents;

  private String TAG = InsertRxDocumentsJob.this.getClass().getSimpleName();

  public InsertRxDocumentsJob(List<Document> docs) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    documents = docs;
    Timber.tag(TAG).v( "construct:"+documents.toString());
  }

  @Override
  public void onAdded() {
    Timber.tag(TAG).v( "onAdded");
    EventBus.getDefault().post( new InsertRxDocumentsEvent("DONE " ) );
  }

  @Override
  public void onRun() throws Throwable {
    Timber.tag(TAG).v( "onRun" + documents.size() );
//    BriteDatabase.Transaction transaction = db.newTransaction();
//
//    try {
//      for (Integer i = 0; i< documents.size(); i++){
//        Document document = documents.get(i);
//
//        long signer = db.insert(RxSigner.TABLE,
//          new RxSigner.Builder()
//            .id( document.getSigner().getId() )
//            .name( document.getSigner().getName() )
//            .type( document.getSigner().getType() )
//            .organisation( document.getSigner().getOrganisation() )
//            .build()
//        );
//
//        db.insert( RxDocuments.TABLE,
//          new RxDocuments.Builder()
//            .uid( document.getUid() )
//            .md5( document.getMd5() )
//            .sort_key( document.getSortKey().toString() )
//            .title( document.getTitle() )
//            .registration_number( document.getRegistrationNumber() )
//            .registration_date( document.getRegistrationDate() )
//            .urgency( document.getUrgency() != null ? document.getUrgency().toString() : "" )
//            .short_description( document.getShortDescription() )
//            .comment( document.getComment() )
//            .external_document_number( document.getExternalDocumentNumber() )
//            .receipt_date(String.valueOf(document.getReceiptDate()))
//            .signer_id(String.valueOf(signer))
//            .viewed(String.valueOf(document.getViewed()))
//            .build()
//        );
//      }
//      EventBus.getDefault().post( new InsertRxDocumentsEvent("ALL DONE" ) );
//      transaction.markSuccessful();
//    } finally {
//      transaction.end();
//    }
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
