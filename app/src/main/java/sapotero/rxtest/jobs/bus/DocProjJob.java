package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Params;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Step;
import timber.log.Timber;

abstract class DocProjJob extends BaseJob {

  DocProjJob(Params params) {
    super(params);
  }

  private <T> boolean notEmpty(Collection<T> collection) {
    return collection != null && collection.size() > 0;
  }

  public boolean exist(Object obj) {
    return obj != null;
  }

  void loadDocument(String uid, String TAG) {
    Observable<DocumentInfo> info = getDocumentInfoObservable(uid);

    info
      .subscribeOn( Schedulers.io() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        doc -> {
          doAfterLoad( doc );
          EventBus.getDefault().post( new StepperLoadDocumentEvent( doc.getUid()) );
        },
        error -> {
          Timber.tag(TAG).e(error);
          EventBus.getDefault().post( new StepperLoadDocumentEvent("Error downloading document info") );
        }
      );
  }

  private Observable<DocumentInfo> getDocumentInfoObservable(String uid) {
    Retrofit retrofit = getRetrofit();
    DocumentService documentService = retrofit.create( DocumentService.class );
    return documentService.getInfo(
            uid,
            settings.getLogin(),
            settings.getToken()
    );
  }

  private Retrofit getRetrofit() {
    return new Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(settings.getHost() + "v3/documents/")
            .client(okHttpClient)
            .build();
  }

  abstract public void doAfterLoad(DocumentInfo document);

  RDocumentEntity createDocument(DocumentInfo documentReceived, String status, boolean shared) {
    DocumentMapper documentMapper = mappers.getDocumentMapper();
    RDocumentEntity doc = documentMapper.toEntity(documentReceived);

    documentMapper.setFilter(doc, status);
    documentMapper.setShared(doc, shared);

    return doc;
  }

  void saveDocument(DocumentInfo documentReceived, RDocumentEntity documentToSave, String TAG) {
    dataStore
      .insert( documentToSave )
      .toObservable()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          Timber.tag(TAG).d("Created " + result.getUid());
          if ( exist( documentReceived ) ) {
            loadLinkedData( documentReceived, result );
          }
        },
        error -> Timber.tag(TAG).e(error)
      );
  }

  void updateDocument(DocumentInfo documentReceived, RDocumentEntity documentToUpdate, String TAG) {
    dataStore
      .update( documentToUpdate )
      .subscribeOn( Schedulers.io() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          Timber.tag(TAG).d("Updated MD5 " + result.getMd5());
          loadLinkedData( documentReceived, result );
          EventBus.getDefault().post( new UpdateCurrentDocumentEvent( result.getUid() ) );
        },
        error -> Timber.tag(TAG).e(error)
      );
  }

  private void loadLinkedData(DocumentInfo documentReceived, RDocumentEntity documentSaved) {
    int jobCount = 0;

    jobCount += loadImages( documentSaved.getImages() );
    jobCount += loadLinks( documentReceived.getLinks() );
    loadCards( documentReceived.getRoute() );

    addPrefJobCount(jobCount);
  }

  private void addPrefJobCount(int value) {
    settings.addJobCount(value);
  }

  private int loadImages(Set<RImage> images) {
    int jobCount = 0;

    if ( notEmpty( images ) ) {
      for (RImage _image : images) {
        jobCount++;
        RImageEntity image = (RImageEntity) _image;
        jobManager.addJobInBackground( new DownloadFileJob( settings.getHost(), image.getPath(), image.getMd5() + "_" + image.getTitle(), image.getId() ) );
      }
    }

    return jobCount;
  }

  private int loadLinks(List<String> links) {
    int jobCount = 0;

    if ( notEmpty( links) ) {
      for (String link : links) {
        jobCount++;
        loadCardByUid(link);
      }
    }

    return jobCount;
  }

  private void loadCards(Route route) {
    Observable
      .just( route )
      .map(Route::getSteps).flatMap(Observable::from)
      .map(Step::getCards).flatMap(Observable::from)
      .map(Card::getUid)
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::loadCardByUid, Timber::e);
  }

  private void loadCardByUid(String uid) {
    jobManager.addJobInBackground( new CreateLinksJob( uid ) );
    addPrefJobCount(1);
  }
}
