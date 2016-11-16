package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;
import android.widget.Toast;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.f2prateek.rx.preferences.Preference;

import java.util.List;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import timber.log.Timber;

public class LoadAllDocumentsByStatusJob extends BaseJob {

  private String TAG = this.getClass().getSimpleName();

  public static final int PRIORITY = 1;
  
  private final int index;
  private final String count;
  private String filter_type;
  private Preference<String> HOST;

  public LoadAllDocumentsByStatusJob(int index, String total) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.index = index;
    this.count = total;

  }

  @Override
  public void onAdded() {
    Timber.tag(TAG).v( "onRun"  );
  }

  @Override
  public void onRun() throws Throwable {
    Timber.tag(TAG).v( "onRun"  );

    String[] values = getApplicationContext().getResources().getStringArray(R.array.FILTER_TYPES_VALUE);
    filter_type = values[index];

    HOST = settings.getString("settings_username_host");

    Retrofit retrofit = new RetrofitManager( getApplicationContext(), HOST.get() + "/v3/", okHttpClient).process();
    DocumentsService documentsService = retrofit.create( DocumentsService.class );

    Preference<String> LOGIN = settings.getString("login");
    Preference<String> TOKEN = settings.getString("token");

    Observable<Documents> documents = documentsService.getDocuments(LOGIN.get(), TOKEN.get(), filter_type, Integer.valueOf(count), 0);

    documents
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {

          Timber.d("LOAD "+ data.getDocuments().size() );
          List<Document> docs = data.getDocuments();
          insertRDocMass(docs);
        },
        error -> {
          Timber.d("_ERROR "+ error.getMessage());
          Toast.makeText(getApplicationContext(), "LoadAllDocumentsByStatusJob " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });

  }

  private void insertRDocMass(List<Document> docs) {
    Timber.tag(TAG).i( "insertRDoc " + docs.size() );

    for (Document d: docs) {
      jobManager.addJobInBackground( new SyncDocumentsJob( d.getUid(), filter_type ) );
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
