package sapotero.rxtest.views.managers.menu.commands;

import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.interfaces.Operation;


public abstract class AbstractCommand implements Command, Operation {

  public Callback callback;

  public interface Callback {
    void onCommandExecuteSuccess();
    void onCommandExecuteError();
  }

}
