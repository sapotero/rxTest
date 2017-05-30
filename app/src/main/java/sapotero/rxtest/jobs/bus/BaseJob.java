package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinks;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Step;
import sapotero.rxtest.utils.Settings;
import timber.log.Timber;

public abstract class BaseJob extends Job {

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject Settings settings;
  @Inject Mappers mappers;
  @Inject SingleEntityStore<Persistable> dataStore;

  protected BaseJob(Params params) {
    super(params);
  }

  public <T> boolean notEmpty(Collection<T> collection) {
    return collection != null && collection.size() > 0;
  }

  public boolean exist(Object obj) {
    return obj != null;
  }

  public void addPrefJobCount(int value) {
    settings.addJobCount(value);
  }

  public String getJournalName(String journal) {
    String journalName = "";

    if ( exist( journal ) ) {
      String[] index = journal.split("_production_db_");
      journalName = index[0];
    }

    return journalName;
  }

  public Observable<DocumentInfo> getDocumentInfoObservable(String uid) {
    Retrofit retrofit = getRetrofit();
    DocumentService documentService = retrofit.create( DocumentService.class );
    return documentService.getInfo(
            uid,
            settings.getLogin(),
            settings.getToken()
    );
  }

  public Retrofit getRetrofit() {
    return new Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(settings.getHost() + "v3/documents/")
            .client(okHttpClient)
            .build();
  }

  public RDocumentEntity createDocument(DocumentInfo documentReceived, String status, boolean shared) {
    DocumentMapper documentMapper = mappers.getDocumentMapper();
    RDocumentEntity doc = documentMapper.toEntity(documentReceived);

    documentMapper.setFilter(doc, status);
    documentMapper.setShared(doc, shared);

    return doc;
  }

  public void saveDocument(DocumentInfo documentReceived, RDocumentEntity documentToSave, String TAG) {
    dataStore
      .insert( documentToSave )
      .toObservable()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          Timber.tag(TAG).d("Created " + result.getUid());
          loadLinkedData( documentReceived, result );
        },
        error -> {
          Timber.tag(TAG).e(error);
        }
      );
  }

  public void updateDocument(DocumentInfo documentReceived, RDocumentEntity documentToUpdate, String TAG) {
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
        error -> {
          Timber.tag(TAG).e(error);
        }
      );
  }

  public void loadLinkedData(DocumentInfo documentReceived, RDocumentEntity documentSaved) {
    int jobCount = 0;

    jobCount += loadImages( documentSaved.getImages() );
    jobCount += loadLinks( documentReceived.getLinks() );
    jobCount += loadCards( documentReceived.getRoute() );

    addPrefJobCount(jobCount);
  }

  public int loadImages(Set<RImage> images) {
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

  public int loadLinks(List<String> links) {
    int jobCount = 0;

    if ( notEmpty( links) ) {
      for (String link : links) {
        jobCount++;
        jobManager.addJobInBackground( new UpdateLinkJob( link ) );
      }
    }

    return jobCount;
  }

  public int loadCards(Route route) {
    int jobCount = 0;

    if ( exist( route ) ) {
      if ( notEmpty( route.getSteps() ) ) {
        for (Step step : route.getSteps()) {
          if ( notEmpty( step.getCards() ) ) {
            for (Card card : step.getCards()) {
              if ( exist( card.getUid() ) ) {
                jobCount++;
                jobManager.addJobInBackground( new UpdateLinkJob( card.getUid() ) );
              }
            }
          }
        }
      }
    }

    return jobCount;
  }
}