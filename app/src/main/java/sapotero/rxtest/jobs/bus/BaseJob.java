package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;

public abstract class BaseJob extends Job {
//  @Inject public BriteDatabase db;
  @Inject public OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;


  protected BaseJob(Params params) {
    super(params);
  }


}