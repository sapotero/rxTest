package sapotero.rxtest.utils.queue.threads;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.utils.queue.interfaces.JobCountInterface;
import sapotero.rxtest.utils.queue.threads.handlers.ThreadRejectedExecutionHandler;
import sapotero.rxtest.utils.queue.threads.producers.LocalCommandProducer;
import sapotero.rxtest.utils.queue.threads.producers.RemoteCommandProducer;
import sapotero.rxtest.utils.queue.threads.utils.SuperVisor;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class QueueSupervisor implements JobCountInterface {

  private String TAG = this.getClass().getSimpleName();

  private final Context context;
  private final CommandFactory commandFactory;
  private ThreadPoolExecutor   commandPool;

  public static int THREAD_POOL_SIZE = 8;
  public static int MAX_FILE_SIZE    = 1*1024*1024;

  public QueueSupervisor(Context context) {

    this.context = context;
    this.commandFactory = CommandFactory.getInstance();

    start();
  }

  private void start() {

    ThreadRejectedExecutionHandler rejectionHandler = new ThreadRejectedExecutionHandler();
    ThreadFactory threadFactory = Executors.defaultThreadFactory();

    commandPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, 10, 10*60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), threadFactory, rejectionHandler);

    SuperVisor monitor   = new SuperVisor(commandPool, 5);
    Thread monitorThread = new Thread(monitor);
    monitorThread.start();
  }

  public Command create(QueueEntity task){
    Command command = null;

    try {
      CommandParams params = new Gson().fromJson( task.getParams(), CommandParams.class );

      command = commandFactory
        .withDocument( new DocumentReceiver( params.getDocument() ) )
        .withParams( params )
        .build( CommandFactory.Operation.getOperation( task.getCommand() ) );

      if (command != null) {
        Timber.tag(TAG).v("create command %s", command.getParams().toString() );
      }

    } catch (JsonSyntaxException error) {
      Timber.tag(TAG).v("create command error %s", error );
    }

    return command;
  }

  public void addLocal(QueueEntity command){
    add(command, true);
  }

  public void addRemote(QueueEntity command){
    add(command, false);
  }

  public void add(QueueEntity command, boolean executeLocal) {
    Runnable producedCommand;

    if ( executeLocal) {
      producedCommand = new LocalCommandProducer(  create(command), context);
    } else {
      producedCommand = new RemoteCommandProducer( create(command), context);
    }

    Timber.tag(TAG).i("local %s | %s", executeLocal, producedCommand.toString() );

    commandPool.execute( producedCommand );
  }


  /* JobCountInterface */
  @Override
  public int getRunningJobsCount(){
    return commandPool.getActiveCount();
  }
}
