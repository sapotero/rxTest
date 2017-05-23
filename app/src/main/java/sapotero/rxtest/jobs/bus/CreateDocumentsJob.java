package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinks;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.adapter.UpdateDocumentAdapterEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import timber.log.Timber;

public class CreateDocumentsJob extends BaseJob {

  public static final int PRIORITY = 1;
  private boolean shared = false;
  private boolean not_processed;
  private String status;
  private String journal;

  private Boolean onControl;
  private Boolean isProcessed = null;
  private Boolean isFavorites = null;

  private Fields.Status filter;
  private String uid;
  private String TAG = this.getClass().getSimpleName();
  private DocumentInfo document;

  private int jobCount;

  public CreateDocumentsJob(String uid, String journal, String status, boolean shared) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.shared = shared;

    if (journal != null) {
      String[] index = journal.split("_production_db_");
      this.journal = index[0];
    }

    this.status  = status;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(settings.getHost() + "v3/documents/")
      .client(okHttpClient)
      .build();

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
    DocumentMapper documentMapper = new DocumentMapper();
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

          if ( result.getImages() != null && result.getImages().size() > 0 ){
            for (RImage _image : result.getImages()) {
              jobCount++;
              RImageEntity image = (RImageEntity) _image;
              jobManager.addJobInBackground( new DownloadFileJob(settings.getHost(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
            }
          }

          if ( doc.getLinks() != null && doc.getLinks().size() > 0 ){
            for (RLinks _link: doc.getLinks()) {
              jobCount++;
              RLinksEntity link = (RLinksEntity) _link;
              jobManager.addJobInBackground( new UpdateLinkJob( link.getUid() ) );
            }
          }

          if ( doc.getRoute() != null ){
            for (RStep _step: ((RRouteEntity) doc.getRoute()).getSteps() ) {
              RStepEntity step = (RStepEntity) _step;

              if ( step.getCards() != null ){
                Card[] cards = new Gson().fromJson(step.getCards(), Card[].class);

                if (cards.length > 0){
                  for (Card card: cards ) {
                    if (card.getUid() != null) {
                      jobCount++;
                      jobManager.addJobInBackground( new UpdateLinkJob( card.getUid() ) );
                    }
                  }
                }
              }
            }
          }

          settings.addJobCount(jobCount);

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
