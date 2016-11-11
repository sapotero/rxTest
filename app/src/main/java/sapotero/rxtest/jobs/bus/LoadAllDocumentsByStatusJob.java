package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;
import android.widget.Toast;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.f2prateek.rx.preferences.Preference;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.events.rx.LoadAllDocumentsByStatusEvent;
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

    Retrofit retrofit = new RetrofitManager( getApplicationContext(), Constant.HOST + "/v3/", okHttpClient).process();
    DocumentsService documentsService = retrofit.create( DocumentsService.class );

    Preference<String> LOGIN = settings.getString("login");
    Preference<String> TOKEN = settings.getString("token");

    Observable<Documents> documents = documentsService.getDocuments(LOGIN.get(), TOKEN.get(), filter_type, Integer.valueOf(count), 0);

    documents.subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {

          List<Document> docs = data.getDocuments();
          if ( docs != null && docs.size() > 0 ){
            for (Document d: docs ) {
              insertRDoc(d);
            }
          }

          EventBus.getDefault().post( new LoadAllDocumentsByStatusEvent() );
        },
        error -> {
          Timber.d("_ERROR "+ error.getMessage());
          Toast.makeText(getApplicationContext(), "LoadAllDocumentsByStatusJob " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });

  }

  private void insertRDoc(Document d) {

    Timber.tag(TAG).i( "insertRDoc " + filter_type );

    RDocumentEntity rd = new RDocumentEntity();

    rd.setUid( d.getUid() );
    rd.setFilter(filter_type);
    rd.setMd5( d.getMd5() );
    rd.setSortKey( d.getSortKey() );
    rd.setTitle( d.getTitle() );
    rd.setRegistrationNumber( d.getRegistrationNumber() );
    rd.setRegistrationDate( d.getRegistrationDate() );
    rd.setUrgency( d.getUrgency() );
    rd.setShortDescription( d.getShortDescription() );
    rd.setComment( d.getComment() );
    rd.setExternalDocumentNumber( d.getExternalDocumentNumber() );
    rd.setReceiptDate( d.getReceiptDate() );
    rd.setViewed( d.getViewed() );

    if ( d.getSigner().getOrganisation() != null && !Objects.equals(d.getSigner().getOrganisation(), "")){
      rd.setOrganization( d.getSigner().getOrganisation() );
    } else {
      rd.setOrganization("Без организации" );
    }

    RSignerEntity signer = new RSignerEntity();
    signer.setUid( d.getSigner().getId() );
    signer.setName( d.getSigner().getName() );
    signer.setOrganisation( d.getSigner().getOrganisation() );
    signer.setType( d.getSigner().getType() );

    rd.setSigner( signer );

    dataStore.insert(rd)
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        result -> {
          Timber.d("inserted ++ " + result.getUid());
        },
        error ->{
          error.printStackTrace();
        }
      );
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
