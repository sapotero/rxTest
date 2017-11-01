package sapotero.rxtest.managers.menu;

import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.invokers.LocalExecutor;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class OperationManager {

  private final String TAG = this.getClass().getSimpleName();

  private  CommandFactory commandBuilder;
  private final LocalExecutor localExecutor;

  public OperationManager() {
    localExecutor = new LocalExecutor();
    commandBuilder = CommandFactory.getInstance();
  }

  public void execute(CommandFactory.Operation operation, CommandParams params) {

    Timber.tag(TAG).i("execute start");

    Command command = commandBuilder
      .withParams( params )
      .build( operation );

    Timber.tag(TAG).i("command startTransactionFor");

    if (command != null) {
      localExecutor
        .setCommand( command )
        .execute();
    }

    Timber.tag(TAG).i("execute end");
  }
}
