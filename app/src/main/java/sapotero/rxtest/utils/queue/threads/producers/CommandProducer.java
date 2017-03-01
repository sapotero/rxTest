package sapotero.rxtest.utils.queue.threads.producers;

import android.content.Context;

import sapotero.rxtest.views.managers.menu.interfaces.Command;
import timber.log.Timber;

public class CommandProducer implements Runnable, AutoCloseable {

  private final Command command;
  private final Context context;

  public CommandProducer(Command command, Context context) {
    Timber.tag("CommandProducer").d( "command: %s", command.toString() );

    this.command = command;
    this.context = context;

  }

  @Override
  public void run() {
    processCommand();
  }

  private void processCommand() {
    try {
      Timber.tag("CommandProducer").e( "star: %s", command.toString() );
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception {
    Timber.tag("CommandProducer").e( "close" );
  }
}