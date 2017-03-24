package sapotero.rxtest.utils.queue.threads.utils;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ThreadPoolExecutor;

import sapotero.rxtest.events.service.SuperVisorUpdateEvent;
import timber.log.Timber;

public class SuperVisor implements Runnable
{
  private ThreadPoolExecutor executor;

  private int seconds;

  private boolean run=true;

  public SuperVisor(ThreadPoolExecutor executor, int delay)
  {
    this.executor = executor;
    this.seconds=delay;
  }

  public void shutdown(){
    this.run=false;
  }

  @Override
  public void run() {
    while(run){
      Timber.e("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
          this.executor.getPoolSize(),
          this.executor.getCorePoolSize(),
          this.executor.getActiveCount(),
          this.executor.getCompletedTaskCount(),
          this.executor.getTaskCount(),
          this.executor.isShutdown(),
          this.executor.isTerminated()
      );


      try {
        Thread.sleep(seconds*1000);
        EventBus.getDefault().post(new SuperVisorUpdateEvent());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }
}