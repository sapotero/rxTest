package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;

public abstract class BaseJob extends Job {

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject ISettings settings;
  @Inject Mappers mappers;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject MemoryStore store;

  public String login;
  public String currentUserId;

  protected BaseJob(Params params) {
    super(params);

    // Save user login and id at the moment of job creation, because they can change on substitute mode change
    login = settings.getLogin();
    currentUserId = settings.getCurrentUserId();
  }
}