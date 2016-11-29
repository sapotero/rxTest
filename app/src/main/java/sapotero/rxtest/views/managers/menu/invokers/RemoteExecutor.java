package sapotero.rxtest.views.managers.menu.invokers;

import sapotero.rxtest.views.managers.menu.interfaces.Command;

public class RemoteExecutor {
  private final String TAG = this.getClass().getSimpleName();
  private Command command;


  public RemoteExecutor setCommand(Command command){
    this.command = command;

    return this;
  }
  public void execute(){
    command.execute();
  }

}
