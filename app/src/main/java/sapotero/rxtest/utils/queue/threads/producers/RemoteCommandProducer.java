package sapotero.rxtest.utils.queue.threads.producers;

import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.invokers.RemoteExecutor;
import timber.log.Timber;

public class RemoteCommandProducer implements Runnable, AutoCloseable {

  private final Command command;
  private final RemoteExecutor remoteExecutor;
  private String TAG = this.getClass().getSimpleName();

  public RemoteCommandProducer(Command command) {
    this.command = command;
    remoteExecutor = new RemoteExecutor();
  }

  @Override
  public void run() {
    Timber.tag(TAG).i("start run");
    if (command != null) {
      remoteExecutor
        .setCommand( command )
        .execute();
    }
  }

  @Override
  public void close() throws Exception {
    Timber.tag(TAG).e( "close" );
  }

}