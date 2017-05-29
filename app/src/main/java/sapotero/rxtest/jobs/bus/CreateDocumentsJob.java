package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.adapter.UpdateDocumentAdapterEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import timber.log.Timber;

public class CreateDocumentsJob extends BaseJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();
  private String uid;

  private String status;
  private String journal;
  private boolean shared = false;

  private int jobCount;

  public CreateDocumentsJob(String uid, String journal, String status, boolean shared) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.journal = getJournalName(journal);
    this.status = status;
    this.shared = shared;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {

    Retrofit retrofit = getRetrofit();
    DocumentService documentService = retrofit.create( DocumentService.class );
    Observable<DocumentInfo> info = documentService.getInfo(
      uid,
      settings.getLogin(),
      settings.getToken()
    );

    info
      .subscribeOn( Schedulers.io() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        doc -> {
          create( doc );
          EventBus.getDefault().post( new StepperLoadDocumentEvent(doc.getUid()) );
        },
        error -> {
          Timber.tag(TAG).e(error);
          EventBus.getDefault().post( new StepperLoadDocumentEvent("Error downloading document info on create") );
        }
      );
  }

  private void create(DocumentInfo document){
    DocumentMapper documentMapper = mappers.getDocumentMapper();
    RDocumentEntity doc = documentMapper.toEntity(document);
    documentMapper.setJournal(doc, journal);
    documentMapper.setFilter(doc, status);
    documentMapper.setShared(doc, shared);

    Timber.tag(TAG).d("signer %s", new Gson().toJson( document.getSigner() ) );

    dataStore
      .insert( doc )
      .toObservable()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          EventBus.getDefault().post( new UpdateCurrentDocumentEvent( doc.getUid() ) );

          jobCount = 0;

          jobCount += loadImages( result.getImages() );
          jobCount += loadLinks( document.getLinks() );
          jobCount += loadCards( document.getRoute() );

          addPrefJobCount(jobCount);

          EventBus.getDefault().post( new UpdateDocumentAdapterEvent( result.getUid(), result.getDocumentType(), result.getFilter() ) );
        },
        error -> {
          Timber.tag(TAG).e(error);
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
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error creating document (job cancelled)") );
  }
}
