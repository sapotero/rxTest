package sapotero.rxtest.jobs.bus;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.queue.QueueManager;

public abstract class BaseJob extends Job {

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject MemoryStore store;
  @Inject QueueManager queueManager;

  public String login;

  protected BaseJob(Params params) {
    super(params);
  }
}