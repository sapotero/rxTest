package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Params;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.Collections;
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

abstract class DocumentJob extends BaseJob {

  DocumentJob(Params params) {
    super(params);
  }

  public <T> boolean notEmpty(Collection<T> collection) {
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

  void saveDocument(DocumentInfo documentReceived, RDocumentEntity documentToSave, boolean isLink, String TAG) {
    dataStore
      .insert( documentToSave )
      .toObservable()
      .subscribeOn( Schedulers.io() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {

          Timber.tag(TAG).d("Created " + result.getUid());
          loadLinkedData( documentReceived, result, isLink );
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
          doAfterUpdate(result);
          loadLinkedData( documentReceived, result, false );
          EventBus.getDefault().post( new UpdateCurrentDocumentEvent( result.getUid() ) );
        },
        error -> Timber.tag(TAG).e(error)
      );
  }

  abstract public void doAfterUpdate(RDocumentEntity document);

  private void loadLinkedData(DocumentInfo documentReceived, RDocumentEntity documentSaved, boolean isLink) {
    if ( !isLink ) {
      loadImages( documentSaved.getImages() );
      loadLinks( documentReceived.getLinks() );
      loadCards( documentReceived.getRoute() );
    }
  }

  private void addPrefJobCount(int value) {
    settings.addJobCount(value);
  }

  private void loadImages(Set<RImage> images) {
    if ( notEmpty( images ) ) {
      for (RImage _image : images) {
        addPrefJobCount(1);
        RImageEntity image = (RImageEntity) _image;
        jobManager.addJobInBackground( new DownloadFileJob( settings.getHost(), image.getPath(), image.getMd5() + "_" + image.getTitle(), image.getId() ) );
      }
    }
  }

  private void loadLinks(List<String> links) {
    if ( notEmpty( links) ) {
      for (String link : links) {
        loadLinkedDoc( link );
      }
    }
  }

  private void loadCards(Route route) {
    if ( exist( route ) ) {
      for (Step step : nullGuard( route.getSteps() )) {
        for (Card card : nullGuard( step.getCards() )) {
          loadLinkedDoc( card.getUid() );
        }
      }
    }
  }

  // Return empty list if input list is null
  private <T> List<T> nullGuard(List<T> list) {
    return list != null ? list : Collections.EMPTY_LIST;
  }

  private void loadLinkedDoc(String uid) {
    if ( exist( uid ) ) {
      addPrefJobCount(1);
      jobManager.addJobInBackground( new CreateLinksJob( uid ) );
    }
  }
}