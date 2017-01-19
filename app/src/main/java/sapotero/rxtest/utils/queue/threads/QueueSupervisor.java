package sapotero.rxtest.utils.queue.threads;

import android.content.Context;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import sapotero.rxtest.utils.queue.objects.DelayObject;
import sapotero.rxtest.utils.queue.threads.producers.CommandProducer;
import sapotero.rxtest.utils.queue.threads.utils.SuperVisor;
import sapotero.rxtest.utils.queue.utils.RejectedExecutionHandlerImpl;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import timber.log.Timber;

public class QueueSupervisor {

  private static final int COMMAND_THREAD_COUNT = 2;
  private static final int AUTH_THREAD_COUNT    = 1;

  private final Context context;
  private final DelayQueue commandQueue;
  private final DelayQueue authQueue;

  private ThreadPoolExecutor commandPool;

  public QueueSupervisor(Context context) {

    this.context = context;

    this.commandQueue = new DelayQueue();
    this.authQueue    = new DelayQueue();

    start();
  }

  private void start() {

    RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
    ThreadFactory threadFactory = Executors.defaultThreadFactory();

    commandPool = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2), threadFactory, rejectionHandler);


    SuperVisor monitor = new SuperVisor(commandPool, 3);
    Thread monitorThread = new Thread(monitor);
    monitorThread.start();



  }

  public void stop(){
    Timber.e( "TASKS: %s", commandQueue.size() );
    getInfo();

    //FIX test only
    commandPool.shutdown();
  }

  private void getInfo() {
    Iterator iterator = commandQueue.iterator();
    while ( iterator.hasNext() ){
      DelayObject element = (DelayObject) iterator.next();
      Timber.e( "OBJECT: %s", element.toString() );
    }
  }


  public void add(Command command) {
    commandPool.execute( new CommandProducer(command, context) );
  }
}
