package sapotero.rxtest.utils.queue.threads.producers;

import android.content.Context;

import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.invokers.RemoteExecutor;
import timber.log.Timber;

public class RemoteCommandProducer implements Runnable, AutoCloseable {

  private final Command command;
  private final Context context;
  private final RemoteExecutor remoteExecutor;
  private String TAG = this.getClass().getSimpleName();

  public RemoteCommandProducer(Command command, Context context) {
    Timber.tag(TAG).d( "command: %s", command.toString() );

    this.command = command;
    this.context = context;

    remoteExecutor = new RemoteExecutor();

  }

  @Override
  public void run() {
    Timber.tag(TAG).i("start run");
    remoteExecutor
      .setCommand( command )
      .execute();
  }

  private void processCommand() {
    try {
      Timber.tag(TAG).e( "star: %s", command.toString() );
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception {
    Timber.tag(TAG).e( "close" );
  }

}