package sapotero.rxtest.utils.queue.threads.utils;

import android.os.Handler;

import java.util.concurrent.ThreadPoolExecutor;

import timber.log.Timber;

public class SuperVisor implements Runnable
{
  private final Handler handler;
  private ThreadPoolExecutor executor;

  public SuperVisor(ThreadPoolExecutor executor, Handler handler){
    this.executor = executor;
    this.handler  = handler;
  }

  @Override
  public void run() {
    Timber.e("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
      this.executor.getPoolSize(),
      this.executor.getCorePoolSize(),
      this.executor.getActiveCount(),
      this.executor.getCompletedTaskCount(),
      this.executor.getTaskCount(),
      this.executor.isShutdown(),
      this.executor.isTerminated()
    );

    handler.postDelayed(this, 1000);
  }
}