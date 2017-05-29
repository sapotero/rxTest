package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.gson.Gson;

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

public class UpdateFavoritesDocumentsJob extends BaseJob {

  public static final int PRIORITY = 1;
  private boolean not_processed;
  private String folder;
  private String journal;

  private Boolean onControl;
  private Boolean isProcessed = null;
  private Boolean isFavorites = null;

  private Fields.Status filter;
  private String uid;
  private String TAG = this.getClass().getSimpleName();
  private DocumentInfo document;

  private int jobCount;

  public UpdateFavoritesDocumentsJob(String uid, String folder) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("SyncDocument") );
    this.uid     = uid;
    this.folder = folder;

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
          Timber.tag(TAG).d("recv title - %s", doc.getTitle() );
          Timber.tag(TAG).d("actions - %s", new Gson().toJson( doc.getOperations() ) );

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
          EventBus.getDefault().post( new StepperLoadDocumentEvent("Error downloading favorite document info") );
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
    }

    Timber.tag(TAG).v("exist " + result );

    return result;
  }

  @NonNull
  private Observable<RDocumentEntity> create(DocumentInfo d){
    RDocumentEntity rd = new RDocumentEntity();
    DocumentMapper documentMapper = mappers.getDocumentMapper();

    documentMapper.setSimpleFields(rd, d);
    documentMapper.setJournal(rd, "");
    documentMapper.setFilter(rd, "");
    rd.setFolder(folder);
    rd.setFromFavoritesFolder(true);
    rd.setFavorites(true);

    return dataStore.insert( rd ).toObservable();
  }

  private void update(Boolean exist){


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
    rDoc.setFromFavoritesFolder( true );
    rDoc.setFavorites(true);
    rDoc.setChanged( false );
    rDoc.setControl(onControl);
    rDoc.setFolder(folder);

    DocumentMapper documentMapper = mappers.getDocumentMapper();

    documentMapper.setSigner(rDoc, document.getSigner());
    documentMapper.setJournal(rDoc, "");
    documentMapper.setFilter(rDoc, "");

    documentMapper.setNestedFields(rDoc, document, false);

    dataStore.update(rDoc)
      .toObservable()
      .subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          Timber.tag(TAG).d("updated " + result.getUid());

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

    DocumentMapper documentMapper = mappers.getDocumentMapper();

    if ( !Objects.equals( document.getMd5(), doc.getMd5() ) ){
      Timber.tag("MD5").d("not equal %s - %s",document.getMd5(), doc.getMd5() );

      doc.setMd5( document.getMd5() );

      if (doc.isFavorites() == null){
        doc.setFavorites(isFavorites);
      }

      documentMapper.setSigner(doc, document.getSigner());
      documentMapper.setNestedFields(doc, document, false);
      documentMapper.updateProcessed(doc, journal, folder, filter);

      EventBus.getDefault().post( new UpdateCurrentDocumentEvent( doc.getUid() ) );

    } else {
      Timber.tag("MD5").d("equal");

      documentMapper.setJournal(doc, journal);

      if (folder != null) {
        if (!Objects.equals(doc.getFilter(), folder)){
          doc.setFolder(folder);
        }
      }

      if (filter != null) {
        doc.setFilter( filter.toString() );
        if (!Objects.equals(doc.getFilter(), filter.getValue())){
          doc.setFolder(folder);
        }
      }

      doc.setChanged(false);

      documentMapper.updateProcessed(doc, journal, folder, filter);

      // если обновляем документ
      if ( doc.getDocumentType() != null && doc.getFilter() != null ){
        doc.setProcessed( false );
        doc.setFavorites(true);
      }
    }

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
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error updating favorite document (job cancelled)") );
  }
}
