package sapotero.rxtest.utils.queue.threads.consumers;

import android.content.Context;

import java.util.concurrent.BlockingQueue;

import sapotero.rxtest.utils.queue.models.DelayedCommand;
import timber.log.Timber;

public class DelayQueueAuthConsumer {

  private String name;
  private BlockingQueue queue;
  private volatile Boolean runnable = true;

  public DelayQueueAuthConsumer(String name, BlockingQueue queue, Context context) {
    this.name = name;
    this.queue = queue;
  }

  private Thread consumerThread =  new Thread(new Runnable() {
    @Override
    public void run() {
      while (runnable) {
        try {

          if (queue == null){
            break;
          }

          DelayedCommand object = (DelayedCommand) queue.take();
          Timber.d("[%s] - Take = %s%n", Thread.currentThread().getName(), object);
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }, "Consumer");

  public void start(){
    this.consumerThread.setName(name);
    this.consumerThread.start();
  }

  public void stop(){
    runnable = false;
  }

}