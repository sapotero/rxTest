package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.documents.Document;
import timber.log.Timber;

public class InvalidateDocumentsJob extends BaseJob {

  public static final int PRIORITY = 1;

  private List<Document> uids;
  private String index;
  private final String status;
  private String TAG = this.getClass().getSimpleName();
  private DocumentInfo document;


  public InvalidateDocumentsJob(List<Document> uids, String journal, String status) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uids = uids;
    this.status = status;

    String[] index = journal.split("_production_db_");
    this.index = index[0];
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {

    List<RDocumentEntity> dbDocs = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.FILTER.eq(status))
      .and(RDocumentEntity.DOCUMENT_TYPE.eq(index))
      .and(RDocumentEntity.FROM_LINKS.eq(false))
      .and(RDocumentEntity.FROM_FAVORITES_FOLDER.eq(false))
      .and(RDocumentEntity.FROM_PROCESSED_FOLDER.eq(false))
      .and(RDocumentEntity.ADDRESSED_TO_TYPE.eq(""))
      .get()
      .toList();


    ArrayList<String> api_uids = new ArrayList<String>();
    ArrayList<String> db_uids  = new ArrayList<String>();

    if (dbDocs.size() > 0){
      for (RDocumentEntity doc :dbDocs ) {
        Timber.tag(TAG).e("db_uid: %s", doc.getUid());
        db_uids.add( doc.getUid() );
      }
    }
    if ( uids.size() > 0 ){
      for (Document doc :uids ) {
        api_uids.add( doc.getUid() );
      }
    }


    for ( String uid: api_uids ) {
      Timber.tag(TAG).e("api_uid: %s", uid);
      if (db_uids.contains(uid)){
        db_uids.remove(uid);
      }
    }


    for ( String uid: db_uids ) {
      Timber.tag(TAG).e("result db_uid: %s", uid);
    }

    for ( String uid: api_uids ) {
      Timber.tag(TAG).e("result api_uid: %s", uid);
    }


    if (db_uids.size() > 0){
      updateAsProcessed(db_uids, true);
    }

    if (api_uids.size() > 0){
      updateAsProcessed(api_uids, false);
    }


  }

  private void updateAsProcessed(ArrayList<String> uid, Boolean processed) {

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.PROCESSED, processed)
      .where(RDocumentEntity.UID.in(uid))
      .get().value();
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
