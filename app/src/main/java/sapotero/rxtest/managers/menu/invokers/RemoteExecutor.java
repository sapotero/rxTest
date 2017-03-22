package sapotero.rxtest.managers.menu.invokers;

import sapotero.rxtest.managers.menu.interfaces.Command;
import timber.log.Timber;

public class RemoteExecutor {
  private final String TAG = this.getClass().getSimpleName();
  private Command command;


  public RemoteExecutor setCommand(Command command){
    this.command = command;

    return this;
  }
  public void execute(){
    Timber.tag(TAG).i("start execute");
    command.executeRemote();
  }

}
