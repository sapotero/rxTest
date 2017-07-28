package sapotero.rxtest.managers.menu;

import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.invokers.OperationExecutor;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.OperationHistory;
import timber.log.Timber;

public class OperationManager implements CommandFactory.Callback {

  private final String TAG = this.getClass().getSimpleName();

  private  CommandFactory commandBuilder;
  private final OperationHistory histrory;
  private final OperationExecutor operationExecutor;

  Callback callback;

  public interface Callback {
    void onExecuteSuccess(String command);
    void onExecuteError();
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public OperationManager() {
    histrory          = new OperationHistory();
    operationExecutor = new OperationExecutor();

    commandBuilder = new CommandFactory();
    commandBuilder.registerCallBack(this);
  }

  public void execute(CommandFactory.Operation operation, CommandParams params) {

    Timber.tag(TAG).i("execute start");

    Command command = commandBuilder
      .withParams( params )
      .build( operation );

    Timber.tag(TAG).i("command startTransactionFor");

    if (command != null) {
      operationExecutor
        .setCommand( command )
        .execute();
    }

    Timber.tag(TAG).i("execute end");
  }


  @Override
  public void onCommandSuccess(String command) {
    Timber.tag(TAG).w("onCommandSuccess");
    if (callback != null) {
      callback.onExecuteSuccess(command);
    }
  }

  @Override
  public void onCommandError() {
    Timber.tag(TAG).w("onCommandError");
  }

}
