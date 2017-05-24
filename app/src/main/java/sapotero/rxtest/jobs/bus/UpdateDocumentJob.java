package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.adapter.UpdateDocumentAdapterEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Step;
import timber.log.Timber;

public class UpdateDocumentJob extends BaseJob {

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

  public UpdateDocumentJob(String uid, Fields.Status filter) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.filter = filter;
  }

  public UpdateDocumentJob(String uid, String journal, String status, boolean shared) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.shared = shared;

    String[] index = journal.split("_production_db_");
    this.journal = index[0];

    this.status  = status;
    this.not_processed = true;
  }

  public UpdateDocumentJob(String uid, String status, boolean shared) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("SyncDocument") );
    this.uid     = uid;
    if (!Objects.equals(status, "")){
      this.status  = status;
    }
    this.shared = shared;
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
          document = doc;
          Timber.tag(TAG).d("recv title - %s %s", doc.getTitle(), shared );

          update( exist(doc.getUid()) );

          EventBus.getDefault().post( new StepperLoadDocumentEvent(doc.getUid()) );

          jobCount = 0;

          if ( doc.getLinks() != null && doc.getLinks().size() > 0 ){

            for (String link: doc.getLinks()) {
              jobCount++;
              jobManager.addJobInBackground( new UpdateLinkJob( link ) );
            }

          }

          if ( doc.getRoute() != null && doc.getRoute().getSteps().size() > 0 ){
            for (Step step: doc.getRoute().getSteps() ) {
              if ( step.getCards() != null && step.getCards().size() > 0){
                for (Card card: step.getCards() ) {
                  if (card.getUid() != null) {
                    jobCount++;
                    jobManager.addJobInBackground( new UpdateLinkJob( card.getUid() ) );
                  }
                }
              }
            }
          }

          addPrefJobCount(jobCount);
        },
        error -> {
          error.printStackTrace();
          EventBus.getDefault().post( new StepperLoadDocumentEvent("Error downloading document info on update") );
        }

      );
  }

  @NonNull
  private Boolean exist(String uid){

    boolean result = false;

    Integer count = dataStore
      .count(RDocumentEntity.UID)
      .where(RDocumentEntity.UID.eq(uid))
      .get().value();

    if( count != 0 ){
      result = true;

      RDocumentEntity doc = dataStore.select(RDocumentEntity.class).where(RDocumentEntity.UID.eq(uid)).get().firstOrNull();

      if (doc != null) {

        new DocumentMapper().setShared(doc, shared);

        dataStore
          .update(doc)
          .toObservable()
          .subscribeOn( Schedulers.io() )
          .observeOn( AndroidSchedulers.mainThread() )
          .subscribe(
            data -> {
              Timber.tag(TAG).e("UPDATED %s %s", uid, shared);
            }, error -> {
              Timber.tag(TAG).e(error);
          });
      }
    }


    Timber.tag(TAG).v("exist " + result );

    return result;
  }

  @NonNull
  private Observable<RDocumentEntity> create(DocumentInfo d){
    RDocumentEntity rd = new RDocumentEntity();
    DocumentMapper documentMapper = new DocumentMapper();

    documentMapper.convertSimpleFields(rd, d);
    rd.setControl(onControl);
    documentMapper.setJournal(rd, journal);
    documentMapper.setFilter(rd, status);
    if (filter != null) {
      documentMapper.setFilter(rd, filter.toString());
    }
    documentMapper.setShared(rd, shared);

    return dataStore.insert( rd ).toObservable();
  }

  private void update(Boolean exist){

    if (filter != null) {
      Timber.tag(TAG).d("create title - %s | %s", document.getTitle(), filter.toString() );
    }

    if (exist) {
      updateDocumentInfo();
    } else {
      create(document)
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          this::createNewDocument,
          Throwable::printStackTrace
        );
    }
  }

  private void createNewDocument(RDocumentEntity documentEntity){

    RDocumentEntity rDoc;

    if (documentEntity != null){
      rDoc = documentEntity;
    } else {
      rDoc = dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq( document.getUid() ))
        .get()
        .first();
    }

    Timber.tag(TAG).v("createNewDocument " + rDoc.getRegistrationNumber() );
    Timber.tag(TAG).v("getImages " + document.getImages().size() );
    Timber.tag(TAG).v("getDecisions " + rDoc.getDecisions().size() );
    Timber.tag(TAG).v("getExemplars " + rDoc.getExemplars().size() );
    Timber.tag(TAG).v("getControlLabels " + rDoc.getControlLabels().size() );

    rDoc.setUser( settings.getLogin() );
    rDoc.setProcessed(false);
    rDoc.setFromLinks( false );
    rDoc.setFromProcessedFolder( false );
    rDoc.setFromFavoritesFolder( false );
    rDoc.setChanged( false );

    DocumentMapper documentMapper = new DocumentMapper();

    documentMapper.setSigner( rDoc, document.getSigner() );
    documentMapper.setJournal( rDoc, journal );
    documentMapper.setFilter( rDoc, status );
    if (filter != null) {
      documentMapper.setFilter( rDoc, filter.toString() );
    }

    documentMapper.convertNestedFields(rDoc, document, false);

    dataStore.update(rDoc)
      .toObservable()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          Timber.tag(TAG).d("updated " + result.getUid());
          Timber.tag(TAG).e("%s", shared);

          jobCount = 0;

          if ( result.getImages() != null && result.getImages().size() > 0 && ( isFavorites != null && !isFavorites ) ){

            for (RImage _image : result.getImages()) {
              jobCount++;
              RImageEntity image = (RImageEntity) _image;
              jobManager.addJobInBackground( new DownloadFileJob(settings.getHost(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
            }

          }

          addPrefJobCount(jobCount);
        },
        error ->{
          error.printStackTrace();
        }
      );
  }

  private void updateDocumentInfo(){

    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().first();

    DocumentMapper documentMapper = new DocumentMapper();

    if ( !Objects.equals( document.getMd5(), doc.getMd5() ) ){
      Timber.tag("MD5").d("not equal %s - %s",document.getMd5(), doc.getMd5() );

      doc.setMd5( document.getMd5() );
      doc.setUser( settings.getLogin() );
      doc.setFromLinks( false );
      doc.setChanged( false );

      documentMapper.setSigner(doc, document.getSigner());

      if ( documentMapper.listNotEmpty( document.getDecisions() ) ) {
        doc.getDecisions().clear();
        dataStore.delete(RDecisionEntity.class).where(RDecisionEntity.DOCUMENT_ID.eq(doc.getId())).get().value();
      }

      documentMapper.convertNestedFields(doc, document, false);
      documentMapper.updateProcessed(doc, journal, status, filter);

      EventBus.getDefault().post( new UpdateCurrentDocumentEvent( doc.getUid() ) );

    } else {
      Timber.tag("MD5").d("equal");

      documentMapper.setJournal(doc, journal);

      if (status != null) {
        if (!Objects.equals(doc.getFilter(), status)){
          doc.setFilter( status );
        }
      }

      if (filter != null) {
        doc.setFilter( filter.toString() );
        if (!Objects.equals(doc.getFilter(), filter.getValue())){
          doc.setFilter( status );
        }
      }

      doc.setChanged(false);

      documentMapper.updateProcessed(doc, journal, status, filter);
    }

    if (doc.isFavorites() != null && doc.isFavorites()){
      doc.setFavorites(true);
    } else {
      doc.setFavorites(false);
    }

    if (doc.isControl() != null && doc.isControl()){
      doc.setControl(true);
    } else {
      doc.setControl(false);
    }

    if (doc.isControl() != null && doc.isControl()){
      doc.setControl(true);
    } else {
      doc.setControl(false);
    }

    if (doc.isFromProcessedFolder() != null && doc.isFromProcessedFolder()){
      doc.setFromProcessedFolder(true);
    } else {
      doc.setFromProcessedFolder(false);
    }

    if (doc.isFromFavoritesFolder() != null && doc.isFromFavoritesFolder()){
      doc.setFromFavoritesFolder(true);
    } else {
      doc.setFromFavoritesFolder(false);
    }

    documentMapper.setRoute(doc, document.getRoute());

    dataStore
      .update( doc )
      .subscribeOn( Schedulers.io() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          Timber.tag("MD5").d("updateDocumentInfo update" + result.getMd5());

          EventBus.getDefault().post( new UpdateDocumentAdapterEvent( result.getUid(), result.getDocumentType(), result.getFilter() ) );

          jobCount = 0;

          if ( result.getImages() != null && result.getImages().size() > 0 ){

            for (RImage _image : result.getImages()) {
              jobCount++;
              RImageEntity image = (RImageEntity) _image;
              jobManager.addJobInBackground( new DownloadFileJob(settings.getHost(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
            }

          }

          addPrefJobCount(jobCount);
        },
        error -> {
          Timber.tag(TAG).e("%s", error);
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
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error updating document (job cancelled)") );
  }

  private void addPrefJobCount(int value) {
    settings.addJobCount(value);
  }
}
