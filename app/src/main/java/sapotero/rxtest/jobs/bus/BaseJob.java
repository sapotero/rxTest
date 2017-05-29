package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.google.gson.Gson;

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
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RLinks;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Step;
import sapotero.rxtest.utils.Settings;

public abstract class BaseJob extends Job {

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject Settings settings;
  @Inject Mappers mappers;
  @Inject SingleEntityStore<Persistable> dataStore;

  protected BaseJob(Params params) {
    super(params);
  }

  public String getJournalName(String journal) {
    String journalName = "";

    if ( exist( journal ) ) {
      String[] index = journal.split("_production_db_");
      journalName = index[0];
    }

    return journalName;
  }

  public Retrofit getRetrofit() {
    return new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(settings.getHost() + "v3/documents/")
                .client(okHttpClient)
                .build();
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

  public int loadLinks(Set<RLinks> links) {
    int jobCount = 0;

    if ( notEmpty( links) ) {
      for (RLinks _link : links) {
        jobCount++;
        RLinksEntity link = (RLinksEntity) _link;
        jobManager.addJobInBackground( new UpdateLinkJob( link.getUid() ) );
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

  public <T> boolean notEmpty(Collection<T> collection) {
    return collection != null && collection.size() > 0;
  }

  public boolean notEmpty(String s) {
    return s != null && !Objects.equals(s, "");
  }

  public boolean exist(Object obj) {
    return obj != null;
  }

  public void addPrefJobCount(int value) {
    settings.addJobCount(value);
  }
}