package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;

import java.util.Collection;
import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import sapotero.rxtest.db.mapper.utils.Mappers;
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

  public <T> boolean notEmpty(Collection<T> collection) {
    return collection != null && collection.size() > 0;
  }

  public boolean notEmpty(String s) {
    return s != null && !Objects.equals(s, "");
  }

  public boolean exist(Object obj) {
    return obj != null;
  }
}