package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.f2prateek.rx.preferences.Preference;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.documents.Document;
import timber.log.Timber;

public class InvalidateDocumentsJob extends BaseJob {

  public static final int PRIORITY = 1;


  private Preference<String> LOGIN = null;
  private Preference<String> TOKEN = null;
  private Preference<String> HOST;

  private List<Document> uids;
  private String index;
  private final String status;
  private String TAG = this.getClass().getSimpleName();
  private DocumentInfo document;


  public InvalidateDocumentsJob(List<Document> uids, String index, String status) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uids = uids;
    this.index = index;
    this.status = status;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {

    HOST  = settings.getString("settings_username_host");
    LOGIN = settings.getString("login");
    TOKEN = settings.getString("token");

    List<RDocumentEntity> dbDocs = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.FILTER.eq(status))
      .get()
      .toList();

    ArrayList<String> db_uids = new ArrayList<String>();

    if (dbDocs.size() > 0){
      for (RDocumentEntity doc :dbDocs ) {
        db_uids.add( doc.getUid() );
      }
    }

    ArrayList<String> api_uids = new ArrayList<String>();
    if ( uids.size() > 0 ){
      for (Document doc :uids ) {
        api_uids.add( doc.getUid() );
      }
    }

    for ( String uid: api_uids ) {
      if (db_uids.contains(uid)){
        db_uids.remove(uid);
      }
    }

    if (db_uids.size() > 0){
      for (String uid: db_uids ) {
        updateAsProcessed(uid);
      }
    }

  }

  private void updateAsProcessed(String uid) {
    int count = dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.PROCESSED, true)
      .where(RDocumentEntity.UID.eq(uid))
      .get()
      .value();
    Timber.tag(TAG).e("updateAsProcessed: %s %s", uid, count);
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
