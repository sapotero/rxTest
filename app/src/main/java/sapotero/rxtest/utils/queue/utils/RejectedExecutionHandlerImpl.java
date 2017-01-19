package sapotero.rxtest.utils.queue.utils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import timber.log.Timber;

public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    Timber.d ("%s is rejected", r.toString());
  }

}