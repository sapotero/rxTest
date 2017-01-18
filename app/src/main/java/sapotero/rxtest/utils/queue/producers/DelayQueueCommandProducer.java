package sapotero.rxtest.utils.queue.producers;

import android.content.Context;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import sapotero.rxtest.utils.queue.objects.DelayObject;
import timber.log.Timber;

public class DelayQueueCommandProducer {

  private BlockingQueue queue;
  private final Random random = new Random();
  private volatile Boolean runnable = true;

  public DelayQueueCommandProducer(BlockingQueue queue, Context context) {
    this.queue = queue;
  }

  private Thread producerThread = new Thread(new Runnable() {
    @Override
    public void run() {
      while (runnable) {
        try {

          int delay = random.nextInt(100);
          DelayObject object = new DelayObject( UUID.randomUUID().toString(), delay );

          Timber.d("Put = %s%n", object);

          queue.put(object);

          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }, "Producer");

  public void start(){
    this.producerThread.start();
  }

  public void stop(){
    runnable = false;
  }

}