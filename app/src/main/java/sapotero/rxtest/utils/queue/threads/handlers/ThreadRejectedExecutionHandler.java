package sapotero.rxtest.utils.queue.threads.handlers;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import timber.log.Timber;

public class ThreadRejectedExecutionHandler implements RejectedExecutionHandler {
  private final String TAG = this.getClass().getSimpleName();
  
  @Override
  public void rejectedExecution(Runnable worker, ThreadPoolExecutor executor) {
    Timber.tag(TAG).w("%s is Rejected", worker.toString());

    Timber.tag(TAG).w("Retrying to Execute");

    try{
      Timber.tag(TAG).w("%s Execution Started ", worker.toString());
    }
    catch(Exception e)
    {
      Timber.tag(TAG).w("Failure to Re-exicute %s", e.getMessage());
    }
  }

}