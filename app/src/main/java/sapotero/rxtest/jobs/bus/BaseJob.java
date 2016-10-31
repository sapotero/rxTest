package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

public abstract class BaseJob extends Job {
  @Inject public BriteDatabase db;
  @Inject public OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;


  protected BaseJob(Params params) {
    super(params);
  }
}